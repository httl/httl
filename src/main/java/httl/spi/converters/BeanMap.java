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
package httl.spi.converters;

import httl.spi.Compiler;
import httl.util.MapEntry;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * BeanMap (Tool, Prototype, NotThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class BeanMap implements Map<String, Object> {

	private static final ConcurrentMap<Class<?>, Class<?>> BEAN_WRAPPERS = new ConcurrentHashMap<Class<?>, Class<?>>();

	@SuppressWarnings("unchecked")
	public static Map<String, Object> convert(Object bean, Compiler compiler) {
		if (bean == null) {
			return null;
		}
		Class<?> beanClass = bean.getClass();
		Class<?> wrapperClass = BEAN_WRAPPERS.get(beanClass);
		if (wrapperClass == null) {
			try {
				wrapperClass = createWrapperClass(beanClass, compiler);
				Class<?> old = BEAN_WRAPPERS.putIfAbsent(beanClass, wrapperClass);
				if (old != null) {
					wrapperClass = old;
				}
			} catch (ParseException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		try {
			return (Map<String, Object>) wrapperClass.getConstructor(beanClass).newInstance(bean);
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static Class<?> createWrapperClass(Class<?> beanClass, Compiler compiler) throws ParseException {
		StringBuilder keys = new StringBuilder();
		StringBuilder gets = new StringBuilder();
		for (Method method : beanClass.getMethods()) {
			String name = method.getName();
			if ((name.length() > 3 && name.startsWith("get") 
					|| name.length() > 2 && name.startsWith("is"))
					&& Modifier.isPublic(method.getModifiers())
					&& method.getParameterTypes().length == 0
					&& method.getDeclaringClass() != Object.class) {
				int i = name.startsWith("get") ? 3 : 2;
				String key = name.substring(i, i + 1).toLowerCase() + name.substring(i + 1);
				if (keys.length() > 0) {
					keys.append(", ");
				}
				keys.append("\"" + key + "\"");
				if (gets.length() > 0) {
					gets.append("else ");
				}
				gets.append("if (\"" + key + "\".equals(key)) return bean." + name + "();\n");
			}
		}
		StringBuilder code = new StringBuilder();
		String className = BeanMap.class.getSimpleName() + "_" + beanClass.getCanonicalName().replace('.', '_');
		code.append("package " + BeanMap.class.getPackage().getName() + ";\n");
		code.append("public class " + className + " extends " + BeanMap.class.getName() + " {\n");
		code.append("private " + beanClass.getCanonicalName() + " bean;\n");
		code.append("public " + className + "(" + beanClass.getCanonicalName() + " bean) {\n");
		code.append("super(bean, new String[] {" + keys + "});\n");
		code.append("this.bean = bean;\n");
		code.append("}\n");
		code.append("public Object get(Object key) {\n");
		code.append(gets);
		code.append("return null;\n");
		code.append("}\n");
		code.append("}\n");
		return compiler.compile(code.toString());
	}

	private final Set<String> keySet;

	protected BeanMap(Object bean, String[] keys) {
		if (bean == null)
			throw new IllegalArgumentException("bean == null");
		if (keys == null)
			throw new IllegalArgumentException("bean keys == null");
		this.keySet = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(keys)));
	}

	public Set<String> keySet() {
		return keySet;
	}

	public Collection<Object> values() {
		return new BeanSet<Object>() {
			@Override
			protected Object getVaue(String key) {
				return get(key);
			}
		};
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return new BeanSet<Map.Entry<String, Object>>() {
			@Override
			protected Map.Entry<String, Object> getVaue(String key) {
				return new MapEntry<String, Object>(key, get(key));
			}
		};
	}

	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	public boolean containsValue(Object value) {
		return values().contains(value);
	}

	public boolean isEmpty() {
		return size() > 0;
	}

	public int size() {
		return keySet().size();
	}

	public Object put(String key, Object value) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map<? extends String, ? extends Object> map) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	private abstract class BeanSet<T> extends AbstractSet<T> {

		@Override
		public Iterator<T> iterator() {
			return new BeanIterator();
		}

		@Override
		public int size() {
			return BeanMap.this.size();
		}
		
		protected abstract T getVaue(String key);

		private class BeanIterator implements Iterator<T> {
			
			private Iterator<String> iterator = keySet().iterator();
			
			public boolean hasNext() {
				return iterator.hasNext();
			}

			public T next() {
				String key = iterator.next();
				return getVaue(key);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		}

	}

}