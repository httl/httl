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
import httl.util.StringUtils;
import httl.util.VolatileReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

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

	/**
	 * Get template.
	 * 
	 * @param name
	 * @param encoding
	 * @return template
	 * @throws IOException
	 * @throws ParseException
	 */
    @SuppressWarnings("unchecked")
	public Template getTemplate(String name, String encoding) throws IOException, ParseException {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("template name == null");
		}
		Map<Object, Object> cache = this.templateCache; // safe copy reference
		if (cache == null) {
		    return parseTemplate(name, encoding);
		}
		long lastModified;
        if (reloadable) {
        	lastModified = getResource(name, encoding).getLastModified();
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
					template = parseTemplate(name, encoding); // slowly
					reference.set(template);
				}
			}
		}
		assert(template != null);
		return template;
	}

    /**
     * Parse the template. (No cache)
     * 
     * @param name - Template name
     * @param encoding - Template encoding
     * @return Template instance.
     * @throws IOException
     * @throws ParseException
     */
    private Template parseTemplate(String name, String encoding) throws IOException, ParseException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("template name == null");
        }
        Resource resource = getResource(name, encoding);
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
     * Get expression.
     * 
     * @param source
     * @param parameterTypes
     * @return
     * @throws ParseException
     */
    @SuppressWarnings("unchecked")
	public Expression getExpression(String source, Map<String, Class<?>> parameterTypes) throws ParseException {
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

	@Override
	public void addResource(String name, String source) {
		stringLoader.add(name, source);
	}

	@Override
	public void removeResource(String name) {
		stringLoader.remove(name);
	}

    /**
     * Get resource.
     * 
     * @param name
     * @param encoding
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public Resource getResource(String name, String encoding) throws IOException {
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

    public boolean hasResource(String name) {
    	return stringLoader.exists(name) || loader.exists(name);
    }

    /**
     * init the engine.
     */
    public void init() {
    	if (precompiled) {
            try {
                List<String> list = loader.list();
                for (String name : list) {
                    try {
                        getTemplate(name);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Set reloadable.
     * 
     * @param reloadable
     */
    public void setReloadable(boolean reloadable) {
    	this.reloadable = reloadable;
    }

    /**
     * Set precompiled.
     * 
     * @param precompiled
     */
    public void setPrecompiled(boolean precompiled) {
    	this.precompiled = precompiled;
    }

    /**
     * Set logger.
     * 
     * @param logger - logger.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Set expression cache.
     * 
     * @param cache expression cache.
     */
    public void setExpressionCache(Map<Object, Object> cache) {
        this.expressionCache = cache;
	}
    
    /**
     * Set template cache.
     * 
     * @param cache template cache.
     */
    public void setTemplateCache(Map<Object, Object> cache) {
        this.templateCache = cache;
	}
    
	/**
	 * Set template loader.
	 * 
	 * @param loader template loader.
	 */
	public void setLoader(Loader loader) {
	    this.loader = loader;
	}

	/**
	 * Set template parser.
	 * 
	 * @param parser template parser.
	 */
	public void setParser(Parser parser) {
		this.parser = parser;
	}

	/**
	 * Set expression translator.
	 * 
	 * @param translator expression translator.
	 */
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}

}
