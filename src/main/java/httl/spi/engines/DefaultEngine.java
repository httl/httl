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
import httl.spi.Translator;
import httl.spi.loaders.StringLoader;
import httl.util.ClassUtils;
import httl.util.ConfigUtils;
import httl.util.StringUtils;
import httl.util.UrlUtils;
import httl.util.VolatileReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

/**
 * DefaultEngine. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DefaultEngine extends Engine {

    private final StringLoader stringLoader = new StringLoader();

    private Loader loader;

    private Parser parser;

    private Translator translator;

    private Logger logger;

    private Map<Object, Object> templateCache;

    private Map<Object, Object> expressionCache;

    private boolean reloadable;
    
    private boolean precompiled;

    // The engine configuration name
    private String name;

    // The engine configuration properties
    private Properties properties;
    
    /**
     * Get config path
     */
    public String getName() {
		return name;
	}

    /**
     * Get config value.
     * 
     * @see #getEngine()
     * @param key - config key
     * @return config value
     */
	public String getProperty(String key) {
        String value = properties == null ? null : properties.getProperty(key);
        return value == null ? null : value.trim();
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
     * @param encoding - template encoding
     * @return template instance
     * @throws IOException - If an I/O error occurs
     * @throws ParseException - If the template cannot be parsed
     */
    @SuppressWarnings("unchecked")
	public Template getTemplate(String name, String encoding) throws IOException, ParseException {
		name = UrlUtils.cleanName(name);
		Map<Object, Object> cache = this.templateCache; // safe copy reference
		if (cache == null) {
		    return parseTemplate(name, encoding, null);
		}
		Resource resource = null;
		long lastModified;
        if (reloadable) {
        	resource = loadResource(name, encoding);
        	lastModified = resource.getLastModified();
        } else {
        	lastModified = Long.MIN_VALUE;
        }
        VolatileReference<Template> reference = (VolatileReference<Template>) cache.get(name);
        if (reference == null) {
        	if (cache instanceof ConcurrentMap) {
        		reference = new VolatileReference<Template>(); // quickly
        		VolatileReference<Template> old = (VolatileReference<Template>) ((ConcurrentMap<Object, Object>) cache).putIfAbsent(name, reference);
        		if (old != null) { // duplicate
        			reference = old;
        		}
        	} else {
	        	synchronized (cache) { // cache lock
	        		reference = (VolatileReference<Template>) cache.get(name);
	                if (reference == null) { // double check
	                	reference = new VolatileReference<Template>(); // quickly
	                	cache.put(name, reference);
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
					template = parseTemplate(name, encoding, resource); // slowly
					reference.set(template);
				}
			}
		}
		assert(template != null);
		return template;
	}

    // Parse the template. (No cache)
    private Template parseTemplate(String name, String encoding, Resource resource) throws IOException, ParseException {
    	if (resource == null) {
    		resource = loadResource(name, encoding);
    	}
        try {
            return parser.parse(resource);
        } catch (ParseException e) {
            int offset = e.getErrorOffset();
            if (offset < 0) {
                offset = 0;
            }
            String location = null;
            if (offset > 0) {
                try {
                    Reader reader = resource.getReader();
                    try {
                        location = StringUtils.getLocationMessage(reader, offset);
                    } finally {
                        reader.close();
                    }
                } catch (Throwable t) {
                }
            }
            throw new ParseException("Failed to parse template " + name + ", cause: " + e.getMessage()  + ". occur to offset: " + offset + 
                                     (location == null || location.length() == 0 ? "" : ", " + location) 
                                     + ", stack: " + ClassUtils.toString(e), offset);
        }
    }

	/**
     * Get template resource.
     * 
     * @see #getEngine()
     * @param name - template name
     * @param encoding - template encoding
     * @return template resource
     * @throws IOException - If an I/O error occurs
     * @throws ParseException
     */
    public Resource getResource(String name, String encoding) throws IOException {
    	name = UrlUtils.cleanName(name);
    	return loadResource(name, encoding);
    }

    // Load the resource. (No clean)
    private Resource loadResource(String name, String encoding) throws IOException {
    	Resource resource;
    	if (stringLoader.exists(name)) {
    		resource = stringLoader.load(name, encoding);
    	} else {
    		resource = loader.load(name, encoding);
    	}
    	if (resource == null) {
    		throw new FileNotFoundException("Not found resource " + name);
    	}
    	return resource;
    }

    /**
     * Add literal template resource.
     * 
     * @see #getEngine()
     * @param name - template name
     * @param source - template source
     */
	public void addResource(String name, String source) {
		name = UrlUtils.cleanName(name);
		stringLoader.add(name, source);
	}

	/**
     * Remove literal template resource.
     * 
     * @see #getEngine()
     * @param name - template name
     */
	public void removeResource(String name) {
		name = UrlUtils.cleanName(name);
		stringLoader.remove(name);
	}

	/**
     * Tests whether the resource denoted by this abstract pathname exists.
     * 
     * @see #getEngine()
     * @param name - template name
     * @return exists
     */
    public boolean hasResource(String name) {
    	name = UrlUtils.cleanName(name);
    	return stringLoader.exists(name) || loader.exists(name);
    }

    /**
     * init the engine.
     */
    public void init() {
    	if (logger != null && logger.isInfoEnabled()
    			&& name != null && name.length() > 0) {
    		String realPath = ConfigUtils.getRealPath(name);
            if (realPath != null && realPath.length() > 0) {
            	logger.info("Load httl config from " + realPath + " in " + (name.startsWith("/") ? "filesystem" : "classpath") + ".");
            }
    	}
    }
    
    /**
     * On inited.
     */
    public void inited() {
    	if (precompiled) {
            try {
                List<String> list = loader.list();
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
	 * httl.properties name
	 */
    public void setName(String name) {
    	this.name = name;
    }

    /**
	 * httl.properties
	 */
    public void setProperties(Properties properties) {
		this.properties = properties;
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
	 * httl.properties: translator=httl.spi.translators.DfaTranslator
	 */
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}

}
