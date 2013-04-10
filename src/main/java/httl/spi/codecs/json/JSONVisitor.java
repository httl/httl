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
package httl.spi.codecs.json;

import httl.internal.util.ClassUtils;
import httl.internal.util.Stack;
import httl.internal.util.StringUtils;
import httl.spi.Converter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JSON to Object visitor.
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
class JSONVisitor {

	public static final String CLASS_PROPERTY = "class";

	public static final boolean[] EMPTY_BOOL_ARRAY = new boolean[0];

	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	public static final char[] EMPTY_CHAR_ARRAY = new char[0];

	public static final short[] EMPTY_SHORT_ARRAY = new short[0];

	public static final int[] EMPTY_INT_ARRAY = new int[0];

	public static final long[] EMPTY_LONG_ARRAY = new long[0];

	public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	private Class<?>[] currentTypes;

	private Class<?> currentType = Object[].class;

	private Object currentValue;

	private Map<String, Object> currentWrapper;

	private JSONValue jsonValue;

	private Converter<Object, Map<String, Object>> converter;

	private Stack<Object> stack = new Stack<Object>();

	JSONVisitor(Class<?> type, JSONValue jc,
			Converter<Object, Map<String, Object>> mc) {
		currentType = type;
		jsonValue = jc;
		converter = mc;
	}

	JSONVisitor(Class<?>[] types, JSONValue jc) {
		currentTypes = types;
		jsonValue = jc;
	}

	public void begin() {
	}

