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
package httl.internal.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 该BeanFactory是一个仿Spring的轻量级“IoC+AOP”容器。
 * 
 * IoC: 基于Setter递归注入属性，如：
 * 
 * <pre>
 * public class DefaultEngine {
 * 
 *	 // 配置：intput.encoding=UTF-8
 *	 // 将点号转为大写驼峰命名注入。
 *	 public void setInputEncoding(String inputEncoding) {
 *		 this.inputEncoding = inputEncoding;
 *	 }
 * 
 *	 // 配置：parser=httl.spi.parsers.CommentParser
 *	 // 通过无参构造函数实例化，对象会被单例缓存，
 *	 // 其中，Parser对象的Setter同样会被递归注入属性
 *	 public void setParser(Parser parser) {
 *		 this.parser = parser;
 *	 }
 * 
 *	 // 配置 packages=java.util,httl.internal.util
 *   // 可以注入数组，如String[]或Loader[]，多个值用逗号分隔。
 *	 public void setPackages(String[] packages) {
 *		 this.packages = packages;
 *	 }
 * 
 *	 // 所有属性注入完成后，会执行init()方法。
 *	 public void init() {
 *	 }
 * 
 * }
 * </pre>
 * 
 * AOP: 基于同类型构造函数进行层层包装，如：
 * 
 * <pre>
 * public class MyParserWrapper implements Parser {
 * 
 *	 private final Parser parser;
 * 
 *	 // 配置：parser^=com.my.MyParserWrapper
 *	 // 注意配置的等号(=)前面有个尖号(^)，多个包装类用逗号分隔，
 *	 // 基于同类型构造函数，注入原始Parser，
 *	 // 如果有多个Wrapper，这里也可能注入的是另一个Wrapper。
 *	 public MyParserWrapper(Parser parser) {
 *		 this.parser = parser;
 *	 }
 * 
 *	 // Wrapper类可以代理原始类的所有行为
 *	 public Template parse(Resource resource) throws IOException, ParseException {
 *		 return new MyTemplateWrapper(parser.parse(resource));
 *	 }
 * 
 * }
 * </pre>
 * 
 * DEP: 基于注解\@Reqiured和\@Optional判断必需和可选依赖，如：
 * 
 * <pre>
 * public class MultiInterceptor {
 * 
 *	 // 当必需属性interceptors实例为空时，不初始化当前类，以null返回。
 *	 \@Reqiured
 *   public void setInterceptors(Interceptor[] interceptors) {
 *		 this.interceptors = interceptors;
 *	 }
 * 
 *	 // 当所有可选属性aaa和bbb同时为空时，不初始化当前类，以null返回。
 *	 \@Optional
 *   public void setAaa(String aaa) {
 *		 this.aaa = aaa;
 *	 }
 *   \@Optional
 *   public void setBbb(String bbb) {
 *		 this.bbb = bbb;
 *	 }
 * 
 * }
 * </pre>
 * 
 * @author liangfei
 */
public class BeanFactory {

	private static final String SET_METHOD = "set";

	private static final String SET_PROPERTIES_METHOD = "setProperties";

	private static final String INIT_METHOD = "init";

	private static final String INITED_METHOD = "inited";

	private static final String WRAPPER_KEY_SUFFIX = "^";

	private static final Object NULL = new Object();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T createBean(Class<T> beanClass, Properties properties) {
		Map<String, Object> caches = new HashMap<String, Object>();
		Map<String, Object> instances = new HashMap<String, Object>();
		instances.putAll((Map) properties);
		List<Object> inits = new ArrayList<Object>();
		String name = beanClass.getSimpleName();
		String property = name.substring(0, 1).toLowerCase() + name.substring(1);
		String key = StringUtils.splitCamelName(property, ".");
		String value = properties.getProperty(key);
		T instance = getInstance(property, key, value, beanClass, properties, caches, instances, inits);
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
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				e = e.getCause();
			}
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			if (e instanceof Error) {
				throw (Error) e;
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
		return instance;
	}

