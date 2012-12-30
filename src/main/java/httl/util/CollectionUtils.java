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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtils {

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
		T[] all = (T[]) Array.newInstance(c1.getClass().getComponentType(), c1.length + c2.length);
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

}
