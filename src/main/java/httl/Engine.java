/*
 * Copyright 2011-2012 HTTL Team.
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
package httl;

import httl.util.BeanFactory;
import httl.util.ConfigUtils;
import httl.util.StringUtils;
import httl.util.Version;
import httl.util.VolatileReference;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Engine. (API, Singleton, Immutable, ThreadSafe)
 * 
 * <pre>
 * Engine engine = Engine.getEngine();
 * </pre>
 * 
 * @see httl.Context#getEngine()
 * @see httl.Template#getEngine()
 * @see httl.Resource#getEngine()
 * @see httl.Expression#getEngine()
 * @see httl.spi.engines.DefaultEngine
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class Engine {

	// Built-in configuration name
	private static final String HTTL_DEFAULT_PROPERTIES = "httl-default.properties";

	// User default configuration name
	private static final String HTTL_PROPERTIES = "httl.properties";

	// The engine name config
	private static final String ENGINE_NAME = "engine.name";

	// The engine singletons cache
	private static final ConcurrentMap<String, VolatileReference<Engine>> ENGINES = new ConcurrentHashMap<String, VolatileReference<Engine>>();

	/**
	 * Get template engine singleton.
	 * 
	 * @return template engine
	 */
	public static Engine getEngine() {
		return getEngine(null, new Properties());
	}

	/**
	 * Get template engine singleton.
	 * 
	 * @param configPath - config path
	 * @return template engine
	 */
	public static Engine getEngine(String configPath) {
		return getEngine(configPath, null);
	}

	/**
	 * Get template engine singleton.
	 * 
	 * @param configProperties - config properties
	 * @return template engine
	 */
	public static Engine getEngine(Properties configProperties) {
		return getEngine(null, configProperties);
	}

	/**
	 * Get template engine singleton.
	 * 
	 * @param configPath - config path
	 * @param configProperties - config properties
	 * @return template engine
	 */
	public static Engine getEngine(String configPath, Properties configProperties) {
		if (StringUtils.isEmpty(configPath)) {
			configPath = HTTL_PROPERTIES;
		}
		VolatileReference<Engine> reference = ENGINES.get(configPath);
		if (reference == null) {
			reference = new VolatileReference<Engine>(); // quickly
			VolatileReference<Engine> old = ENGINES.putIfAbsent(configPath, reference);
			if (old != null) { // duplicate
				reference = old;
			}
		}
		assert(reference != null);
		Engine engine = reference.get();
		if (engine == null) {
			synchronized (reference) { // reference lock
				engine = reference.get();
				if (engine == null) { // double check
					Properties properties = ConfigUtils.mergeProperties(HTTL_DEFAULT_PROPERTIES, configPath, configProperties);
					properties.setProperty(ENGINE_NAME, configPath);
					engine = BeanFactory.createBean(Engine.class, properties); // slowly
					reference.set(engine);
				}
			}
		}
		assert(engine != null);
		return engine;
	}

	/**
	 * Get the engine config name.
	 * 
	 * @return config name
	 */
	public abstract String getName();

	/**
	 * Get the engine version.
	 * 
	 * @return engine version
	 */
	public String getVersion() {
		return Version.getVersion();
	}

	/**
	 * Get config value.
	 * 
	 * @see #getEngine()
	 * @param key - config key
	 * @return config value
	 */
	public Object getProperty(String key) {
		return getProperty(key, Object.class);
	}

	/**
	 * Get config value.
	 * 
	 * @see #getEngine()
	 * @param key - config key
	 * @param defaultValue - default value
	 * @return config value
	 */
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key, String.class);
		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

	/**
	 * Get config value.
	 * 
	 * @see #getEngine()
	 * @param key - config key
	 * @param defaultValue - default value
	 * @return config value
	 */
	public String[] getProperty(String key, String[] defaultValue) {
		String value = getProperty(key, String.class);
		return StringUtils.isEmpty(value) ? defaultValue : StringUtils.splitByComma(value);
	}

	/**
	 * Get config int value.
	 * 
	 * @see #getEngine()
	 * @param key - config key
	 * @param defaultValue - default int value
	 * @return config int value
	 */
	public int getProperty(String key, int defaultValue) {
		String value = getProperty(key, String.class);
		return StringUtils.isEmpty(value) ? defaultValue : Integer.parseInt(value);
	}

	/**
	 * Get config boolean value.
	 * 
	 * @see #getEngine()
	 * @param key - config key
	 * @param defaultValue - default boolean value
	 * @return config boolean value
	 */
	public boolean getProperty(String key, boolean defaultValue) {
		String value = getProperty(key, String.class);
		return StringUtils.isEmpty(value) ? defaultValue : Boolean.parseBoolean(value);
	}

	/**
	 * Get config instantiated value.
	 * 
	 * @see #getEngine()
	 * @param key - config key
	 * @param cls - config value type
	 * @return config value
	 */
	public abstract <T> T getProperty(String key, Class<T> cls);

	/**
	 * Get expression.
	 * 
	 * @see #getEngine()
	 * @param source - expression source
	 * @return expression instance
	 * @throws ParseException - If the expression cannot be parsed
	 */
	public Expression getExpression(String source) throws ParseException {
		return getExpression(source, null);
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
	public abstract Expression getExpression(String source, Map<String, Class<?>> parameterTypes) throws ParseException;

	/**
	 * Get template resource.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @return template resource
	 * @throws IOException - If an I/O error occurs
	 */
	public Resource getResource(String name) throws IOException {
		return getResource(name, null, null);
	}

	/**
	 * Get template resource.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @param encoding - template encoding
	 * @return template resource
	 * @throws IOException - If an I/O error occurs
	 */
	public Resource getResource(String name, String encoding) throws IOException {
		return getResource(name, null, encoding);
	}

	/**
	 * Get template resource.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @param locale - template locale
	 * @return template resource
	 * @throws IOException - If an I/O error occurs
	 */
	public Resource getResource(String name, Locale locale) throws IOException {
		return getResource(name, locale, null);
	}

	/**
	 * Get template resource.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @param locale - template locale
	 * @param encoding - template encoding
	 * @return template resource
	 * @throws IOException - If an I/O error occurs
	 */
	public abstract Resource getResource(String name, Locale locale, String encoding) throws IOException;

	/**
	 * Tests whether the resource denoted by this abstract pathname exists.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @return exists
	 */
	public boolean hasResource(String name) {
		return hasResource(name, null);
	}

	/**
	 * Tests whether the resource denoted by this abstract pathname exists.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @param locale - template locale
	 * @return exists
	 */
	public abstract boolean hasResource(String name, Locale locale);

	/**
	 * Get template.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @return template instance
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed
	 */
	public Template getTemplate(String name) throws IOException, ParseException {
		return getTemplate(name, null, null, null);
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
	public Template getTemplate(String name, String encoding) throws IOException, ParseException {
		return getTemplate(name, null, encoding, null);
	}

	/**
	 * Get template.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @param locale - template locale
	 * @return template instance
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed
	 */
	public Template getTemplate(String name, Locale locale) throws IOException, ParseException {
		return getTemplate(name, locale, null, null);
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
	public Template getTemplate(String name, Locale locale, String encoding) throws IOException, ParseException {
		return getTemplate(name, locale, encoding, null);
	}

	/**
	 * Get template.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @return template instance
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed
	 */
	public Template getTemplate(String name, Map<String, Class<?>> parameterTypes) throws IOException, ParseException {
		return getTemplate(name, null, null, parameterTypes);
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
	public Template getTemplate(String name, String encoding, Map<String, Class<?>> parameterTypes) throws IOException, ParseException {
		return getTemplate(name, null, encoding, parameterTypes);
	}

	/**
	 * Get template.
	 * 
	 * @see #getEngine()
	 * @param name - template name
	 * @param locale - template locale
	 * @return template instance
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed
	 */
	public Template getTemplate(String name, Locale locale, Map<String, Class<?>> parameterTypes) throws IOException, ParseException {
		return getTemplate(name, locale, null, parameterTypes);
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
	public abstract Template getTemplate(String name, Locale locale, String encoding, Map<String, Class<?>> parameterTypes) throws IOException, ParseException;

	/**
	 * Parse string template.
	 * 
	 * @see #getEngine()
	 * @param source - template source
	 * @return template instance
	 * @throws ParseException - If the template cannot be parsed
	 */
	public Template parseTemplate(String source) throws ParseException {
		return parseTemplate(source, null);
	}

	/**
	 * Parse string template.
	 * 
	 * @see #getEngine()
	 * @param source - template source
	 * @return template instance
	 * @throws ParseException - If the template cannot be parsed
	 */
	public abstract Template parseTemplate(String source, Map<String, Class<?>> parameterTypes) throws ParseException;

}