/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"){} you may not use this file except in compliance with
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
package httl;

import httl.util.BeanFactory;
import httl.util.ConfigUtils;
import httl.util.VolatileReference;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Engine. (API, Singleton, ThreadSafe)
 * 
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

    // The engine singletons cache
    private static final ConcurrentMap<String, VolatileReference<Engine>> ENGINES = new ConcurrentHashMap<String, VolatileReference<Engine>>();

    /**
     * Get template engine singleton.
     * 
     * @return template engine
     */
    public static Engine getEngine() {
        return getEngine(HTTL_PROPERTIES, new Properties());
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
        return getEngine(HTTL_PROPERTIES, configProperties);
    }

    /**
     * Get template engine singleton.
     * 
     * @param configPath - config path
     * @param configProperties - config properties
     * @return template engine
     */
    public static Engine getEngine(String configPath, Properties configProperties) {
        if (configPath == null || configPath.length() == 0) {
            throw new IllegalArgumentException("httl config path == null");
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
                    properties.setProperty(BeanFactory.getPropertyKey(Engine.class, "name"), configPath);
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
     * Get config value.
     * 
     * @see #getEngine()
     * @param key - config key
     * @return config value
     */
    public abstract String getProperty(String key);

    /**
     * Get config boolean value.
     * 
     * @see #getEngine()
     * @param key - config key
     * @param defaultValue - default boolean value
     * @return config boolean value
     */
    public abstract <T> T getProperty(String key, Class<T> cls);

    /**
     * Get config value.
     * 
     * @see #getEngine()
     * @param key - config key
     * @param defaultValue - default value
     * @return config value
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null || value.length() == 0 ? defaultValue : value;
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
        String value = getProperty(key);
        return value == null || value.length() == 0 ? defaultValue : Integer.parseInt(value);
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
        String value = getProperty(key);
        return value == null || value.length() == 0 ? defaultValue : "true".equalsIgnoreCase(value);
    }

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
     * @throws ParseException
     */
    public Resource getResource(String name) throws IOException {
        return getResource(name, null);
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
    public abstract Resource getResource(String name, String encoding) throws IOException;

    /**
     * Add literal template resource.
     * 
     * @see #getEngine()
     * @param name - template name
     * @param source - template source
     */
    public abstract void addResource(String name, String source);

    /**
     * Remove literal template resource.
     * 
     * @see #getEngine()
     * @param name - template name
     */
    public abstract void removeResource(String name);

    /**
     * Tests whether the resource denoted by this abstract pathname exists.
     * 
     * @see #getEngine()
     * @param name - template name
     * @return exists
     */
    public abstract boolean hasResource(String name);

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
        return getTemplate(name, null);
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
    public abstract Template getTemplate(String name, String encoding) throws IOException, ParseException;

}
