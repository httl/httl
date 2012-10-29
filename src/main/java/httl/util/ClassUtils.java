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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * ClassUtils. (Tool, Static, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ClassUtils {
    
    public static final String CLASS_EXTENSION = ".class";

    public static final String JAVA_EXTENSION = ".java";

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
    
    public static Class<?> _forName(String className) throws ClassNotFoundException {
        if ("boolean".equals(className))
            return boolean.class;
        if ("byte".equals(className))
            return byte.class;
        if ("char".equals(className))
            return char.class;
        if ("short".equals(className))
            return short.class;
        if ("int".equals(className))
            return int.class;
        if ("long".equals(className))
            return long.class;
        if ("float".equals(className))
            return float.class;
        if ("double".equals(className))
            return double.class;
        if ("boolean[]".equals(className))
            return boolean[].class;
        if ("byte[]".equals(className))
            return byte[].class;
        if ("char[]".equals(className))
            return char[].class;
        if ("short[]".equals(className))
            return short[].class;
        if ("int[]".equals(className))
            return int[].class;
        if ("long[]".equals(className))
            return long[].class;
        if ("float[]".equals(className))
            return float[].class;
        if ("double[]".equals(className))
            return double[].class;
        try {
            return arrayForName(className);
        } catch (ClassNotFoundException e) {
            if (className.indexOf('.') == -1) { // 尝试java.lang包
                try {
                    return arrayForName("java.lang." + className);
                } catch (ClassNotFoundException e2) {
                    // 忽略尝试异常, 抛出原始异常
                }
            }
            throw e;
        }
    }
    
    private static Class<?> arrayForName(String className) throws ClassNotFoundException {
        return Class.forName(className.endsWith("[]")
                ? "[L" + className.substring(0, className.length() - 2) + ";"
                        : className, true, Thread.currentThread().getContextClassLoader());
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
    
    public static int getSize(Object object) {
        if (object == null) {
            return 0;
        } if (object instanceof Collection<?>) {
            return ((Collection<?>)object).size();
        } else if (object instanceof Map<?, ?>) {
            return ((Map<?, ?>)object).size();
        } else if (object.getClass().isArray()) {
            return Array.getLength(object);
        } else {
            return -1;
        }
    }

    public static Iterator<?> toIterator(Object object) {
        if (object == null) {
            return Collections.EMPTY_LIST.iterator();
        } else if (object instanceof Iterator<?>) {
            return ((Iterator<?>)object);
        } else if (object instanceof Iterable<?>) {
            return ((Iterable<?>)object).iterator();
        } else if (object instanceof Map<?, ?>) {
            return ((Map<?, ?>)object).entrySet().iterator();
        } else if (object.getClass().isArray()) {
            return new ArrayIterator<Object>(object);
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

    public static boolean[] subArray(boolean[] list, IntegerSequence sequence) {
        return (boolean[])doSubArray(list, sequence);
    }

    public static char[] subArray(char[] list, IntegerSequence sequence) {
        return (char[])doSubArray(list, sequence);
    }

    public static byte[] subArray(byte[] list, IntegerSequence sequence) {
        return (byte[])doSubArray(list, sequence);
    }

    public static short[] subArray(short[] list, IntegerSequence sequence) {
        return (short[])doSubArray(list, sequence);
    }

    public static int[] subArray(int[] list, IntegerSequence sequence) {
        return (int[])doSubArray(list, sequence);
    }

    public static long[] subArray(long[] list, IntegerSequence sequence) {
        return (long[])doSubArray(list, sequence);
    }

    public static float[] subArray(float[] list, IntegerSequence sequence) {
        return (float[])doSubArray(list, sequence);
    }

    public static double[] subArray(double[] list, IntegerSequence sequence) {
        return (double[])doSubArray(list, sequence);
    }
    
    public static Object[] subArray(Object[] list, IntegerSequence sequence) {
        return (Object[])doSubArray(list, sequence);
    }
    
    public static boolean[] subArray(boolean[] list, int[] sequence) {
        return (boolean[])doSubArray(list, sequence);
    }

    public static char[] subArray(char[] list, int[] sequence) {
        return (char[])doSubArray(list, sequence);
    }

    public static byte[] subArray(byte[] list, int[] sequence) {
        return (byte[])doSubArray(list, sequence);
    }

    public static short[] subArray(short[] list, int[] sequence) {
        return (short[])doSubArray(list, sequence);
    }

    public static int[] subArray(int[] list, int[] sequence) {
        return (int[])doSubArray(list, sequence);
    }

    public static long[] subArray(long[] list, int[] sequence) {
        return (long[])doSubArray(list, sequence);
    }

    public static float[] subArray(float[] list, int[] sequence) {
        return (float[])doSubArray(list, sequence);
    }

    public static double[] subArray(double[] list, int[] sequence) {
        return (double[])doSubArray(list, sequence);
    }
    
    public static Object[] subArray(Object[] list, int[] sequence) {
        return (Object[])doSubArray(list, sequence);
    }
    
    private static Object doSubArray(Object array, IntegerSequence sequence) {
        if (array == null || ! array.getClass().isArray() 
                || Array.getLength(array) == 0) {
            return array;
        }
        if (sequence == null) {
            return array;
        }
        int[] index = getIntegerSequenceBeginAndEnd(Array.getLength(array), sequence);
        int len = index[1] - index[0];
        Object sub = Array.newInstance(array.getClass().getComponentType(), len);
        for (int i = 0; i < len; i ++) {
            Array.set(sub, i, Array.get(array, i + index[0]));
        }
        return sub;
    }

    private static Object doSubArray(Object array, int[] indexs) {
        if (array == null || ! array.getClass().isArray() 
                || Array.getLength(array) == 0) {
            return array;
        }
        if (indexs == null || indexs.length == 0) {
            return Array.newInstance(array.getClass().getComponentType(), 0);
        }
        int len = Array.getLength(array);
        Object sub = Array.newInstance(array.getClass().getComponentType(), indexs.length);
        for (int i = 0; i < indexs.length; i ++) {
            int index = indexs[i];
            if (index < 0) {
                index = len + index;
            }
            if (index >= 0 && index < len) {
                Array.set(sub, i, Array.get(array, index));
            }
        }
        return sub;
    }
    
    public static <T> List<T> subList(List<T> list, IntegerSequence sequence) {
        if (list == null || list.size() == 0) {
            return list;
        }
        if (sequence == null) {
            return list;
        }
        int[] index = getIntegerSequenceBeginAndEnd(list.size(), sequence);
        return list.subList(index[0], index[1]);
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
