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

import httl.util.ClassUtils;
import httl.util.ConfigUtils;
import httl.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Engine. (API, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class Engine {
 
	private static final String DEFAULT_PATH = "httl.properties";

	private static final String ENGINE_KEY= "engine";

	private static final String PLUS = "+";

	private static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*\\,\\s*");
    
    private static final ConcurrentMap<String, ReentrantLock> ENGINE_LOCKS = new ConcurrentHashMap<String, ReentrantLock>();

	private static final ConcurrentMap<String, Engine> ENGINES = new ConcurrentHashMap<String, Engine>();

    private final Properties config = new Properties();
    
    private final ConcurrentMap<String, Object> instances = new ConcurrentHashMap<String, Object>();
    
    /**
     * Get template engine singleton.
     * 
     * @return template engine.
     */
	public static Engine getEngine() {
		return getEngine(DEFAULT_PATH);
	}

    /**
     * Get template engine singleton.
     * 
     * @param configPath config path.
     * @return template engine.
     */
    public static Engine getEngine(String configPath) {
        return getEngine(configPath, null);
    }

	/**
     * Get template engine singleton.
     * 
     * @param configProperties config properties.
     * @return template engine.
     */
	public static Engine getEngine(Properties configProperties) {
		return getEngine(DEFAULT_PATH, configProperties);
	}

	/**
     * Get template engine singleton.
     * 
     * @param configPath config path.
     * @param configProperties config map.
     * @return template engine.
     */
    public static Engine getEngine(String configPath, Properties configProperties) {
		if (configPath == null || configPath.length() == 0) {
			throw new IllegalArgumentException("httl config path == null");
		}
		ReentrantLock lock = ENGINE_LOCKS.get(configPath);
        if (lock == null) {
            ENGINE_LOCKS.putIfAbsent(configPath, new ReentrantLock());
            lock= ENGINE_LOCKS.get(configPath);
        }
        assert(lock != null);
        Engine engine = ENGINES.get(configPath);
        if (engine == null) { // double check
            lock.lock();
            try {
                engine = ENGINES.get(configPath);
                if (engine == null) { // double check
                	Properties config = mergeConfig(configPath, configProperties);
                	String engineClassName = config.getProperty(ENGINE_KEY);
                	Class<?> engineClass = ClassUtils.forName(engineClassName);
            		try {
						engine = (Engine) engineClass.newInstance();
						engine.config(config);
					} catch (Exception e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
                    ENGINES.put(configPath, engine);
                }
            } finally {
                lock.unlock();
            }
        }
        assert(engine != null);
        return engine;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Properties mergeConfig(String configPath, Properties configProperties) {
		Properties defaultConfig = ConfigUtils.loadProperties("httl-default.properties", false);
		Properties config = ConfigUtils.loadProperties(configPath, configProperties != null || DEFAULT_PATH.equals(configPath));
        if (configProperties != null) {
        	config.putAll(configProperties);
        }
        for (Map.Entry<String, String> entry : new HashMap<String, String>((Map) config).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.endsWith(PLUS)) {
                if (value != null && value.length() > 0) {
                    String k = key.substring(0, key.length() - PLUS.length());
                    String v = config.getProperty(k);
                    if (v != null && v.length() > 0) {
                        v += "," + value;
                    } else {
                        v = value;
                    }
                    config.setProperty(k, v);
                }
                config.remove(key);
            }
            if (value != null && value.startsWith(PLUS)) {
                value = value.substring(PLUS.length());
                String v = defaultConfig.getProperty(key);
                if (v != null && v.length() > 0) {
                    v += "," + value;
                } else {
                    v = value;
                }
                config.setProperty(key, v);
            }
        }
        defaultConfig.putAll(config);
        return defaultConfig;
	}

	protected Engine() {
	}

	/**
	 * Get config.
	 * 
	 * @return config.
	 */
	public Properties getConfig() {
		return config;
	}

	/**
	 * Get config value.
	 * 
	 * @param key config key.
	 * @return config value.
	 */
	public String getConfig(String key) {
		String value = config.getProperty(key);
		return value == null ? null : value.trim();
	}

	/**
	 * Get config value.
	 * 
	 * @param key config key.
	 * @param defaultValue default value.
	 * @return config value
	 */
	public String getConfig(String key, String defaultValue) {
		String value = getConfig(key);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * Get config values.
	 * 
	 * @param key config key.
	 * @param defaultValue default value.
	 * @return config value
	 */
	public String[] getConfig(String key, String[] defaultValue) {
		String value = getConfig(key);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return COMMA_SPLIT_PATTERN.split(value);
	}

	/**
	 * Get config extension value.
	 * 
	 * @param key config key.
	 * @param type extension type.
	 * @return config extension.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfig(String key, Class<T> type) {
		String value = getConfig(key);
		if (value == null || value.length() == 0 || "null".equals(value)) {
			return null;
		}
		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			String[] values = COMMA_SPLIT_PATTERN.split(value);
			Object results = Array.newInstance(componentType, values.length);
			for (int i = 0; i < values.length; i ++) {
				Array.set(results, i, cache(values[i], componentType));
			}
			return (T) results;
		} else {
			return cache(getConfig(key), type);
		}
	}

	/**
	 * Get config extension value.
	 * 
	 * @param key config key.
	 * @param type extension type.
	 * @param defaultValue default value.
	 * @return config extension.
	 */
	public <T> T getConfig(String key, Class<T> type, T defaultValue) {
		T value = getConfig(key, type);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * Get config int value.
	 * 
	 * @param key config key.
	 * @param defaultValue default value.
	 * @return config value
	 */
	public int getConfig(String key, int defaultValue) {
		String value = getConfig(key);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}

	/**
	 * Get config boolean value.
	 * 
	 * @param key config key.
	 * @param defaultValue default value.
	 * @return config value
	 */
	public boolean getConfig(String key, boolean defaultValue) {
		String value = getConfig(key);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

    /**
     * Get expression.
     * 
     * @param source
     * @return expression.
     * @throws ParseException
     */
    public Expression getExpression(String source) throws ParseException {
        return getExpression(source, null, 0);
    }

    /**
     * Get expression.
     * 
     * @param source
     * @param parameterTypes
     * @return expression.
     * @throws ParseException
     */
    public Expression getExpression(String source, Map<String, Class<?>> parameterTypes) throws ParseException {
        return getExpression(source, parameterTypes, 0);
    }

    /**
     * Get expression.
     * 
     * @param source
     * @param parameterTypes
     * @return template resource.
     * @throws ParseException
     */
    public abstract Expression getExpression(String source, Map<String, Class<?>> parameterTypes, int offset) throws ParseException;

    /**
     * Get template resource.
     * 
     * @param name
     * @return template resource.
     * @throws IOException
     * @throws ParseException
     */
    public Resource getResource(String name) throws IOException {
        return getResource(name, null);
    }

    /**
     * Get template resource.
     * 
     * @param name
     * @param encoding
     * @return template resource.
     * @throws IOException
     * @throws ParseException
     */
    public abstract Resource getResource(String name, String encoding) throws IOException;

	/**
	 * Get template.
	 * 
	 * @param name
	 * @return template
	 * @throws IOException
	 * @throws ParseException
	 */
	public Template getTemplate(String name) throws IOException, ParseException {
		return getTemplate(name, null);
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
    public abstract Template getTemplate(String name, String encoding) throws IOException, ParseException;

	private void config(Properties properties) {
		config.putAll(properties);
        init(this);
	}

	@SuppressWarnings("unchecked")
	private <T> T cache(String value, Class<T> type) {
		if (value == null || value.length() == 0 || "null".equals(value)) {
			return null;
		}
		Class<?> cls = ClassUtils.forName(value);
		if (! type.isAssignableFrom(cls)) {
			throw new IllegalStateException("The class + " + value + " unimplemented interface " + cls.getName() + ".");
		}
		try {
			Object instance = instances.get(value);
			if (instance == null) {
				Object newInstance = cls.newInstance();
				instances.putIfAbsent(value, newInstance);
				instance = instances.get(value);
				if (instance == newInstance) {
					init(instance);
				}
			}
			return (T) instance;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

    private void init(Object object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            try {
                String name = method.getName();
                if (name.length() > 3 && name.startsWith("set")
                        && Modifier.isPublic(method.getModifiers())
                        && ! Modifier.isStatic(method.getModifiers())
                        && method.getParameterTypes().length == 1) {
                	Class<?> parameterType = method.getParameterTypes()[0];
                	if ("setEngine".equals(name) && Engine.class.isAssignableFrom(parameterType)) {
                		method.invoke(object, new Object[] { this });
                		continue;
                	}
                	if ("setConfig".equals(name) && Properties.class.isAssignableFrom(parameterType)) {
                		method.invoke(object, new Object[] { config });
                		continue;
                	}
                	String key = StringUtils.splitCamelName(name.substring(3), ".");
                	String value = getConfig(key);
                    if (value != null && value.length() > 0) {
                    	Object obj;
                    	if (parameterType.isArray()) {
                    		Class<?> componentType = parameterType.getComponentType();
                    		String[] values = COMMA_SPLIT_PATTERN.split(value);
                    		obj = Array.newInstance(componentType, values.length);
                    		for (int i = 0; i < values.length; i ++) {
                    			Array.set(obj, i, parseValue(values[i], componentType));
                    		}
                    	} else {
                    		obj = parseValue(value, parameterType);
                    	}
                    	method.invoke(object, new Object[] { obj });
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private Object parseValue(String value, Class<?> parameterType) {
    	if (parameterType == String.class) {
    		return value;
        } else if (parameterType == char.class) {
        	return value.charAt(0);
        } else if (parameterType == int.class) {
    		return Integer.valueOf(value);
        } else if (parameterType == long.class) {
    		return Long.valueOf(value);
        } else if (parameterType == float.class) {
    		return Float.valueOf(value);
        } else if (parameterType == double.class) {
    		return Double.valueOf(value);
        } else if (parameterType == short.class) {
    		return Short.valueOf(value);
        } else if (parameterType == byte.class) {
    		return Byte.valueOf(value);
        } else if (parameterType == boolean.class) {
            return Boolean.valueOf(value);
        } else if (parameterType == Class.class) {
            return ClassUtils.forName(value);
        } else {
        	return cache(value, parameterType);
        }
    }

}