	public Object end(Object obj, boolean isValue) throws ParseException {
		stack.clear();
		try {
			return jsonValue.readValue(currentType, obj);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void objectBegin() throws ParseException {
		stack.push(currentValue);
		stack.push(currentType);
		stack.push(currentWrapper);

		if (currentType == Object.class || Map.class.isAssignableFrom(currentType)) {
			if (!currentType.isInterface() && currentType != Object.class) {
				try {
					currentValue = currentType.newInstance();
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			} else if (currentType == ConcurrentMap.class) {
				currentValue = new ConcurrentHashMap<String, Object>();
			} else {
				currentValue = new HashMap<String, Object>();
			}
			currentWrapper = null;
		} else {
			try {
				currentValue = currentType.newInstance();
				currentWrapper = converter.convert(currentValue, null);
			} catch (IllegalAccessException e) {
				throw new ParseException(StringUtils.toString(e), 0);
			} catch (InstantiationException e) {
				throw new ParseException(StringUtils.toString(e), 0);
			} catch (IOException e) {
				throw new ParseException(StringUtils.toString(e), 0);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Object objectEnd(int count) {
		Object ret = currentValue;
		currentWrapper = (Map<String, Object>) stack.pop();
		currentType = (Class<?>) stack.pop();
		currentValue = stack.pop();
		if (ret instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) ret;
			Object obj = map.get(CLASS_PROPERTY);
			if (obj instanceof String) {
				String className = (String) obj;
				Class<?> cls = ClassUtils.forName(className);
				if (! cls.isInterface() && ! cls.isPrimitive()
						&& ! cls.isAssignableFrom(ret.getClass())) {
					try {
						Object value = cls.newInstance();
						if (value instanceof Map) {
							((Map<String, Object>) value).putAll((Map<String, Object>) ret);
						} else {
							ClassUtils.setProperties(value, (Map<String, Object>) ret);
						}
						ret = value;
					} catch (Exception e) {
					}
				}
			}
		}
		return ret;
	}

	public void objectItem(String name) {
		stack.push(name); // push name.
		Class<?> v = currentWrapper == null ? null : (Class<?>) currentWrapper.get(name + "." + CLASS_PROPERTY);
		currentType = (v == null ? Object.class : v);
	}

	@SuppressWarnings("unchecked")
	public void objectItemValue(Object obj, boolean isValue)
			throws ParseException {
		String name = (String) stack.pop(); // pop name.
		if (currentWrapper == null) {
			((Map<String, Object>) currentValue).put(name, obj);
		} else {
			if (currentType != null) {
				if (isValue && obj != null) {
					try {
						obj = jsonValue.readValue(currentType, obj);
					} catch (IOException e) {
						throw new ParseException(StringUtils.toString(e), 0);
					}
				}
				if (currentValue instanceof Throwable && "message".equals(name)) {
					try {
						Field field = Throwable.class
								.getDeclaredField("detailMessage");
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						field.set(currentValue, obj);
					} catch (NoSuchFieldException e) {
						throw new ParseException(StringUtils.toString(e), 0);
					} catch (IllegalAccessException e) {
						throw new ParseException(StringUtils.toString(e), 0);
					}
				} else if (! CLASS_PROPERTY.equals(name) || currentValue instanceof Map) {
					currentWrapper.put(name, obj);
				}
			}
		}
	}

	public void arrayBegin() throws ParseException {
		stack.push(currentType);

		if (currentType.isArray())
			currentType = currentType.getComponentType();
		else if (currentType == Object.class
				|| Collection.class.isAssignableFrom(currentType))
			currentType = Object.class;
		else
			throw new ParseException(
					"Convert error, can not load json array data into class ["
							+ currentType.getName() + "].", 0);
	}

	@SuppressWarnings("unchecked")
	public Object arrayEnd(int count) throws ParseException {
		Object ret;
		currentType = (Class<?>) stack.get(-1 - count);

		if (currentType.isArray()) {
			ret = toArray(currentType.getComponentType(), stack, count);
		} else {
			Collection<Object> items;
			if (currentType == Object.class
					|| Collection.class.isAssignableFrom(currentType)) {
				if (!currentType.isInterface() && currentType != Object.class) {
					try {
						items = (Collection<Object>) currentType.newInstance();
					} catch (Exception e) {
						throw new IllegalStateException(e.getMessage(), e);
					}
				} else if (currentType.isAssignableFrom(ArrayList.class)) { // List
					items = new ArrayList<Object>(count);
				} else if (currentType.isAssignableFrom(HashSet.class)) { // Set
					items = new HashSet<Object>(count);
				} else if (currentType.isAssignableFrom(LinkedList.class)) { // Queue
					items = new LinkedList<Object>();
				} else { // Other
					items = new ArrayList<Object>(count);
				}
			} else {
				throw new ParseException(
						"Convert error, can not load json array data into class ["
								+ currentType.getName() + "].", 0);
			}
			for (int i = 0; i < count; i++)
				items.add(stack.remove(i - count));
			ret = items;
		}
		stack.pop();
		return ret;
	}

	public void arrayItem(int index) throws ParseException {
		if (currentTypes != null && stack.size() == index + 1) {
			if (index < currentTypes.length)
				currentType = currentTypes[index];
			else
				throw new ParseException("Can not load json array data into ["
						+ name(currentTypes) + "].", 0);
		}
	}

	public void arrayItemValue(int index, Object obj, boolean isValue)
			throws ParseException {
		if (isValue && obj != null) {
			try {
				obj = jsonValue.readValue(currentType, obj);
			} catch (IOException e) {
				throw new ParseException(e.getMessage(), 0);
			}
		}

		stack.push(obj);
	}

	private static Object toArray(Class<?> c, Stack<Object> list, int len)
			throws ParseException {
		if (c == String.class) {
			if (len == 0) {
				return EMPTY_STRING_ARRAY;
			} else {
				Object o;
				String ss[] = new String[len];
				for (int i = len - 1; i >= 0; i--) {
					o = list.pop();
					ss[i] = (o == null ? null : o.toString());
				}
				return ss;
			}
		}
		if (c == boolean.class) {
			if (len == 0)
				return EMPTY_BOOL_ARRAY;
			Object o;
			boolean[] ret = new boolean[len];
			for (int i = len - 1; i >= 0; i--) {
				o = list.pop();
				if (o instanceof Boolean)
					ret[i] = ((Boolean) o).booleanValue();
			}
			return ret;
		}
		if (c == int.class) {
			if (len == 0)
				return EMPTY_INT_ARRAY;
			Object o;
			int[] ret = new int[len];
			for (int i = len - 1; i >= 0; i--) {
				o = list.pop();
				if (o instanceof Number)
					ret[i] = ((Number) o).intValue();
			}
			return ret;
		}
		if (c == long.class) {
			if (len == 0)
				return EMPTY_LONG_ARRAY;
			Object o;
			long[] ret = new long[len];
			for (int i = len - 1; i >= 0; i--) {
				o = list.pop();
				if (o instanceof Number)
					ret[i] = ((Number) o).longValue();
			}
			return ret;
		}
		if (c == float.class) {
			if (len == 0)
				return EMPTY_FLOAT_ARRAY;
			Object o;
			float[] ret = new float[len];
			for (int i = len - 1; i >= 0; i--) {
				o = list.pop();
				if (o instanceof Number)
					ret[i] = ((Number) o).floatValue();
			}
			return ret;
		}
		if (c == double.class) {
			if (len == 0)
				return EMPTY_DOUBLE_ARRAY;
			Object o;
			double[] ret = new double[len];
			for (int i = len - 1; i >= 0; i--) {
				o = list.pop();
				if (o instanceof Number)
					ret[i] = ((Number) o).doubleValue();
			}
			return ret;
		}
		if (c == byte.class) {
			if (len == 0)
				return EMPTY_BYTE_ARRAY;
			Object o;
			byte[] ret = new byte[len];
			for (int i = len - 1; i >= 0; i--) {
				o = list.pop();
				if (o instanceof Number)
					ret[i] = ((Number) o).byteValue();
			}
			return ret;
		}
		if (c == char.class) {
			if (len == 0)
				return EMPTY_CHAR_ARRAY;
			Object o;
			char[] ret = new char[len];
			for (int i = len - 1; i >= 0; i--) {
				o = list.pop();
				if (o instanceof Character)
					ret[i] = ((Character) o).charValue();
			}
			return ret;
		}
		if (c == short.class) {
			if (len == 0)
				return EMPTY_SHORT_ARRAY;
			Object o;
			short[] ret = new short[len];
			for (int i = len - 1; i >= 0; i--) {
				o = list.pop();
				if (o instanceof Number)
					ret[i] = ((Number) o).shortValue();
			}
			return ret;
		}

		Object ret = Array.newInstance(c, len);
		for (int i = len - 1; i >= 0; i--)
			Array.set(ret, i, list.pop());
		return ret;
	}

	private static String name(Class<?>[] types) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < types.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(types[i].getName());
		}
		return sb.toString();
	}
}