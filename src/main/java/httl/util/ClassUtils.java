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
package httl.util;

import httl.spi.sequences.IntegerSequence;
import httl.util.iterators.BooleanArrayIterator;
import httl.util.iterators.ByteArrayIterator;
import httl.util.iterators.CharArrayIterator;
import httl.util.iterators.DoubleArrayIterator;
import httl.util.iterators.EmptyIterator;
import httl.util.iterators.FloatArrayIterator;
import httl.util.iterators.IntArrayIterator;
import httl.util.iterators.LongArrayIterator;
import httl.util.iterators.ObjectArrayIterator;
import httl.util.iterators.ShortArrayIterator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ClassUtils. (Tool, Static, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ClassUtils {
    
    public static final String CLASS_EXTENSION = ".class";

    public static final String JAVA_EXTENSION = ".java";
    
    private static final ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<String, Class<?>>();

    private static final ConcurrentMap<Class<?>, Map<String, Method>> GETTER_CACHE = new ConcurrentHashMap<Class<?>, Map<String, Method>>();

    public static Object newInstance(String name) {
        try {
            return forName(name).newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    public static Class<?> forName(String[] packages, String className)  {
        try {
            return _forName(className);
        } catch (ClassNotFoundException e) {
        	// import class
    		if (! className.contains(".")) {
    			if (packages != null && packages.length > 0) {
                    for (String pkg : packages) {
                        try {
                            return _forName(pkg + "." + className);
                        } catch (ClassNotFoundException e2) {
                        }
                    }
                }
    			try {
                    return _forName("java.lang." + className);
                } catch (ClassNotFoundException e2) {
                }
    		} else {
	    		// inner class
	    		int i = className.lastIndexOf('.');
	        	if (i > 0 && i < className.length() - 1) {
	        		try {
	                    return _forName(className.substring(0, i) + "$" + className.substring(i + 1));
	                } catch (ClassNotFoundException e2) {
	                }
	        	}
    		}
        	throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    public static Class<?> forName(String className) {
        try {
            return _forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    private static Class<?> _forName(String name) throws ClassNotFoundException {
		if (name == null || name.length() == 0)
			return null;
		int index = name.indexOf('[');
		if (index > 0) {
			int i = (name.length() - index) / 2;
			name = name.substring(0, index);
			StringBuilder sb = new StringBuilder();
			while (i-- > 0)
				sb.append("["); // int[][]
			if ("void".equals(name))
				sb.append("V");
			else if ("boolean".equals(name))
				sb.append("Z");
			else if ("byte".equals(name))
				sb.append("B");
			else if ("char".equals(name))
				sb.append("C");
			else if ("double".equals(name))
				sb.append("D");
			else if ("float".equals(name))
				sb.append("F");
			else if ("int".equals(name))
				sb.append("I");
			else if ("long".equals(name))
				sb.append("J");
			else if ("short".equals(name))
				sb.append("S");
			else
				sb.append('L').append(name).append(';');
			name = sb.toString();
		} else {
			if ("void".equals(name))
				return void.class;
			else if ("boolean".equals(name))
				return boolean.class;
			else if ("byte".equals(name))
				return byte.class;
			else if ("char".equals(name))
				return char.class;
			else if ("double".equals(name))
				return double.class;
			else if ("float".equals(name))
				return float.class;
			else if ("int".equals(name))
				return int.class;
			else if ("long".equals(name))
				return long.class;
			else if ("short".equals(name))
				return short.class;
			else if ("Void".equals(name))
				return Void.class;
			else if ("Boolean".equals(name))
				return Boolean.class;
			else if ("Byte".equals(name))
				return Byte.class;
			else if ("Char".equals(name))
				return Character.class;
			else if ("Double".equals(name))
				return Double.class;
			else if ("Float".equals(name))
				return Float.class;
			else if ("Integer".equals(name))
				return Integer.class;
			else if ("Long".equals(name))
				return Long.class;
			else if ("Short".equals(name))
				return Short.class;
			else if ("String".equals(name))
				return String.class;
		}
		Class<?> clazz = CLASS_CACHE.get(name);
		if (clazz == null) {
			clazz = Class.forName(name, true, Thread.currentThread()
					.getContextClassLoader());
			CLASS_CACHE.put(name, clazz);
		}
		return clazz;
    }
    
    public static Class<?> getBoxedClass(Class<?> type) {
        if (type == boolean.class) {
            return Boolean.class; 
        } else if (type == char.class) {
            return Character.class; 
        } else if (type == byte.class) {
            return Byte.class; 
        } else if (type == short.class) {
            return Short.class; 
        } else if (type == int.class) {
            return Integer.class; 
        } else if (type == long.class) {
            return Long.class; 
        } else if (type == float.class) {
            return Float.class; 
        } else if (type == double.class) {
            return Double.class; 
        } else {
            return type;
        }
    }
    
    public static Boolean boxed(boolean v) {
        return Boolean.valueOf(v);
    }

    public static Character boxed(char v) {
        return Character.valueOf(v);
    }

    public static Byte boxed(byte v) {
        return Byte.valueOf(v);
    }

    public static Short boxed(short v) {
        return Short.valueOf(v);
    }

    public static Integer boxed(int v) {
        return Integer.valueOf(v);
    }

    public static Long boxed(long v) {
        return Long.valueOf(v);
    }

    public static Float boxed(float v) {
        return Float.valueOf(v);
    }

    public static Double boxed(double v) {
        return Double.valueOf(v);
    }
    
    public static Object boxed(Object v) {
        return v;
    }
    
    public static boolean unboxed(Boolean v) {
        return v == null ? false : v.booleanValue();
    }

    public static char unboxed(Character v) {
        return v == null ? '\0' : v.charValue();
    }

    public static byte unboxed(Byte v) {
        return v == null ? 0 : v.byteValue();
    }

    public static short unboxed(Short v) {
        return v == null ? 0 : v.shortValue();
    }

    public static int unboxed(Integer v) {
        return v == null ? 0 : v.intValue();
    }

    public static long unboxed(Long v) {
        return v == null ? 0 : v.longValue();
    }

    public static float unboxed(Float v) {
        return v == null ? 0 : v.floatValue();
    }

    public static double unboxed(Double v) {
        return v == null ? 0 : v.doubleValue();
    }
    
    public static Object unboxed(Object v) {
        return v;
    }
    
    public static boolean isNotEmpty(Object object) {
        return getSize(object) > 0;
    }
    
    public static Method getGetter(Object bean, String property) {
    	Map<String, Method> cache = GETTER_CACHE.get(bean.getClass());
    	if (cache == null) {
    		cache = new ConcurrentHashMap<String, Method>();
    		for (Method method : bean.getClass().getMethods()) {
    			if (Modifier.isPublic(method.getModifiers()) 
    					&& ! Modifier.isStatic(method.getModifiers()) 
    					&& ! void.class.equals(method.getReturnType())
    					&& method.getParameterTypes().length == 0) {
    				String name = method.getName();
    				if (name.length() > 3 && name.startsWith("get")) {
    					cache.put(name.substring(3, 4).toLowerCase() + name.substring(4), method);
    				} else if (name.length() > 2 && name.startsWith("is")) {
    					cache.put(name.substring(2, 3).toLowerCase() + name.substring(3), method);
    				}
    			}
    		}
    		Map<String, Method> old = GETTER_CACHE.putIfAbsent(bean.getClass(), cache);
    		if (old != null) {
    			cache = old;
    		}
    	}
    	return cache.get(property);
    }

    public static Object getProperty(Object bean, String property) {
    	if (bean == null || property == null || property.length() == 0) {
    		return null;
    	}
		Method getter = getGetter(bean, property);
		if (getter != null) {
			try {
				return getter.invoke(bean, new Object[0]);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
    	return null;
    }

    public static int getSize(Object object) {
        if (object == null) {
            return 0;
        } if (object instanceof Collection<?>) {
            return ((Collection<?>)object).size();
        } else if (object instanceof Map<?, ?>) {
            return ((Map<?, ?>)object).size();
        } else if (object instanceof Object[]) {
            return ((Object[]) object).length;
        } else if (object instanceof int[]) {
            return ((int[]) object).length;
        } else if (object instanceof long[]) {
            return ((long[]) object).length;
        } else if (object instanceof float[]) {
            return ((float[]) object).length;
        } else if (object instanceof double[]) {
            return ((double[]) object).length;
        } else if (object instanceof short[]) {
            return ((short[]) object).length;
        } else if (object instanceof byte[]) {
            return ((byte[]) object).length;
        } else if (object instanceof char[]) {
            return ((char[]) object).length;
        } else if (object instanceof boolean[]) {
            return ((boolean[]) object).length;
        } else {
            return -1;
        }
    }

    public static Iterator<Boolean> toIterator(boolean[] object) {
    	return new BooleanArrayIterator(object);
    }

    public static Iterator<Character> toIterator(char[] object) {
    	return new CharArrayIterator(object);
    }

    public static Iterator<Byte> toIterator(byte[] object) {
    	return new ByteArrayIterator(object);
    }

    public static Iterator<Short> toIterator(short[] object) {
    	return new ShortArrayIterator(object);
    }

    public static Iterator<Integer> toIterator(int[] object) {
    	return new IntArrayIterator(object);
    }

    public static Iterator<Long> toIterator(long[] object) {
    	return new LongArrayIterator(object);
    }

    public static Iterator<Float> toIterator(float[] object) {
    	return new FloatArrayIterator(object);
    }

    public static Iterator<Double> toIterator(double[] object) {
    	return new DoubleArrayIterator(object);
    }

    public static <T> Iterator<T> toIterator(T[] object) {
    	return new ObjectArrayIterator<T>(object);
    }

    public static <T> Iterator<T> toIterator(Iterator<T> object) {
    	return object;
    }

    @SuppressWarnings("unchecked")
	public static <T> Iterator<T> toIterator(Collection<T> object) {
    	return object == null ? (Iterator<T>) EmptyIterator.getEmptyIterator(): object.iterator();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K, V> Iterator<Entry<K, V>> toIterator(Map<K, V> object) {
    	return object == null ? (Iterator) EmptyIterator.getEmptyIterator(): object.entrySet().iterator();
    }

    @SuppressWarnings("unchecked")
	public static <T> Iterator<T> toIterator(Iterable<T> object) {
    	return object == null ? (Iterator<T>) EmptyIterator.getEmptyIterator(): object.iterator();
    }

    public static Iterator<?> toIterator(Object object) {
        if (object == null) {
            return EmptyIterator.getEmptyIterator();
        } else if (object instanceof Iterator<?>) {
            return ((Iterator<?>)object);
        } else if (object instanceof Iterable<?>) {
            return ((Iterable<?>)object).iterator();
        } else if (object instanceof Map<?, ?>) {
            return ((Map<?, ?>)object).entrySet().iterator();
        } else if (object instanceof Object[]) {
            return new ObjectArrayIterator<Object>((Object[]) object);
        } else if (object instanceof int[]) {
            return new IntArrayIterator((int[]) object);
        } else if (object instanceof long[]) {
            return new LongArrayIterator((long[]) object);
        } else if (object instanceof float[]) {
            return new FloatArrayIterator((float[]) object);
        } else if (object instanceof double[]) {
            return new DoubleArrayIterator((double[]) object);
        } else if (object instanceof short[]) {
            return new ShortArrayIterator((short[]) object);
        } else if (object instanceof byte[]) {
            return new ByteArrayIterator((byte[]) object);
        } else if (object instanceof char[]) {
            return new CharArrayIterator((char[]) object);
        } else if (object instanceof boolean[]) {
            return new BooleanArrayIterator((boolean[]) object);
        } else {
            throw new UnsupportedOperationException("Unsupported foreach type " + object.getClass().getName());
        }
    }

    public static URI toURI(String name) {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Class<?> getGenericClass(Class<?> cls) {
        return getGenericClass(cls, 0);
    }

    public static Class<?> getGenericClass(Class<?> cls, int i) {
        try {
            ParameterizedType parameterizedType = ((ParameterizedType) cls.getGenericInterfaces()[0]);
            Object genericClass = parameterizedType.getActualTypeArguments()[i];
            if (genericClass instanceof ParameterizedType) { // 处理多级泛型
                return (Class<?>) ((ParameterizedType) genericClass).getRawType();
            } else if (genericClass instanceof GenericArrayType) { // 处理数组泛型
                return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
            } else if (genericClass != null) {
                return (Class<?>) genericClass;
            }
        } catch (Throwable e) {
        }
        if (cls.getSuperclass() != null) {
            return getGenericClass(cls.getSuperclass(), i);
        } else {
            throw new IllegalArgumentException(cls.getName() + " generic type undefined!");
        }
    }
    
    public static String getJavaVersion() {
    	return System.getProperty("java.specification.version");
    }
    
    public static boolean isBeforeJava5(String javaVersion) {
        return (javaVersion == null || javaVersion.length() == 0 || "1.0".equals(javaVersion) 
                || "1.1".equals(javaVersion) || "1.2".equals(javaVersion) 
                || "1.3".equals(javaVersion) || "1.4".equals(javaVersion));
    }
    
    public static boolean isBeforeJava6(String javaVersion) {
        return isBeforeJava5(javaVersion) || "1.5".equals(javaVersion);
    }
    
    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName() + ": ");
        if (e.getMessage() != null) {
            p.print(e.getMessage() + "\n");
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    private static final int JIT_LIMIT = 5 * 1024;
    
    public static void checkBytecode(String name, byte[] bytecode) {
        if (bytecode.length > JIT_LIMIT) {
            System.err.println("The template bytecode too long, may be affect the JIT compiler. template class: " + name);
        }
    }
    
    public static String getSizeMethod(Class<?> cls) {
        try {
            return cls.getMethod("size", new Class<?>[0]).getName() + "()";
        } catch (NoSuchMethodException e) {
            try {
                return cls.getMethod("length", new Class<?>[0]).getName() + "()";
            } catch (NoSuchMethodException e2) {
                try {
                    return cls.getMethod("getSize", new Class<?>[0]).getName() + "()";
                } catch (NoSuchMethodException e3) {
                    try {
                        return cls.getMethod("getLength", new Class<?>[0]).getName() + "()";
                    } catch (NoSuchMethodException e4) {
                        return null;
                    }
                }
            }
        }
    }
    
    public static String getMethodName(Method method, Class<?>[] parameterClasses, String rightCode) {
        if (method.getParameterTypes().length > parameterClasses.length) {
            Class<?>[] types = method.getParameterTypes();
            StringBuilder buf = new StringBuilder(rightCode);
            for (int i = parameterClasses.length; i < types.length; i ++) {
                if (buf.length() > 0) {
                    buf.append(",");
                }
                Class<?> type = types[i];
                String def;
                if (type == boolean.class) {
                    def = "false";
                } else if (type == char.class) {
                    def = "\'\\0\'";
                } else if (type == byte.class
                        || type == short.class
                        || type == int.class
                        || type == long.class
                        || type == float.class
                        || type == double.class) {
                    def = "0";
                } else {
                    def = "null";
                }
                buf.append(def);
            }
        }
        return method.getName() + "(" + rightCode + ")";
    }
    
    public static Method searchMethod(Class<?> currentClass, String name, Class<?>[] parameterTypes) throws NoSuchMethodException {
        if (currentClass == null) {
            throw new NoSuchMethodException("class == null");
        }
        try {
            return currentClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            for (Method method : currentClass.getMethods()) {
                if (method.getName().equals(name)
                        && parameterTypes.length == method.getParameterTypes().length
                        && Modifier.isPublic(method.getModifiers())) {
                    if (parameterTypes.length > 0) {
                        Class<?>[] types = method.getParameterTypes();
                        boolean match = true;
                        for (int i = 0; i < parameterTypes.length; i ++) {
                            if (! types[i].isAssignableFrom(parameterTypes[i])) {
                                match = false;
                                break;
                            }
                        }
                        if (! match) {
                            continue;
                        }
                    }
                    return method;
                }
            }
            throw e;
        }
    }
    
    public static String getInitCode(Class<?> type) {
        if (byte.class.equals(type)
                || short.class.equals(type)
                || int.class.equals(type)
                || long.class.equals(type)
                || float.class.equals(type)
                || double.class.equals(type)) {
            return "0";
        } else if (char.class.equals(type)) {
            return "'\\0'";
        } else if (boolean.class.equals(type)) {
            return "false";
        } else {
            return "null";
        }
    }
    
    public static <K, V> Map<K, V> toMap(Map.Entry<K, V>[] entries) {
        Map<K, V> map = new HashMap<K, V>();
        if (entries != null && entries.length > 0) {
            for (Map.Entry<K, V> enrty : entries) {
                map.put(enrty.getKey(), enrty.getValue());
            }
        }
        return map;
    }

    public static boolean[] subArray(boolean[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }

    public static char[] subArray(char[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }

    public static byte[] subArray(byte[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }

    public static short[] subArray(short[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }

    public static int[] subArray(int[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }

    public static long[] subArray(long[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }

    public static float[] subArray(float[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }

    public static double[] subArray(double[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }
    
    public static <T> T[] subArray(T[] array, IntegerSequence sequence) {
        return subArray(array, array == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(array.length, sequence));
    }

    public static boolean[] subArray(boolean[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return new boolean[0];
        }
        int len = array.length;
        boolean[] sub = new boolean[indexs.length];
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }

    public static char[] subArray(char[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return new char[0];
        }
        int len = array.length;
        char[] sub = new char[indexs.length];
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }

    public static byte[] subArray(byte[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return new byte[0];
        }
        int len = array.length;
        byte[] sub = new byte[indexs.length];
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }

    public static short[] subArray(short[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return new short[0];
        }
        int len = array.length;
        short[] sub = new short[indexs.length];
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }

    public static int[] subArray(int[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return new int[0];
        }
        int len = array.length;
        int[] sub = new int[indexs.length];
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }

    public static long[] subArray(long[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return new long[0];
        }
        int len = array.length;
        long[] sub = new long[indexs.length];
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }

    public static float[] subArray(float[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return new float[0];
        }
        int len = array.length;
        float[] sub = new float[indexs.length];
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }

    public static double[] subArray(double[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return new double[0];
        }
        int len = array.length;
        double[] sub = new double[indexs.length];
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }
    
    @SuppressWarnings("unchecked")
	public static <T> T[] subArray(T[] array, int[] indexs) {
    	if (array == null || array.length == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
        }
        int len = array.length;
        T[] sub = (T[]) Array.newInstance(array.getClass().getComponentType(), indexs.length);
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                sub[i] = array[index];
            }
        }
        return sub;
    }

    public static <T> List<T> subList(List<T> list, IntegerSequence sequence) {
    	return subList(list, list == null || sequence == null ? null : getIntegerSequenceBeginAndEnd(list.size(), sequence));
    }

    public static <T> List<T> subList(List<T> list, int[] indexs) {
        if (list == null || list.size() == 0) {
            return list;
        }
        if (indexs == null || indexs.length == 0) {
            return new ArrayList<T>(0);
        }
        List<T> result = new ArrayList<T>(indexs.length);
        for (int index : indexs) {
            if (index < 0) {
                index = list.size() + index;
            }
            if (index >= 0 && index < list.size()) {
                result.add(list.get(index));
            }
        }
        return result;
    }
    
    private static int[] getIntegerSequenceBeginAndEnd(int size, IntegerSequence sequence) {
        int begin = sequence.getBegin();
        if (begin < 0) {
            begin = size + begin;
        }
        if (begin < 0) {
            begin = 0;
        }
        if (begin >= size) {
            begin = size - 1;
        }
        int end = sequence.getEnd();
        if (end < 0) {
            end = size + end;
        }
        if (end < 0) {
            end = 0;
        }
        if (end >= size) {
            end = size - 1;
        }
        return new int[] {Math.min(begin, end), Math.max(begin, end) + 1};
    }
    
    public static Map<String, Object> toMap(Collection<String> names, Object[] parameters) {
    	return toMap(names.toArray(new String[0]), parameters);
    }

    public static Map<String, Object> toMap(String[] names, Object[] parameters) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (parameters == null || parameters.length == 0) {
            return map;
        }
        if (names == null || names.length < parameters.length) {
            throw new IllegalArgumentException("Mismatch parameters. names: " + Arrays.toString(names) + ", values: " + Arrays.toString(parameters));
        }
        for (int i = 0; i < parameters.length; i ++) {
            map.put(names[i], parameters[i]);
        }
        return map;
    }
    
    public static Map<String, Object> getBeanProperties(Object bean) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	for (Method method : bean.getClass().getMethods()) {
    		String name = method.getName();
    		if ((name.length() > 3 && name.startsWith("get") 
    				|| name.length() > 2 && name.startsWith("is"))
    				&& Modifier.isPublic(method.getModifiers())
    				&& method.getParameterTypes().length == 0
    				&& method.getDeclaringClass() != Object.class) {
    			int i = name.startsWith("get") ? 3 : 2;
    			String key = name.substring(i, i + 1).toLowerCase() + name.substring(i + 1);
    			try {
					map.put(key, method.invoke(bean, new Object[0]));
				} catch (Exception e) {
				}
    		}
    	}
    	return map;
    }
    
    public static void add(Object left, Object right) {
    	
    }
    
    private ClassUtils() {}

}
