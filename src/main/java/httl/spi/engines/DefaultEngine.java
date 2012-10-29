/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.spi.engines;

import httl.Engine;
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.spi.Cache;
import httl.spi.Loader;
import httl.spi.Logger;
import httl.spi.Parser;
import httl.spi.Translator;
import httl.util.ClassUtils;
import httl.util.StringUtils;
import httl.util.UrlUtils;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultEngine extends Engine {

    private Logger logger;
    
    private Cache cache;
    
    private Loader loader;

    private Parser parser;

    private Translator translator;

    private boolean reloadable;

    private final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<String, ReentrantLock>();

    /**
     * Get expression.
     * 
     * @param source
     * @param parameterTypes
     * @return
     * @throws ParseException
     */
    public Expression getExpression(String source, Map<String, Class<?>> parameterTypes, int offset) throws ParseException {
        return translator.translate(source, parameterTypes, offset);
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
        return loader.load(name, encoding);
    }
	
	/**
	 * Get template.
	 * 
	 * @param name
	 * @param encoding
	 * @return template
	 * @throws IOException
	 * @throws ParseException
	 */
    public Template getTemplate(String name, String encoding) throws IOException, ParseException {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("template name == null");
		}
		name = UrlUtils.cleanUrl(name.trim());
		Cache cache = this.cache; // safe copy reference
		if (cache == null) {
		    return parseTemplate(name, encoding);
		}
		ReentrantLock lock = locks.get(name);
        if (lock == null) {
            locks.putIfAbsent(name, new ReentrantLock());
            lock= locks.get(name);
        }
        assert(lock != null);
        Resource resource;
        if (reloadable) {
            resource = loader.load(name, encoding);
        } else {
            resource = null;
        }
		Template template = (Template) cache.get(name);
		if (template == null || (resource != null 
		        && resource.getLastModified() > template.getLastModified())) {
    		lock.lock();
    		try {
    			template = (Template) cache.get(name);
    			// double check
    			if (template == null || (resource != null 
    			        && resource.getLastModified() > template.getLastModified())) {
    				template = parseTemplate(name, encoding);
    				cache.put(name, template);
    			}
    		} finally {
    		    lock.unlock();
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
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("template name == null");
        }
        name = UrlUtils.cleanUrl(name.trim());
        Resource resource = loader.load(name, encoding);
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
                    Reader reader = resource.getSource();
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
    
    public void setReloadable(boolean reloadable) {
    	this.reloadable = reloadable;
    }

    public void setPrecompiled(boolean precompiled) {
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
     * Set logger.
     * 
     * @param logger - logger.
     */
    public void setLogger(Logger logger) {
        if (loader == null) {
            throw new IllegalArgumentException("logger == null");
        }
        this.logger = logger;
    }
    
    /**
     * Set template cache.
     * 
     * @param cache template cache.
     */
    public void setCache(Cache cache) {
        this.cache = cache;
	}
    
	/**
	 * Set template loader.
	 * 
	 * @param loader template loader.
	 */
	public void setLoader(Loader loader) {
	    if (loader == null) {
	        throw new IllegalArgumentException("loader == null");
	    }
	    this.loader = loader;
	}

	/**
	 * Set template parser.
	 * 
	 * @param parser template parser.
	 */
	public void setParser(Parser parser) {
	    if (parser == null) {
            throw new IllegalArgumentException("parser == null");
        }
		this.parser = parser;
	}

	/**
	 * Set expression translator.
	 * 
	 * @param translator expression translator.
	 */
	public void setTranslator(Translator translator) {
	    if (translator == null) {
            throw new IllegalArgumentException("translator == null");
        }
		this.translator = translator;
	}

}