	private static boolean injectInstance(Object object, Properties properties, String parent, Map<String, Object> caches, Map<String, Object> instances, List<Object> inits) {
		try {
			if (! inits.contains(object)) {
				inits.add(object);
			}
			boolean useOptional = false;
			boolean hasOptional = false;
			Method[] methods = object.getClass().getMethods();
			for (Method method : methods) {
				String name = method.getName();
				if (name.length() > 3 && name.startsWith(SET_METHOD)
						&& Modifier.isPublic(method.getModifiers())
						&& !Modifier.isStatic(method.getModifiers())
						&& method.getParameterTypes().length == 1) {
					Class<?> parameterType = method.getParameterTypes()[0];
					if (Map.class.equals(parameterType) && SET_PROPERTIES_METHOD.equals(name)) {
						method.invoke(object, new Object[] { instances });
					} else {
						String property = name.substring(3, 4).toLowerCase() + name.substring(4);
						String key = StringUtils.splitCamelName(property, ".");
						String value = null;
						if (StringUtils.isNotEmpty(parent)) {
							value = properties.getProperty(parent + "." + key);
						}
						if (StringUtils.isEmpty(value)) {
							value = properties.getProperty(key);
						}
						Object obj = null;
						if (value != null && value.trim().length() > 0) {
							value = value.trim();
							if (parameterType.isArray()) {
								Class<?> componentType = parameterType.getComponentType();
								String[] values = StringUtils.splitByComma(value);
								Object[] objs = (Object[]) Array.newInstance(componentType, values.length);
								int idx = 0;
								for (int i = 0; i < values.length; i++) {
									Object o = parseValue(property, key, values[i], componentType, properties, caches, instances, inits);
									if (o != null) {
										objs[idx ++] = o;
									}
								}
								if (idx == 0) {
									obj = null;
								} else if (idx < values.length) {
									obj = (Object[]) Array.newInstance(componentType, idx);
									System.arraycopy(objs, 0, obj, 0, idx);
								} else {
									obj = objs;
								}
								if (obj != null && ! componentType.isPrimitive() 
										&& componentType != String.class
										&& componentType != Boolean.class
										&& componentType != Character.class
										&& ! Number.class.isAssignableFrom(componentType)) {
									instances.put(property, obj);
								}
							} else {
								obj = parseValue(property, key, value, parameterType, properties, caches, instances, inits);
							}
						}
						if (obj != null) {
							method.invoke(object, new Object[] { obj });
							if (method.isAnnotationPresent(Optional.class)) {
								useOptional = true;
								hasOptional = true;
							}
						} else {
							if (method.isAnnotationPresent(Reqiured.class)) {
								return false;
							}
							if (method.isAnnotationPresent(Optional.class)) {
								useOptional = true;
							}
						}
					}
				}
			}
			if (useOptional && ! hasOptional) {
				return false;
			}
			try {
				Method method = object.getClass().getMethod(INIT_METHOD, new Class<?>[0]);
				if (Modifier.isPublic(method.getModifiers())
						&& ! Modifier.isStatic(method.getModifiers())) {
					method.invoke(object, new Object[0]);
				}
			} catch (NoSuchMethodException e) {
			}
			return true;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getInstance(String property, String key, String value, Class<T> type, Properties properties, Map<String, Object> caches, Map<String, Object> instances, List<Object> inits) {
		if (StringUtils.isEmpty(value) || "null".equals(value)) {
			return null;
		}
		Class<?> cls = ClassUtils.forName(value);
		if (! type.isAssignableFrom(cls)) {
			throw new IllegalStateException("The class " + cls.getName() + " unimplemented interface " + type.getName() + " in config " + key);
		}
		try {
			String index = key + "=" + value;
			Object instance = caches.get(index);
			if (instance == null) {
				try {
					instance = cls.getConstructor(new Class<?>[0]).newInstance();
					caches.put(index, instance);
					boolean valid = injectInstance(instance, properties, key, caches, instances, inits);
					if (! valid) {
						instance = NULL;
						caches.put(index, instance);
					} else {
						if (cls.getInterfaces().length > 0) {
							Class<?> face = cls.getInterfaces()[0];
							String insert = properties.getProperty(key + WRAPPER_KEY_SUFFIX);
							if (insert != null && insert.trim().length() > 0) {
								insert = insert.trim();
								String[] wrappers = StringUtils.splitByComma(insert);
								for (String wrapper : wrappers) {
									Class<?> wrapperClass = ClassUtils.forName(wrapper);
									if (! face.isAssignableFrom(wrapperClass)) {
										throw new IllegalStateException("The wrapper class " + wrapperClass.getName() + " must be implements interface " + face.getName() + ", config key: " + key + WRAPPER_KEY_SUFFIX);
									}
									Constructor<?> constructor = wrapperClass.getConstructor(new Class<?>[] { face });
									if (Modifier.isPublic(constructor.getModifiers())) {
										Object wrapperInstance = constructor.newInstance(new Object[] {instance});
										boolean wrapperValid = injectInstance(wrapperInstance, properties, key, caches, instances, inits);
										if (wrapperValid) {
											instance = wrapperInstance;
											caches.put(index, instance);
										}
									}
								}
							}
						}
					}
				} catch (NoSuchMethodException e) {
					if (type.isAssignableFrom(Class.class)) {
						instance = cls;
						caches.put(index, instance);
					} else {
						throw e;
					}
				}
			}
			if (instance == NULL) {
				return null;
			}
			instances.put(property + "=" + value, instance);
			instances.put(property, instance);
			return (T) instance;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private static Object parseValue(String property, String key, String value, Class<?> parameterType, Properties properties, Map<String, Object> caches, Map<String, Object> instances, List<Object> inits) {
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
			return getInstance(property, key, value, parameterType, properties, caches, instances, inits);
		}
	}

}