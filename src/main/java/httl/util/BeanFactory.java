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
package httl.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class BeanFactory {

    private static final String SET_METHOD = "set";

    private static final String INIT_METHOD = "init";

    private static final String INITED_METHOD = "inited";

    private static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*\\,\\s*");
    
    public static <T> T createBean(Class<T> beanClass, Properties properties) {
    	Map<String, Object> instances = new HashMap<String, Object>();
    	List<Object> inits = new ArrayList<Object>();
    	String key = StringUtils.splitCamelName(beanClass.getSimpleName(), ".");
    	String value = properties.getProperty(key);
    	T instance = getInstance(key, value, beanClass, properties, instances, inits);
    	try {
    		for (int i = inits.size() - 1; i >= 0; i --) { // reverse init order.
    			try {
    				Object object = inits.get(i);
	    			Method method = object.getClass().getMethod(INITED_METHOD, new Class<?>[0]);
	    			if (Modifier.isPublic(method.getModifiers())
	    					&& ! Modifier.isStatic(method.getModifiers())) {
	    				method.invoke(object, new Object[0]);
	    			}
    			} catch (NoSuchMethodException e) {
    			}
			}
    		inits.clear();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
    	return instance;
    }

    private static void injectInstance(Object object, Properties properties, Map<String, Object> instances, List<Object> inits) {
		try {
			if (! inits.contains(object)) {
				inits.add(object);
			}
			Method[] methods = object.getClass().getMethods();
			for (Method method : methods) {
				String name = method.getName();
				if (name.length() > 3 && name.startsWith(SET_METHOD)
						&& Modifier.isPublic(method.getModifiers())
						&& !Modifier.isStatic(method.getModifiers())
						&& method.getParameterTypes().length == 1) {
					Class<?> parameterType = method.getParameterTypes()[0];
					String key = StringUtils.splitCamelName(name.substring(3), ".");
					String value = properties.getProperty(key);
					if (value != null && value.length() > 0) {
						Object obj;
						if (parameterType.isArray()) {
							Class<?> componentType = parameterType.getComponentType();
							String[] values = COMMA_SPLIT_PATTERN.split(value);
							Object[] objs = (Object[]) Array.newInstance(componentType, values.length);
							for (int i = 0; i < values.length; i++) {
								objs[i] = parseValue(key, values[i], componentType, properties, instances, inits);
							}
							obj = objs;
						} else {
							obj = parseValue(key, value, parameterType, properties, instances, inits);
						}
						method.invoke(object, new Object[] { obj });
					}
				}
			}
			try {
    			Method method = object.getClass().getMethod(INIT_METHOD, new Class<?>[0]);
    			if (Modifier.isPublic(method.getModifiers())
    					&& ! Modifier.isStatic(method.getModifiers())) {
    				method.invoke(object, new Object[0]);
    			}
			} catch (NoSuchMethodException e) {
			}
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
    }

    @SuppressWarnings("unchecked")
    private static <T> T getInstance(String key, String value, Class<T> type, Properties properties, Map<String, Object> instances, List<Object> inits) {
        if (value == null || value.length() == 0 || "null".equals(value)) {
            return null;
        }
        Class<?> cls = ClassUtils.forName(value);
        if (! type.isAssignableFrom(cls)) {
            throw new IllegalStateException("The class + " + value + " unimplemented interface " + cls.getName() + ".");
        }
        try {
        	String index = key + "=" + value;
            Object instance = instances.get(index);
            if (instance == null) {
            	try {
	            	Constructor<?> constructor = cls.getConstructor(new Class<?>[0]);
	            	if (Modifier.isPublic(constructor.getModifiers())) {
	            		instance = constructor.newInstance();
	            	}
            	} catch (NoSuchMethodException e) {
            	}
            	if (instance == null) {
	            	if (type.isAssignableFrom(Class.class)) {
	            		instance = cls;
	            	} else {
	            		throw new NoSuchMethodException("No such public empty constructor in " + cls.getName());
	            	}
            	}
            	instances.put(index, instance);
            	injectInstance(instance, properties, instances, inits);
            }
            return (T) instance;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static Object parseValue(String key, String value, Class<?> parameterType, Properties properties, Map<String, Object> instances, List<Object> inits) {
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
            return getInstance(key, value, parameterType, properties, instances, inits);
        }
    }

}
