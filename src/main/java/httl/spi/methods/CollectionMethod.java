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
package httl.spi.methods;

import httl.internal.util.CollectionUtils;
import httl.spi.methods.cycles.ArrayCycle;
import httl.spi.methods.cycles.BooleanArrayCycle;
import httl.spi.methods.cycles.ByteArrayCycle;
import httl.spi.methods.cycles.CharArrayCycle;
import httl.spi.methods.cycles.DoubleArrayCycle;
import httl.spi.methods.cycles.FloatArrayCycle;
import httl.spi.methods.cycles.IntArrayCycle;
import httl.spi.methods.cycles.ListCycle;
import httl.spi.methods.cycles.LongArrayCycle;
import httl.spi.methods.cycles.ShortArrayCycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * CollectionMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CollectionMethod {
	
	private CollectionMethod() {}

	public static <T> ListCycle<T> toCycle(Collection<T> values) {
		return new ListCycle<T>(values);
	}

	public static <T> ArrayCycle<T> toCycle(T[] values) {
		return new ArrayCycle<T>(values);
	}

	public static BooleanArrayCycle toCycle(boolean[] values) {
		return new BooleanArrayCycle(values);
	}

	public static CharArrayCycle toCycle(char[] values) {
		return new CharArrayCycle(values);
	}

	public static ByteArrayCycle toCycle(byte[] values) {
		return new ByteArrayCycle(values);
	}

	public static ShortArrayCycle toCycle(short[] values) {
		return new ShortArrayCycle(values);
	}

	public static IntArrayCycle toCycle(int[] values) {
		return new IntArrayCycle(values);
	}

	public static LongArrayCycle toCycle(long[] values) {
		return new LongArrayCycle(values);
	}

	public static FloatArrayCycle toCycle(float[] values) {
		return new FloatArrayCycle(values);
	}

	public static DoubleArrayCycle toCycle(double[] values) {
		return new DoubleArrayCycle(values);
	}

	public static int length(Map<?, ?> values) {
		return values == null ? 0 : values.size();
	}

	public static int length(Collection<?> values) {
		return values == null ? 0 : values.size();
	}

	public static int length(Object[] values) {
		return values == null ? 0 : values.length;
	}

	public static int length(boolean[] values) {
		return values == null ? 0 : values.length;
	}

	public static int length(char[] values) {
		return values == null ? 0 : values.length;
	}

	public static int length(byte[] values) {
		return values == null ? 0 : values.length;
	}

	public static int length(short[] values) {
		return values == null ? 0 : values.length;
	}

	public static int length(int[] values) {
		return values == null ? 0 : values.length;
	}

	public static int length(long[] values) {
		return values == null ? 0 : values.length;
	}

	public static int length(float[] values) {
		return values == null ? 0 : values.length;
	}

	public static int length(double[] values) {
		return values == null ? 0 : values.length;
	}

	public static <K, V> Map<K, V> sort(Map<K, V> map) {
		if (map == null) {
			return null;
		}
		return new TreeMap<K, V>(map);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<T> sort(List<T> list) {
		if (list == null) {
			return null;
		}
		list = new ArrayList<T>(list);
		Collections.sort((List) list);
		return list;
	}

	public static <T> Set<T> sort(Set<T> set) {
		if (set == null) {
			return null;
		}
		return new TreeSet<T>(set);
	}
	
	public static <T> Collection<T> sort(Collection<T> set) {
		if (set == null) {
			return null;
		}
		return new TreeSet<T>(set);
	}

	public static <T> T[] sort(T[] array) {
		if (array == null) {
			return null;
		}
		array = CollectionUtils.copyOf(array, array.length);
		Arrays.sort(array);
		return array;
	}

	public static char[] sort(char[] array) {
		if (array == null) {
			return null;
		}
		array = CollectionUtils.copyOf(array, array.length);
		Arrays.sort(array);
		return array;
	}

	public static byte[] sort(byte[] array) {
		if (array == null) {
			return null;
		}
		array = CollectionUtils.copyOf(array, array.length);
		Arrays.sort(array);
		return array;
	}

	public static short[] sort(short[] array) {
		if (array == null) {
			return null;
		}
		array = CollectionUtils.copyOf(array, array.length);
		Arrays.sort(array);
		return array;
	}

	public static int[] sort(int[] array) {
		if (array == null) {
			return null;
		}
		array = CollectionUtils.copyOf(array, array.length);
		Arrays.sort(array);
		return array;
	}

	public static long[] sort(long[] array) {
		if (array == null) {
			return null;
		}
		array = CollectionUtils.copyOf(array, array.length);
		Arrays.sort(array);
		return array;
	}

	public static float[] sort(float[] array) {
		if (array == null) {
			return null;
		}
		array = CollectionUtils.copyOf(array, array.length);
		Arrays.sort(array);
		return array;
	}

	public static double[] sort(double[] array) {
		if (array == null) {
			return null;
		}
		array = CollectionUtils.copyOf(array, array.length);
		Arrays.sort(array);
		return array;
	}

}