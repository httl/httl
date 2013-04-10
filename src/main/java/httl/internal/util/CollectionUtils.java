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

import httl.internal.util.iterators.BooleanArrayIterator;
import httl.internal.util.iterators.ByteArrayIterator;
import httl.internal.util.iterators.CharArrayIterator;
import httl.internal.util.iterators.DoubleArrayIterator;
import httl.internal.util.iterators.EmptyIterator;
import httl.internal.util.iterators.FloatArrayIterator;
import httl.internal.util.iterators.IntArrayIterator;
import httl.internal.util.iterators.LongArrayIterator;
import httl.internal.util.iterators.ObjectArrayIterator;
import httl.internal.util.iterators.ShortArrayIterator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CollectionUtils {

	public static byte[] createSequence(byte begin, byte end) {
		int sign = end > begin ? 1 : -1;
		int len = Math.abs(end - begin) + 1;
		byte[] seq = new byte[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = (byte) (begin + i * sign);
		}
		return seq;
	}

	public static char[] createSequence(char begin, char end) {
		int sign = end > begin ? 1 : -1;
		int len = Math.abs(end - begin) + 1;
		char[] seq = new char[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = (char) (begin + i * sign);
		}
		return seq;
	}

	public static short[] createSequence(short begin, short end) {
		int sign = end > begin ? 1 : -1;
		int len = Math.abs(end - begin) + 1;
		short[] seq = new short[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = (short) (begin + i * sign);
		}
		return seq;
	}

	public static int[] createSequence(int begin, int end) {
		int sign = end > begin ? 1 : -1;
		int len = Math.abs(end - begin) + 1;
		int[] seq = new int[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = begin + i * sign;
		}
		return seq;
	}

	public static long[] createSequence(long begin, long end) {
		int sign = end > begin ? 1 : -1;
		int len = (int) (Math.abs(end - begin) + 1);
		long[] seq = new long[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = begin + i * sign;
		}
		return seq;
	}

	public static float[] createSequence(float begin, float end) {
		int sign = end > begin ? 1 : -1;
		int len = (int) (Math.abs(end - begin) + 1);
		float[] seq = new float[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = begin + i * sign;
		}
		return seq;
	}

	public static double[] createSequence(double begin, double end) {
		int sign = end > begin ? 1 : -1;
		int len = (int) (Math.abs(end - begin) + 1);
		double[] seq = new double[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = begin + i * sign;
		}
		return seq;
	}

	public static Byte[] createSequence(Byte begin, Byte end) {
		int sign = end > begin ? 1 : -1;
		int len = Math.abs(end - begin) + 1;
		Byte[] seq = new Byte[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = (byte) (begin + i * sign);
		}
		return seq;
	}

	public static Character[] createSequence(Character begin, Character end) {
		int sign = end > begin ? 1 : -1;
		int len = Math.abs(end - begin) + 1;
		Character[] seq = new Character[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = (char) (begin + i * sign);
		}
		return seq;
	}

	public static Short[] createSequence(Short begin, Short end) {
		int sign = end > begin ? 1 : -1;
		int len = Math.abs(end - begin) + 1;
		Short[] seq = new Short[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = (short) (begin + i * sign);
		}
		return seq;
	}

	public static Integer[] createSequence(Integer begin, Integer end) {
		int sign = end > begin ? 1 : -1;
		int len = Math.abs(end - begin) + 1;
		Integer[] seq = new Integer[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = begin + i * sign;
		}
		return seq;
	}

	public static Long[] createSequence(Long begin, Long end) {
		int sign = end > begin ? 1 : -1;
		int len = (int) (Math.abs(end - begin) + 1);
		Long[] seq = new Long[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = begin + i * sign;
		}
		return seq;
	}

	public static Float[] createSequence(Float begin, Float end) {
		int sign = end > begin ? 1 : -1;
		int len = (int) (Math.abs(end - begin) + 1);
		Float[] seq = new Float[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = begin + i * sign;
		}
		return seq;
	}

	public static Double[] createSequence(Double begin, Double end) {
		int sign = end > begin ? 1 : -1;
		int len = (int) (Math.abs(end - begin) + 1);
		Double[] seq = new Double[len];
		for (int i = 0; i < len; i ++) {
			seq[i] = begin + i * sign;
		}
		return seq;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] remove(T[] values, T value) {
		if (values != null && value != null) {
			for (int i = 0; i < values.length; i ++) {
				T v = values[i];
				if (value.equals(v)) {
					T[] newValues = (T[]) Array.newInstance(values.getClass().getComponentType(), values.length - 1);
					if (i > 0) {
						System.arraycopy(values, 0, newValues, 0, i);
					}
					if (i < values.length - 1) {
						System.arraycopy(values, i + 1, newValues, i, values.length - 1 - i);
					}
					values = newValues;
					i --;
				}
			}
		}
		return values;
	}

	public static <K, V> Map<K, V> merge(Map<K, V> c1, Map<K, V> c2) {
		if (c1 == null || c1.size() == 0) {
			return c2;
		}
		if (c2 == null || c2.size() == 0) {
			return c1;
		}
		Map<K, V> all = new HashMap<K, V>(c1.size() + c2.size());
		all.putAll(c1);
		all.putAll(c2);
		return all;
	}

	public static <T> Collection<T> merge(Collection<T> c1, Collection<T> c2) {
		if (c1 == null || c1.size() == 0) {
			return c2;
		}
		if (c2 == null || c2.size() == 0) {
			return c1;
		}
		Collection<T> all = (c1 instanceof List ? new ArrayList<T>(c1.size() + c2.size()) : new HashSet<T>(c1.size() + c2.size()));
		all.addAll(c1);
		all.addAll(c2);
		return all;
	}

	public static <T> Set<T> merge(Set<T> c1, Set<T> c2) {
		if (c1 == null || c1.size() == 0) {
			return c2;
		}
		if (c2 == null || c2.size() == 0) {
			return c1;
		}
		Set<T> all = new HashSet<T>(c1.size() + c2.size());
		all.addAll(c1);
		all.addAll(c2);
		return all;
	}

	public static <T> List<T> merge(List<T> c1, List<T> c2) {
		if (c1 == null || c1.size() == 0) {
			return c2;
		}
		if (c2 == null || c2.size() == 0) {
			return c1;
		}
		List<T> all = new ArrayList<T>(c1.size() + c2.size());
		all.addAll(c1);
		all.addAll(c2);
		return all;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] merge(T[] c1, T[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		Class<?> t;
		if (c1.getClass().isAssignableFrom(c2.getClass())) {
			t = c1.getClass().getComponentType();
		} else if (c2.getClass().isAssignableFrom(c1.getClass())) {
			t = c2.getClass().getComponentType();
		} else {
			throw new IllegalStateException("Can not merge " + c1.getClass().getCanonicalName() + " and " + c2.getClass().getCanonicalName());
		}
		T[] all = (T[]) Array.newInstance(t, c1.length + c2.length);
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] merge(T[] c1, Collection<T> c2) {
		return merge(c1, c2 == null ? null : (T[]) c2.toArray(new Object[0]));
	}

	public static <T> T[] merge(Collection<T> c1, T[] c2) {
		return merge(c2, c2 == null ? null : Arrays.asList(c2));
	}

	public static boolean[] merge(boolean[] c1, boolean[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		boolean[] all = new boolean[c1.length + c2.length];
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	public static char[] merge(char[] c1, char[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		char[] all = new char[c1.length + c2.length];
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	public static byte[] merge(byte[] c1, byte[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		byte[] all = new byte[c1.length + c2.length];
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	public static short[] merge(short[] c1, short[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		short[] all = new short[c1.length + c2.length];
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	public static int[] merge(int[] c1, int[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		int[] all = new int[c1.length + c2.length];
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	public static long[] merge(long[] c1, long[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		long[] all = new long[c1.length + c2.length];
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	public static float[] merge(float[] c1, float[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		float[] all = new float[c1.length + c2.length];
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	public static double[] merge(double[] c1, double[] c2) {
		if (c1 == null || c1.length == 0) {
			return c2;
		}
		if (c2 == null || c2.length == 0) {
			return c1;
		}
		double[] all = new double[c1.length + c2.length];
		System.arraycopy(c1, 0, all, 0, c1.length);
		System.arraycopy(c2, 0, all, c1.length, c2.length);
		return all;
	}

	public static <K, V> void putIfAbsent(Map<K, V> from, Map<K, ? super V> to) {
		if (from == null || to == null) {
			return;
		}
		for (Map.Entry<K, V> entry : from.entrySet()) {
			if (! to.containsKey(entry.getKey())) {
				to.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public static boolean[] copyOf(boolean[] cs, int len) {
		if (cs == null) {
			return null;
		}
		boolean[] cr = new boolean[len];
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	public static char[] copyOf(char[] cs, int len) {
		if (cs == null) {
			return null;
		}
		char[] cr = new char[len];
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	public static byte[] copyOf(byte[] cs, int len) {
		if (cs == null) {
			return null;
		}
		byte[] cr = new byte[len];
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	public static short[] copyOf(short[] cs, int len) {
		if (cs == null) {
			return null;
		}
		short[] cr = new short[len];
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	public static int[] copyOf(int[] cs, int len) {
		if (cs == null) {
			return null;
		}
		int[] cr = new int[len];
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	public static long[] copyOf(long[] cs, int len) {
		if (cs == null) {
			return null;
		}
		long[] cr = new long[len];
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	public static float[] copyOf(float[] cs, int len) {
		if (cs == null) {
			return null;
		}
		float[] cr = new float[len];
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	public static double[] copyOf(double[] cs, int len) {
		if (cs == null) {
			return null;
		}
		double[] cr = new double[len];
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] copyOf(T[] cs, int len) {
		if (cs == null) {
			return null;
		}
		T[] cr = (T[]) Array.newInstance(cs.getClass().getComponentType(), len);
		if (len > 0) {
			System.arraycopy(cs, 0, cr, 0, len);
		}
		return cr;
	}

	public static boolean isIntegerArray(Object[] values) {
		for (Object value : values) {
			if (! (value instanceof Number)) {
				return false;
			}
		}
		return true;
	}

	public static int[] toIntegerArray(Object[] values) {
		int[] result = new int[values.length];
		for (int i = 0; i < values.length; i ++) {
			result[i] = ((Number) values[i]).intValue();
		}
		return result;
	}

	public static boolean isEmpty(boolean[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(boolean[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(byte[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(byte[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(char[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(char[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(short[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(short[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(int[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(int[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(long[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(long[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(float[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(float[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(double[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(double[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(Object[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNotEmpty(Object[] values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(Collection<?> values) {
		return values == null || values.size() == 0;
	}

	public static boolean isNotEmpty(Collection<?> values) {
		return ! isEmpty(values);
	}

	public static boolean isEmpty(Map<?, ?> values) {
		return values == null || values.size() == 0;
	}

	public static boolean isNotEmpty(Map<?, ?> values) {
		return ! isEmpty(values);
	}

	public static boolean containsAny(List<?> value, Object[] values) {
		if (value != null && values != null) {
			for (Object v : values) {
				if (value.contains(v)) {
					return true;
				}
			}
		}
		return false;
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

	public static <K, V> Map<K, V> toMap(Map.Entry<K, V>[] entries) {
		Map<K, V> map = new HashMap<K, V>();
		if (entries != null && entries.length > 0) {
			for (Map.Entry<K, V> enrty : entries) {
				map.put(enrty.getKey(), enrty.getValue());
			}
		}
		return map;
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
	
}