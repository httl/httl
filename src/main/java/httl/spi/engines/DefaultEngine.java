/*
 * Copyright 2011-2013 HTTL Team.
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
import httl.Resource;
import httl.Template;
import httl.internal.util.ConfigUtils;
import httl.internal.util.DelegateMap;
import httl.internal.util.Digest;
import httl.internal.util.StringUtils;
import httl.internal.util.UrlUtils;
import httl.internal.util.VolatileReference;
import httl.spi.Converter;
import httl.spi.Loader;
import httl.spi.Logger;
import httl.spi.Resolver;
import httl.spi.Translator;
import httl.spi.loaders.StringLoader;
import httl.spi.translators.templates.LazyTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
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

	// httl.properties: translator=httl.spi.translators.DefaultTranslator
	private Translator translator;
	
	// httl.properties: resolver=httl.spi.resolvers.SystemResolver
	private Resolver resolver;

	// httl.properties: loggers=httl.spi.loggers.Log4jLogger
	private Logger logger;

	// httl.properties: template.cache=java.util.concurrent.ConcurrentHashMap
	private Map<Object, Object> cache;

	// httl.properties: template.directory=/META-INF/templates
	private String templateDirectory;

	// httl.properties: template.suffix=.httl
	private String[] templateSuffix;

	// httl.properties: reloadable=true
	private boolean reloadable;
	
	// httl.properties: precompiled=true
	private boolean precompiled;
	
	// httl.properties: localized=true
	private boolean localized;

	// httl.properties: use.render.variable.type=false
	private boolean useRenderVariableType;

	// httl.properties: map.converters=httl.spi.converters.BeanMapConverter
	private Converter<Object, Object> mapConverter;

	// httl.properties: name
	private String name;

	// httl.properties: instantiated content
	private Map<String, Object> properties;

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
	 * Get config instantiated value.
	 * 
	 * @see #getEngine()
	 * @param key - config key
	 * @param cls - config value type
	 * @return config value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key, Class<T> cls) {
		if (properties != null) {
			if (cls != null && cls != Object.class && cls != String.class 
					&& ! cls.isInterface() && ! cls.isArray()) {
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
	 * Create context map.
	 * 
	 * @return context map
	 */
	public Map<String, Object> createContext(final Map<String, Object> parent, Map<String, Object> current) {
		return new DelegateMap<String, Object>(parent, current) {
			private static final long serialVersionUID = 1L;
			@Override
			public Object get(Object key) {
				Object value = super.get(key);
				if (value == null && parent == null
						&& resolver != null) {
					return resolver.get((String) key);
				}
				return value;
			}
		};
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
	public Template getTemplate(String name, Locale locale, String encoding, Map<String, Class<?>> parameterTypes) throws IOException, ParseException {
		name = UrlUtils.cleanName(name);
		locale = cleanLocale(locale);
		Map<Object, Object> cache = this.cache; // safe copy reference
		if (cache == null) {
			return parseTemplate(null, name, locale, encoding, parameterTypes);
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
					template = parseTemplate(resource, name, locale, encoding, parameterTypes); // slowly
					reference.set(template);
				}
			}
		}
		assert(template != null);
		return template;
	}

	// Parse the template. (No cache)
	private Template parseTemplate(Resource resource, String name, Locale locale, String encoding, Map<String, Class<?>> parameterTypes) throws IOException, ParseException {
		if (resource == null) {
			resource = loadResource(name, locale, encoding);
		}
		if (useRenderVariableType) {
			return new LazyTemplate(translator, resource, parameterTypes, mapConverter);
		}
		return translator.translate(resource, parameterTypes);
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
	public Template parseTemplate(String source, Map<String, Class<?>> parameterTypes) throws ParseException {
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
		locale = cleanLocale(locale);
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
	 * Tests whether the resource denoted by this abstract pathname exists.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @param locale - resource locale
	 * @return exists
	 */
	public boolean hasResource(String name, Locale locale) {
		name = UrlUtils.cleanName(name);
		locale = cleanLocale(locale);
		return stringLoader.exists(name, locale) || loader.exists(name, locale);
	}
	
	private Locale cleanLocale(Locale locale) {
		if (localized) {
			return locale;
		}
		return null;
	}

	/**
	 * Init the engine.
	 */
	public void init() {
		if (logger != null && StringUtils.isNotEmpty(name)) {
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
				if (StringUtils.isNotEmpty(realPath)) {
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
				int count = 0;
				if (templateSuffix == null) {
					templateSuffix = new String[] { ".httl" };
				}
				for (String suffix : templateSuffix) {
					List<String> list = loader.list(suffix);
					if (list == null) {
						continue;
					}
					count += list.size();
					for (String name : list) {
						try {
							if (logger != null && logger.isDebugEnabled()) {
								logger.debug("Precompiled the template: " + name);
							}
							getTemplate(name);
						} catch (Exception e) {
							if (logger != null && logger.isErrorEnabled()) {
								logger.error(e.getMessage(), e);
							}
						}
					}
				}
				if (logger != null && logger.isInfoEnabled()) {
					logger.info("Precompiled " + count + " templates from directory " + (templateDirectory == null ? "/" : templateDirectory) + " with suffix " + Arrays.toString(templateSuffix));
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
	 * httl.properties: template.directory=/META-INF/templates
	 */
	public void setTemplateDirectory(String templateDirectory) {
		this.templateDirectory = templateDirectory;
	}

	/**
	 * httl.properties: template.suffix=.httl
	 */
	public void setTemplateSuffix(String[] suffix) {
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
	 * httl.properties: localized=true
	 */
	public void setLocalized(boolean localized) {
		this.localized = localized;
	}

	/**
	 * httl.properties: use.render.variable.type=false
	 */
	public void setUseRenderVariableType(boolean useRenderVariableType) {
		this.useRenderVariableType = useRenderVariableType;
	}

	/**
	 * httl.properties: map.converters=httl.spi.converters.BeanMapConverter
	 */
	public void setMapConverter(Converter<Object, Object> mapConverter) {
		this.mapConverter = mapConverter;
	}

	/**
	 * httl.properties: loggers=httl.spi.loggers.Log4jLogger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * httl.properties: cache=java.util.concurrent.ConcurrentHashMap
	 */
	public void setCache(Map<Object, Object> cache) {
		this.cache = cache;
	}
	
	/**
	 * httl.properties: loaders=httl.spi.loaders.ClasspathLoader
	 */
	public void setLoader(Loader loader) {
		this.loader = loader;
	}

	/**
	 * httl.properties: translator=httl.spi.translators.CompileTranslator
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