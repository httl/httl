package httl.spi.methods;

import httl.util.ClassUtils;
import httl.util.CollectionUtils;
import httl.util.MapEntry;
import httl.util.StringSequence;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class LangMethod {

	private String[] importPackages;

	private final List<StringSequence> importSequences = new CopyOnWriteArrayList<StringSequence>();

	/**
	 * httl.properties: import.packages=java.util
	 */
	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	/**
	 * httl.properties: import.sequences=Mon Tue Wed Thu Fri Sat Sun Mon
	 */
	public void setImportSequences(String[] sequences) {
		for (String s : sequences) {
			s = s.trim();
			if (s.length() > 0) {
				String[] ts = s.split("\\s+");
				List<String> sequence = new ArrayList<String>();
				for (String t : ts) {
					t = t.trim();
					if (t.length() > 0) {
						sequence.add(t);
					}
				}
				this.importSequences.add(new StringSequence(sequence));
			}
		}
	}

	public static int neg(byte left) {
		return - left;
	}

	public static int neg(short left) {
		return - left;
	}

	public static int neg(int left) {
		return - left;
	}

	public static long neg(long left) {
		return - left;
	}

	public static float neg(float left) {
		return - left;
	}

	public static double neg(double left) {
		return - left;
	}

	public static int add(byte left, byte right) {
		return left + right;
	}

	public static int add(short left, short right) {
		return left + right;
	}

	public static int add(int left, int right) {
		return left + right;
	}

	public static long add(long left, long right) {
		return left + right;
	}

	public static float add(float left, float right) {
		return left + right;
	}

	public static double add(double left, double right) {
		return left + right;
	}

	public static String add(String left, String right) {
		return left + right;
	}

	public static String add(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null) {
			return String.valueOf(right);
		}
		if (right == null) {
			return String.valueOf(left);
		}
		return String.valueOf(left) + String.valueOf(right);
	}

	public static boolean[] add(boolean[] left, boolean[] right) {
		return CollectionUtils.merge(left, right);
	}

	public static char[] add(char[] left, char[] right) {
		return CollectionUtils.merge(left, right);
	}

	public static byte[] add(byte[] left, byte[] right) {
		return CollectionUtils.merge(left, right);
	}

	public static short[] add(short[] left, short[] right) {
		return CollectionUtils.merge(left, right);
	}

	public static int[] add(int[] left, int[] right) {
		return CollectionUtils.merge(left, right);
	}

	public static long[] add(long[] left, long[] right) {
		return CollectionUtils.merge(left, right);
	}

	public static float[] add(float[] left, float[] right) {
		return CollectionUtils.merge(left, right);
	}

	public static double[] add(double[] left, double[] right) {
		return CollectionUtils.merge(left, right);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] add(T[] left, T right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null) {
			T[] all = (T[]) Array.newInstance(right.getClass(), 1);
			all[0] = right;
			return all;
		}
		if (right == null) {
			return left;
		}
		T[] all = (T[]) Array.newInstance(left.getClass().getComponentType(), left.length + 1);
		System.arraycopy(left, 0, all, 0, left.length);
		all[left.length] = right;
		return all;
	}

	public static <T> List<T> add(List<T> left, T right) {
		if (right instanceof List) {
			return _add(left, (List<T>) right);
		}
		if (left == null && right == null) {
			return null;
		}
		if (left == null) {
			List<T> all = new ArrayList<T>(1);
			all.add(right);
			return all;
		}
		if (right == null) {
			return left;
		}
		List<T> all = new ArrayList<T>(left.size() + 1);
		all.addAll(left);
		all.add(right);
		return all;
	}

	public static <T> Set<T> add(Set<T> left, T right) {
		if (right instanceof Set) {
			return _add(left, (Set<T>) right);
		}
		if (left == null && right == null) {
			return null;
		}
		if (left == null) {
			Set<T> all = new HashSet<T>(1);
			all.add(right);
			return all;
		}
		if (right == null) {
			return left;
		}
		Set<T> all = new HashSet<T>(left.size() + 1);
		all.addAll(left);
		all.add(right);
		return all;
	}

	public static <T> Collection<T> add(Collection<T> left, T right) {
		if (left instanceof Set) {
			return add((Set<T>) left, right);
		}
		return add((List<T>) left, right);
	}

	public static <T> T[] add(T[] left, T[] right) {
		return CollectionUtils.merge(left, right);
	}

	private static <T> List<T> _add(List<T> left, List<T> right) {
		return CollectionUtils.merge(left, right);
	}

	private static <T> Set<T> _add(Set<T> left, Set<T> right) {
		return CollectionUtils.merge(left, right);
	}

	private static <T> Collection<T> _add(Collection<T> left, Collection<T> right) {
		return CollectionUtils.merge(left, right);
	}

	public static <T> T[] add(T[] left, Collection<T> right) {
		return CollectionUtils.merge(left, right);
	}

	public static <T> T[] add(Collection<T> left, T[] right) {
		return CollectionUtils.merge(left, right);
	}

	public static <K, V> Map<K, V> add(Map<K, V> left, Map<K, V> right) {
		return CollectionUtils.merge(left, right);
	}

	public static int sub(byte left, byte right) {
		return left - right;
	}

	public static int sub(short left, short right) {
		return left - right;
	}

	public static int sub(int left, int right) {
		return left - right;
	}

	public static long sub(long left, long right) {
		return left - right;
	}

	public static float sub(float left, float right) {
		return left - right;
	}

	public static double sub(double left, double right) {
		return left - right;
	}

	public static int mul(byte left, byte right) {
		return left * right;
	}

	public static int mul(short left, short right) {
		return left * right;
	}

	public static int mul(int left, int right) {
		return left * right;
	}

	public static long mul(long left, long right) {
		return left * right;
	}

	public static float mul(float left, float right) {
		return left * right;
	}

	public static double mul(double left, double right) {
		return left * right;
	}

	public static int div(byte left, byte right) {
		return left / right;
	}

	public static int div(short left, short right) {
		return left / right;
	}

	public static int div(int left, int right) {
		return left / right;
	}

	public static long div(long left, long right) {
		return left / right;
	}

	public static float div(float left, float right) {
		return left / right;
	}

	public static double div(double left, double right) {
		return left / right;
	}

	public static int mod(byte left, byte right) {
		return left % right;
	}

	public static int mod(short left, short right) {
		return left % right;
	}

	public static int mod(int left, int right) {
		return left % right;
	}

	public static long mod(long left, long right) {
		return left % right;
	}

	public static float mod(float left, float right) {
		return left % right;
	}

	public static double mod(double left, double right) {
		return left % right;
	}

	public static boolean eq(boolean left, boolean right) {
		return left == right;
	}

	public static boolean eq(char left, char right) {
		return left == right;
	}

	public static boolean eq(byte left, byte right) {
		return left == right;
	}

	public static boolean eq(short left, short right) {
		return left == right;
	}

	public static boolean eq(int left, int right) {
		return left == right;
	}

	public static boolean eq(long left, long right) {
		return left == right;
	}

	public static boolean eq(float left, float right) {
		return left == right;
	}

	public static boolean eq(double left, double right) {
		return left == right;
	}

	public static boolean eq(Object left, Object right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}
		if (left instanceof String) {
			right = right.toString();
		} else if (right instanceof String) {
			left = left.toString();
		}
		return left.equals(right);
	}

	public static boolean ne(boolean left, boolean right) {
		return left != right;
	}

	public static boolean ne(char left, char right) {
		return left != right;
	}

	public static boolean ne(byte left, byte right) {
		return left != right;
	}

	public static boolean ne(short left, short right) {
		return left != right;
	}

	public static boolean ne(int left, int right) {
		return left != right;
	}

	public static boolean ne(long left, long right) {
		return left != right;
	}

	public static boolean ne(float left, float right) {
		return left != right;
	}

	public static boolean ne(double left, double right) {
		return left != right;
	}

	public static boolean ne(Object left, Object right) {
		return left == null ? right != null : ! left.equals(right);
	}

	public static boolean gt(char left, char right) {
		return left > right;
	}

	public static boolean gt(byte left, byte right) {
		return left > right;
	}

	public static boolean gt(short left, short right) {
		return left > right;
	}

	public static boolean gt(int left, int right) {
		return left > right;
	}

	public static boolean gt(long left, long right) {
		return left > right;
	}

	public static boolean gt(float left, float right) {
		return left > right;
	}

	public static boolean gt(double left, double right) {
		return left > right;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean gt(Comparable left, Comparable right) {
		return left != null && left.compareTo(right) > 0;
	}

	public static boolean ge(char left, char right) {
		return left >= right;
	}

	public static boolean ge(byte left, byte right) {
		return left >= right;
	}

	public static boolean ge(short left, short right) {
		return left >= right;
	}

	public static boolean ge(int left, int right) {
		return left >= right;
	}

	public static boolean ge(long left, long right) {
		return left >= right;
	}

	public static boolean ge(float left, float right) {
		return left >= right;
	}

	public static boolean ge(double left, double right) {
		return left >= right;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean ge(Comparable left, Comparable right) {
		return left == null && right == null || 
				left != null && left.compareTo(right) >= 0;
	}

	public static boolean lt(char left, char right) {
		return left < right;
	}

	public static boolean lt(byte left, byte right) {
		return left < right;
	}

	public static boolean lt(short left, short right) {
		return left < right;
	}

	public static boolean lt(int left, int right) {
		return left < right;
	}

	public static boolean lt(long left, long right) {
		return left < right;
	}

	public static boolean lt(float left, float right) {
		return left < right;
	}

	public static boolean lt(double left, double right) {
		return left < right;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean lt(Comparable left, Comparable right) {
		return left != null && left.compareTo(right) < 0;
	}

	public static boolean le(char left, char right) {
		return left <= right;
	}

	public static boolean le(byte left, byte right) {
		return left <= right;
	}

	public static boolean le(short left, short right) {
		return left <= right;
	}

	public static boolean le(int left, int right) {
		return left <= right;
	}

	public static boolean le(long left, long right) {
		return left <= right;
	}

	public static boolean le(float left, float right) {
		return left <= right;
	}

	public static boolean le(double left, double right) {
		return left <= right;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean le(Comparable left, Comparable right) {
		return left == null && right == null || 
				left != null && left.compareTo(right) <= 0;
	}

	public static boolean and(boolean left, boolean right) {
		return left && right;
	}

	public static Boolean and(Boolean left, Boolean right) {
		return left && right;
	}

	public static Object and(Object left, Object right) {
		return ClassUtils.isTrue(left) ? right : left;
	}

	public static boolean or(boolean left, boolean right) {
		return left || right;
	}

	public static Boolean or(Boolean left, Boolean right) {
		return left || right;
	}

	public static Object or(Object left, Object right) {
		return ClassUtils.isTrue(left) ? left : right;
	}

	public static boolean not(boolean left) {
		return ! left;
	}

	public static boolean not(Object left) {
		return ! ClassUtils.isTrue(left);
	}

	public static int bitand(byte left, byte right) {
		return left & right;
	}

	public static int bitand(short left, short right) {
		return left & right;
	}

	public static int bitand(int left, int right) {
		return left & right;
	}

	public static long bitand(long left, long right) {
		return left & right;
	}

	public static int bitor(byte left, byte right) {
		return left | right;
	}

	public static int bitor(short left, short right) {
		return left | right;
	}

	public static int bitor(int left, int right) {
		return left | right;
	}

	public static long bitor(long left, long right) {
		return left | right;
	}

	public static int bitnot(byte left) {
		return ~ left;
	}

	public static int bitnot(short left) {
		return ~ left;
	}

	public static int bitnot(int left) {
		return ~ left;
	}

	public static long bitnot(long left) {
		return ~ left;
	}

	public static int xor(byte left, byte right) {
		return left ^ right;
	}

	public static int xor(short left, short right) {
		return left ^ right;
	}

	public static int xor(int left, int right) {
		return left ^ right;
	}

	public static long xor(long left, long right) {
		return left ^ right;
	}

	public static int leftshift(byte left, byte right) {
		return left << right;
	}

	public static int leftshift(short left, short right) {
		return left << right;
	}

	public static int leftshift(int left, int right) {
		return left << right;
	}

	public static long leftshift(long left, long right) {
		return left << right;
	}

	public static int rightshift(byte left, byte right) {
		return left >> right;
	}

	public static int rightshift(short left, short right) {
		return left >> right;
	}

	public static int rightshift(int left, int right) {
		return left >> right;
	}

	public static long rightshift(long left, long right) {
		return left >> right;
	}

	public static int unsignshift(byte left, byte right) {
		return left >>> right;
	}

	public static int unsignshift(short left, short right) {
		return left >>> right;
	}

	public static int unsignshift(int left, int right) {
		return left >>> right;
	}

	public static long unsignshift(long left, long right) {
		return left >>> right;
	}
	
	public boolean is(Object left, String right) {
		return is(left, ClassUtils.forName(importPackages, right));
	}

	public static boolean is(Object left, Class<?> right) {
		return right.isInstance(left);
	}

	public static <T> T[] array(T[] left, T right) {
		return add(left, right);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] array(T left, T right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null) {
			T[] array = (T[]) Array.newInstance(right.getClass(), 1);
			array[0] = right;
			return array;
		}
		if (right == null) {
			T[] array = (T[]) Array.newInstance(left.getClass(), 1);
			array[0] = left;
			return array;
		}
		T[] array = (T[]) Array.newInstance(left.getClass(), 2);
		array[0] = left;
		array[1] = right;
		return array;
	}

	public static Object[] list(Object left) {
		if (left == null)
			return null;
		return new Object[] { left };
	}

	public static <K, V> Map<K, V> list(Entry<K, V> left) {
		if (left == null)
			return null;
		Map<K, V> map = new HashMap<K, V>();
		map.put(left.getKey(), left.getValue());
		return map;
	}

	public String[] seq(String left, String right) {
		if (importSequences != null) {
			for (StringSequence sequence : importSequences) {
				if (sequence.containSequence(left, right)) {
					return sequence.getSequence(left, right).toArray(new String[0]);
				}
			}
		}
		throw new IllegalStateException("No such sequence from \"" + left + "\" to \"" + right + "\".");
	}

	public static char[] seq(char left, char right) {
		return CollectionUtils.createSequence(left, right);
	}

	public static byte[] seq(byte left, byte right) {
		return CollectionUtils.createSequence(left, right);
	}

	public static short[] seq(short left, short right) {
		return CollectionUtils.createSequence(left, right);
	}

	public static int[] seq(int left, int right) {
		return CollectionUtils.createSequence(left, right);
	}

	public static long[] seq(long left, long right) {
		return CollectionUtils.createSequence(left, right);
	}

	public static float[] seq(float left, float right) {
		return CollectionUtils.createSequence(left, right);
	}

	public static double[] seq(double left, double right) {
		return CollectionUtils.createSequence(left, right);
	}

	public static boolean index(boolean[] left, int right) {
		return left[right];
	}

	public static char index(char[] left, int right) {
		return left[right];
	}

	public static byte index(byte[] left, int right) {
		return left[right];
	}

	public static short index(short[] left, int right) {
		return left[right];
	}

	public static int index(int[] left, int right) {
		return left[right];
	}

	public static long index(long[] left, int right) {
		return left[right];
	}

	public static float index(float[] left, int right) {
		return left[right];
	}

	public static double index(double[] left, int right) {
		return left[right];
	}

	public static <T> T index(T[] left, int right) {
		return left[right];
	}

	public static <T> T index(List<T> left, int right) {
		return left.get(right);
	}

	public static <K, V> V index(Map<K, V> left, K right) {
		return left.get(right);
	}

	public static <K, V> Entry<K, V> entry(K left, V right) {
		return new MapEntry<K, V>(left, right);
	}

	public static Object select(boolean left, Entry<Object, Object> right) {
		return left ? right.getKey() : right.getValue();
	}

	public static Object select(Object left, Entry<Object, Object> right) {
		return ClassUtils.isTrue(left) ? right.getKey() : right.getValue();
	}

	public static Object $new(Class<?> left) {
		if (left == null)
			return null;
		try {
			return left.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Class<T> left, Object right) {
		return (T) right;
	}

}