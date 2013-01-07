/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.engines;

import httl.Engine;
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.spi.Loader;
import httl.spi.Logger;
import httl.spi.Parser;
import httl.spi.Resolver;
import httl.spi.Translator;
import httl.spi.loaders.StringLoader;
import httl.util.ClassUtils;
import httl.util.ConfigUtils;
import httl.util.Digest;
import httl.util.StringUtils;
import httl.util.UrlUtils;
import httl.util.Version;
import httl.util.VolatileReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * DefaultEngine. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#getEngine()
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DefaultEngine extends Engine {

	// The storage for string template.
	// @see parseTemplate()
    private final StringLoader stringLoader;

    // httl.properties: loaders=httl.spi.loaders.ClasspathLoader
    private Loader loader;

	// httl.properties: parser=httl.spi.parsers.CommentParser
    private Parser parser;

    // httl.properties: translator=httl.spi.translators.DefaultTranslator
    private Translator translator;
    
    // httl.properties: resolver=httl.spi.resolvers.SystemResolver
    private Resolver resolver;

	// httl.properties: loggers=httl.spi.loggers.Log4jLogger
    private Logger logger;

    // httl.properties: template.cache=java.util.concurrent.ConcurrentHashMap
    private Map<Object, Object> templateCache;

    // httl.properties: expression.cache=java.util.concurrent.ConcurrentHashMap
    private Map<Object, Object> expressionCache;

    // httl.properties: template.suffix=.httl
    private String templateSuffix;

    // httl.properties: reloadable=true
    private boolean reloadable;
    
    // httl.properties: precompiled=true
    private boolean precompiled;

    // httl.properties: name
    private String name;

	// httl.properties: instantiated content
    private Map<String, Object> properties;

    private final String version = Version.getVersion(DefaultEngine.class, "1.0.0");
    
    public DefaultEngine() {
    	this.stringLoader = new StringLoader(this);
    }

	/**
     * Get config path.
     */
    public String getName() {
		return name;
	}

    /**
     * Get engine version.
     */
	public String getVersion() {
		return version;
	}

    /**
     * Get config instantiated value.
     * 
     * @see #getEngine()
     * @param key - config key
     * @param cls - config value type
     * @return config value
     */
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key, Class<T> cls) {
		if (resolver != null) {
			Object value = resolver.get(key);
			if (value != null) {
				return (T) value;
			}
		}
		if (properties != null) {
			if (cls != null && cls != Object.class 
					&& cls != String.class && ! cls.isInterface()) {
				// engine.getProperty("loaders", ClasspathLoader.class);
				key = key + "=" + cls.getName();
			}
			Object value = properties.get(key);
			if (value != null) {
				if (cls == String.class && value.getClass() != String.class) {
					return (T) value.getClass().getName();
				}
				return (T) value;
			}
		}
		return null;
    }

    /**
     * Get expression.
     * 
     * @see #getEngine()
     * @param source - expression source
     * @param parameterTypes - expression parameter types
     * @return expression instance
     * @throws ParseException - If the expression cannot be parsed
     */
    @SuppressWarnings("unchecked")
	public Expression getExpression(String source, Map<String, Class<?>> parameterTypes) throws ParseException {
    	if (source == null || source.length() == 0) {
    		throw new IllegalArgumentException("expression source == null");
    	}
    	Map<Object, Object> cache = this.expressionCache; // safe copy reference
		if (cache == null) {
		    return translator.translate(source, parameterTypes, 0);
		}
        VolatileReference<Expression> reference = (VolatileReference<Expression>) cache.get(source);
        if (reference == null) {
        	if (cache instanceof ConcurrentMap) {
        		reference = new VolatileReference<Expression>(); // quickly
        		VolatileReference<Expression> old = (VolatileReference<Expression>) ((ConcurrentMap<Object, Object>) cache).putIfAbsent(source, reference);
        		if (old != null) { // duplicate
        			reference = old;
        		}
        	} else {
	        	synchronized (cache) { // cache lock
	        		reference = (VolatileReference<Expression>) cache.get(source);
	                if (reference == null) { // double check
	                	reference = new VolatileReference<Expression>(); // quickly
	                	cache.put(source, reference);
	                }
				}
        	}
        }
        assert(reference != null);
        Expression expression = (Expression) reference.get();
		if (expression == null) {
			synchronized (reference) { // reference lock
				expression = (Expression) reference.get();
				if (expression == null) { // double check
					expression = translator.translate(source, parameterTypes, 0); // slowly
					reference.set(expression);
				}
			}
		}
		assert(expression != null);
		return expression;
    }
    
    /**
     * Get template.
     * 
     * @see #getEngine()
     * @param name - template name
     * @param locale - template locale
     * @param encoding - template encoding
     * @return template instance
     * @throws IOException - If an I/O error occurs
     * @throws ParseException - If the template cannot be parsed
     */
    @SuppressWarnings("unchecked")
	public Template getTemplate(String name, Locale locale, String encoding) throws IOException, ParseException {
		name = UrlUtils.cleanName(name);
		Map<Object, Object> cache = this.templateCache; // safe copy reference
		if (cache == null) {
		    return parseTemplate(name, locale, encoding, null);
		}
		Resource resource = null;
		long lastModified;
        if (reloadable) {
        	resource = loadResource(name, locale, encoding);
        	lastModified = resource.getLastModified();
        } else {
        	lastModified = Long.MIN_VALUE;
        }
        String key = name;
        if (locale != null || encoding != null) {
	        StringBuilder buf = new StringBuilder(name.length() + 20);
	    	buf.append(name);
	    	if (locale != null) {
	    		buf.append("_");
	    		buf.append(locale);
	    	}
	    	if (encoding != null) {
	    		buf.append("_");
	    		buf.append(encoding);
	    	}
	    	key = buf.toString();
        }
        VolatileReference<Template> reference = (VolatileReference<Template>) cache.get(key);
        if (reference == null) {
        	if (cache instanceof ConcurrentMap) {
        		reference = new VolatileReference<Template>(); // quickly
        		VolatileReference<Template> old = (VolatileReference<Template>) ((ConcurrentMap<Object, Object>) cache).putIfAbsent(key, reference);
        		if (old != null) { // duplicate
        			reference = old;
        		}
        	} else {
	        	synchronized (cache) { // cache lock
	        		reference = (VolatileReference<Template>) cache.get(key);
	                if (reference == null) { // double check
	                	reference = new VolatileReference<Template>(); // quickly
	                	cache.put(key, reference);
	                }
				}
        	}
        }
        assert(reference != null);
        Template template = (Template) reference.get();
		if (template == null || template.getLastModified() < lastModified) {
			synchronized (reference) { // reference lock
				template = (Template) reference.get();
				if (template == null || template.getLastModified() < lastModified) { // double check
					template = parseTemplate(name, locale, encoding, resource); // slowly
					reference.set(template);
				}
			}
		}
		assert(template != null);
		return template;
	}

    // Parse the template. (No cache)
    private Template parseTemplate(String name, Locale locale, String encoding, Resource resource) throws IOException, ParseException {
    	if (resource == null) {
    		resource = loadResource(name, locale, encoding);
    	}
        try {
            return parser.parse(resource);
        } catch (ParseException e) {
            int offset = e.getErrorOffset();
            if (offset <= 0) {
                throw e;
            }
            String location = null;
            try {
                Reader reader = resource.getReader();
                try {
                    location = StringUtils.getLocationMessage(name, reader, offset);
                } finally {
                    reader.close();
                }
            } catch (Throwable t) {
            }
            throw new ParseException(e.getMessage()  + ". \nOccur to offset: " + offset + 
                                     (location == null || location.length() == 0 ? "" : ", " + location) 
                                     + ", stack: " + ClassUtils.toString(e), offset);
        }
    }

	/**
     * Get resource.
     * 
     * @see #getEngine()
     * @param name - resource name
	 * @param locale - resource locale
	 * @param encoding - resource encoding
     * @return resource instance
     * @throws IOException - If an I/O error occurs
     */
    public Resource getResource(String name, Locale locale, String encoding) throws IOException {
    	name = UrlUtils.cleanName(name);
    	return loadResource(name, locale, encoding);
    }

    // Load the resource. (No url clean)
    private Resource loadResource(String name, Locale locale, String encoding) throws IOException {
    	Resource resource;
    	if (stringLoader.exists(name, locale)) {
    		resource = stringLoader.load(name, locale, encoding);
    	} else {
    		resource = loader.load(name, locale, encoding);
    	}
    	if (resource == null) {
    		throw new FileNotFoundException("Not found resource " + name);
    	}
    	return resource;
    }

    /**
     * Parse string template.
     * 
     * @see #getEngine()
     * @param source - template source
     * @return template instance
     * @throws IOException - If an I/O error occurs
     * @throws ParseException - If the template cannot be parsed
     */
	public Template parseTemplate(String source) throws ParseException {
		String name = "/$" + Digest.getMD5(source);
        if (! hasResource(name)) {
        	stringLoader.add(name, source);
        }
		try {
			return getTemplate(name);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
     * Tests whether the resource denoted by this abstract pathname exists.
     * 
     * @see #getEngine()
     * @param name - template name
     * @param locale - resource locale
     * @return exists
     */
    public boolean hasResource(String name, Locale locale) {
    	name = UrlUtils.cleanName(name);
    	return stringLoader.exists(name, locale) || loader.exists(name, locale);
    }

    /**
     * Init the engine.
     */
    public void init() {
    	if (logger != null && name != null && name.length() > 0) {
    		if (logger.isWarnEnabled() && ! ConfigUtils.isFilePath(name)) {
    			try {
    				List<String> realPaths = new ArrayList<String>();
					Enumeration<URL> e = Thread.currentThread().getContextClassLoader().getResources(name);
					while (e.hasMoreElements()) {
						URL url = (URL) e.nextElement();
						realPaths.add(url.getFile());
					}
					if (realPaths.size() > 1) {
						logger.warn("Multi httl config in classpath, conflict configs: " + realPaths + ". Please keep only one config.");
					}
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
    		}
    		if (logger.isInfoEnabled()) {
	    		String realPath = ConfigUtils.getRealPath(name);
	            if (realPath != null && realPath.length() > 0) {
	            	logger.info("Load httl config from " + realPath + " in " + (name.startsWith("/") ? "filesystem" : "classpath") + ".");
	            }
    		}
    	}
    }

    /**
     * On all inited.
     */
    public void inited() {
    	if (precompiled) {
            try {
                List<String> list = loader.list(templateSuffix);
                for (String name : list) {
                    try {
                        getTemplate(name);
                    } catch (Exception e) {
                    	if (logger != null && logger.isErrorEnabled()) {
                    		logger.error(e.getMessage(), e);
                    	}
                    }
                }
            } catch (Exception e) {
            	if (logger != null && logger.isErrorEnabled()) {
            		logger.error(e.getMessage(), e);
            	}
            }
        }
    }

    /**
	 * httl.properties: name
	 */
    public void setName(String name) {
    	this.name = name;
    }

    /**
	 * httl.properties: instantiated content
	 */
    public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

    /**
	 * httl.properties: template.suffix=.httl
	 */
    public void setTemplateSuffix(String suffix) {
    	this.templateSuffix = suffix;
    }

    /**
	 * httl.properties: reloadable=true
	 */
    public void setReloadable(boolean reloadable) {
    	this.reloadable = reloadable;
    }

    /**
	 * httl.properties: precompiled=true
	 */
    public void setPrecompiled(boolean precompiled) {
    	this.precompiled = precompiled;
    }

    /**
	 * httl.properties: loggers=httl.spi.loggers.Log4jLogger
	 */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
	 * httl.properties: expression.cache=java.util.concurrent.ConcurrentHashMap
	 */
    public void setExpressionCache(Map<Object, Object> cache) {
        this.expressionCache = cache;
	}
    
    /**
	 * httl.properties: template.cache=java.util.concurrent.ConcurrentHashMap
	 */
    public void setTemplateCache(Map<Object, Object> cache) {
        this.templateCache = cache;
	}
    
    /**
	 * httl.properties: loaders=httl.spi.loaders.ClasspathLoader
	 */
	public void setLoader(Loader loader) {
	    this.loader = loader;
	}

	/**
	 * httl.properties: parser=httl.spi.parsers.CommentParser
	 */
	public void setParser(Parser parser) {
		this.parser = parser;
	}

	/**
	 * httl.properties: translator=httl.spi.translators.DefaultTranslator
	 */
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}

    /**
     * httl.properties: resolver=httl.spi.resolvers.SystemResolver
     */
	public void setResolver(Resolver resolver) {
		this.resolver = resolver;
	}

}
