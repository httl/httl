package httl.spi.methods;

import httl.util.ClassUtils;
import httl.util.CollectionUtils;
import httl.util.MapEntry;
import httl.util.StringSequence;
import httl.util.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

	public static int add(Number left, byte right) {
		if (left == null)
			return right;
		return left.byteValue() + right;
	}

	public static int add(byte left, Number right) {
		if (right == null)
			return left;
		return left + right.byteValue();
	}

	public static Object add(String left, byte right) {
		if (left == null) {
			if (! StringUtils.isNumber(left))
				return left + right;
			else
				return right;
		}
		return Byte.parseByte(left) + right;
	}

	public static Object add(byte left, String right) {
		if (right == null) {
			if (! StringUtils.isNumber(right))
				return left + right;
			else
				return left;
		}
		return left + Byte.parseByte(right);
	}

	public static Object add(Object left, byte right) {
		if (left == null)
			return right;
		if (left instanceof Number)
			return add((Number) left, right);
		if (left instanceof String)
			return add((String) left, right);
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(byte left, Object right) {
		if (right == null)
			return left;
		if (right instanceof Number)
			return add(left, (Number) right);
		if (right instanceof String)
			return add(left, (String) right);
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static Integer add(Byte left, Byte right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left + right;
	}

	public static Integer add(Number left, Byte right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left.byteValue() + right;
	}

	public static Integer add(Byte left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left + right.byteValue();
	}

	public static Object add(Object left, Byte right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (left instanceof Number)
			return ((Number) left).byteValue() + right;
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(Byte left, Object right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (right instanceof Number)
			return left + ((Number) right).byteValue();
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static int add(short left, short right) {
		return left + right;
	}

	public static int add(Number left, short right) {
		if (left == null)
			return right;
		return left.shortValue() + right;
	}

	public static int add(short left, Number right) {
		if (right == null)
			return left;
		return left + right.shortValue();
	}

	public static Object add(String left, short right) {
		if (left == null) {
			if (! StringUtils.isNumber(left))
				return left + right;
			else
				return right;
		}
		return Short.parseShort(left) + right;
	}

	public static Object add(short left, String right) {
		if (right == null) {
			if (! StringUtils.isNumber(right))
				return left + right;
			else
				return left;
		}
		return left + Short.parseShort(right);
	}

	public static Object add(Object left, short right) {
		if (left == null)
			return right;
		if (left instanceof Number)
			return add((Number) left, right);
		if (left instanceof String)
			return add((String) left, right);
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(short left, Object right) {
		if (right == null)
			return left;
		if (right instanceof Number)
			return add(left, (Number) right);
		if (right instanceof String)
			return add(left, (String) right);
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static Integer add(Short left, Short right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left + right;
	}

	public static Integer add(Number left, Short right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left.shortValue() + right;
	}

	public static Integer add(Short left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left + right.shortValue();
	}

	public static Object add(Object left, Short right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (left instanceof Number)
			return ((Number) left).shortValue() + right;
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(Short left, Object right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (right instanceof Number)
			return left + ((Number) right).shortValue();
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static int add(int left, int right) {
		return left + right;
	}

	public static int add(Number left, int right) {
		if (left == null)
			return right;
		return left.intValue() + right;
	}

	public static int add(int left, Number right) {
		if (right == null)
			return left;
		return left + right.intValue();
	}

	public static Object add(String left, int right) {
		if (left == null) {
			if (! StringUtils.isNumber(left))
				return left + right;
			else
				return right;
		}
		return Integer.parseInt(left) + right;
	}

	public static Object add(int left, String right) {
		if (right == null) {
			if (! StringUtils.isNumber(right))
				return left + right;
			else
				return left;
		}
		return left + Integer.parseInt(right);
	}

	public static Object add(Object left, int right) {
		if (left == null)
			return right;
		if (left instanceof Number)
			return add((Number) left, right);
		if (left instanceof String)
			return add((String) left, right);
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(int left, Object right) {
		if (right == null)
			return left;
		if (right instanceof Number)
			return add(left, (Number) right);
		if (right instanceof String)
			return add(left, (String) right);
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static Integer add(Integer left, Integer right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null)
			return right;
		if (right == null)
			return left;
		return left + right;
	}

	public static Integer add(Number left, Integer right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left.intValue();
		return left.intValue() + right;
	}

	public static Integer add(Integer left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left;
		return left + right.intValue();
	}

	public static Object add(Object left, Integer right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (left instanceof Number)
			return ((Number) left).intValue() + right;
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(Integer left, Object right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (right instanceof Number)
			return left + ((Number) right).intValue();
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static long add(long left, long right) {
		return left + right;
	}

	public static long add(Number left, long right) {
		if (left == null)
			return right;
		return left.longValue() + right;
	}

	public static long add(long left, Number right) {
		if (right == null)
			return left;
		return left + right.longValue();
	}

	public static Object add(String left, long right) {
		if (left == null) {
			if (! StringUtils.isNumber(left))
				return left + right;
			else
				return right;
		}
		return Long.parseLong(left) + right;
	}

	public static Object add(long left, String right) {
		if (right == null) {
			if (! StringUtils.isNumber(right))
				return left + right;
			else
				return left;
		}
		return left + Long.parseLong(right);
	}

	public static Object add(Object left, long right) {
		if (left == null)
			return right;
		if (left instanceof Number)
			return add((Number) left, right);
		if (left instanceof String)
			return add((String) left, right);
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(long left, Object right) {
		if (right == null)
			return left;
		if (right instanceof Number)
			return add(left, (Number) right);
		if (right instanceof String)
			return add(left, (String) right);
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static Long add(Long left, Long right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null)
			return right;
		if (right == null)
			return left;
		return left + right;
	}

	public static Long add(Number left, Long right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left.longValue();
		return left.longValue() + right;
	}

	public static Long add(Long left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.longValue();
		if (right == null)
			return left;
		return left + right.longValue();
	}

	public static Object add(Object left, Long right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (left instanceof Number)
			return ((Number) left).longValue() + right;
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(Long left, Object right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (right instanceof Number)
			return left + ((Number) right).longValue();
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static float add(float left, float right) {
		return left + right;
	}

	public static float add(Number left, float right) {
		if (left == null)
			return right;
		return left.floatValue() + right;
	}

	public static float add(float left, Number right) {
		if (right == null)
			return left;
		return left + right.floatValue();
	}

	public static Object add(String left, float right) {
		if (left == null) {
			if (! StringUtils.isNumber(left))
				return left + right;
			else
				return right;
		}
		return Float.parseFloat(left) + right;
	}

	public static Object add(float left, String right) {
		if (right == null) {
			if (! StringUtils.isNumber(right))
				return left + right;
			else
				return left;
		}
		return left + Float.parseFloat(right);
	}

	public static Object add(Object left, float right) {
		if (left == null)
			return right;
		if (left instanceof Number)
			return add((Number) left, right);
		if (left instanceof String)
			return add((String) left, right);
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(float left, Object right) {
		if (right == null)
			return left;
		if (right instanceof Number)
			return add(left, (Number) right);
		if (right instanceof String)
			return add(left, (String) right);
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static Float add(Float left, Float right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null)
			return right;
		if (right == null)
			return left;
		return left + right;
	}

	public static Float add(Number left, Float right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left.floatValue();
		return left.floatValue() + right;
	}

	public static Float add(Float left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.floatValue();
		if (right == null)
			return left;
		return left + right.floatValue();
	}

	public static Object add(Object left, Float right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (left instanceof Number)
			return ((Number) left).floatValue() + right;
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(Float left, Object right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (right instanceof Number)
			return left + ((Number) right).floatValue();
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static double add(double left, double right) {
		return left + right;
	}

	public static double add(Number left, double right) {
		if (left == null)
			return right;
		return left.doubleValue() + right;
	}

	public static double add(double left, Number right) {
		if (right == null)
			return left;
		return left + right.doubleValue();
	}

	public static Object add(String left, double right) {
		if (left == null) {
			if (! StringUtils.isNumber(left))
				return left + right;
			else
				return right;
		}
		return Double.parseDouble(left) + right;
	}

	public static Object add(double left, String right) {
		if (right == null) {
			if (! StringUtils.isNumber(right))
				return left + right;
			else
				return left;
		}
		return left + Double.parseDouble(right);
	}

	public static Object add(Object left, double right) {
		if (left == null)
			return right;
		if (left instanceof Number)
			return add((Number) left, right);
		if (left instanceof String)
			return add((String) left, right);
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(double left, Object right) {
		if (right == null)
			return left;
		if (right instanceof Number)
			return add(left, (Number) right);
		if (right instanceof String)
			return add(left, (String) right);
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static Double add(Double left, Double right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null)
			return right;
		if (right == null)
			return left;
		return left + right;
	}

	public static Double add(Number left, Double right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left.doubleValue();
		return left.doubleValue() + right;
	}

	public static Double add(Double left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.doubleValue();
		if (right == null)
			return left;
		return left + right.doubleValue();
	}

	public static Object add(Object left, Double right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (left instanceof Number)
			return ((Number) left).doubleValue() + right;
		return StringUtils.valueOf(left) + String.valueOf(right);
	}

	public static Object add(Double left, Object right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		if (right instanceof Number)
			return left + ((Number) right).doubleValue();
		return String.valueOf(left) + StringUtils.valueOf(right);
	}

	public static String add(String left, String right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null) {
			return right;
		}
		if (right == null) {
			return left;
		}
		return left + right;
	}

	public static String add(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		}
		if (left == null) {
			return StringUtils.valueOf(right);
		}
		if (right == null) {
			return StringUtils.valueOf(left);
		}
		return StringUtils.valueOf(left) + StringUtils.valueOf(right);
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

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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

	public static int sub(Number left, byte right) {
		if (left == null)
			return - right;
		return left.byteValue() - right;
	}

	public static int sub(byte left, Number right) {
		if (right == null)
			return left;
		return left - right.byteValue();
	}

	public static int sub(String left, byte right) {
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		return Byte.parseByte(left) - right;
	}

	public static int sub(byte left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left - Byte.parseByte(right);
	}

	public static int sub(Object left, byte right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return - right;
	}

	public static int sub(byte left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static Integer sub(Byte left, Byte right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left.intValue();
		return left - right;
	}

	public static Integer sub(Number left, Byte right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left.intValue();
		return left.byteValue() - right;
	}

	public static Integer sub(Byte left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right.byteValue();
		if (right == null)
			return left.intValue();
		return left - right.byteValue();
	}

	public static Integer sub(String left, Byte right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		if (right == null)
			return Integer.parseInt(left);
		return Byte.parseByte(left) - right;
	}

	public static Integer sub(Byte left, String right) {
		if (left == null && right == null)
			return null;
		if (right == null || ! StringUtils.isNumber(right))
			return left.intValue();
		if (left == null)
			return Integer.parseInt(right);
		return left - Byte.parseByte(right);
	}

	public static Integer sub(Object left, Byte right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return right == null ? null : - right;
	}

	public static Integer sub(Byte left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left == null ? null : left.intValue();
	}

	public static int sub(short left, short right) {
		return left - right;
	}

	public static int sub(Number left, short right) {
		if (left == null)
			return - right;
		return left.shortValue() - right;
	}

	public static int sub(short left, Number right) {
		if (right == null)
			return left;
		return left - right.shortValue();
	}

	public static int sub(String left, short right) {
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		return Short.parseShort(left) - right;
	}

	public static int sub(short left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left - Short.parseShort(right);
	}

	public static int sub(Object left, short right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return - right;
	}

	public static int sub(short left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static Integer sub(Short left, Short right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left.intValue();
		return left - right;
	}

	public static Integer sub(Number left, Short right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left.intValue();
		return left.shortValue() - right;
	}

	public static Integer sub(Short left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right.intValue();
		if (right == null)
			return left.intValue();
		return left - right.shortValue();
	}

	public static Integer sub(String left, Short right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		if (right == null)
			return Integer.parseInt(left);
		return Short.parseShort(left) - right;
	}

	public static Integer sub(Short left, String right) {
		if (left == null && right == null)
			return null;
		if (right == null || ! StringUtils.isNumber(right))
			return left.intValue();
		if (left == null)
			return Integer.parseInt(right);
		return left - Short.parseShort(right);
	}

	public static Integer sub(Object left, Short right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return right == null ? null : - right;
	}

	public static Integer sub(Short left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left == null ? null : left.intValue();
	}

	public static int sub(int left, int right) {
		return left - right;
	}

	public static int sub(Number left, int right) {
		if (left == null)
			return - right;
		return left.intValue() - right;
	}

	public static int sub(int left, Number right) {
		if (right == null)
			return left;
		return left - right.intValue();
	}

	public static int sub(String left, int right) {
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		return Integer.parseInt(left) - right;
	}

	public static int sub(int left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left - Integer.parseInt(right);
	}

	public static int sub(Object left, int right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return - right;
	}

	public static int sub(int left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static Integer sub(Integer left, Integer right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left;
		return left - right;
	}

	public static Integer sub(Number left, Integer right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left.intValue();
		return left.intValue() - right;
	}

	public static Integer sub(Integer left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right.intValue();
		if (right == null)
			return left;
		return left - right.intValue();
	}

	public static Integer sub(String left, Integer right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		if (right == null)
			return Integer.parseInt(left);
		return Integer.parseInt(left) - right;
	}

	public static Integer sub(Integer left, String right) {
		if (left == null && right == null)
			return null;
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		if (left == null)
			return Integer.parseInt(right);
		return left - Integer.parseInt(right);
	}

	public static Integer sub(Object left, Integer right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return right == null ? null : - right;
	}

	public static Integer sub(Integer left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static long sub(long left, long right) {
		return left - right;
	}

	public static long sub(Number left, long right) {
		if (left == null)
			return - right;
		return left.longValue() - right;
	}

	public static long sub(long left, Number right) {
		if (right == null)
			return left;
		return left - right.longValue();
	}

	public static long sub(String left, long right) {
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		return Long.parseLong(left) - right;
	}

	public static long sub(long left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left - Long.parseLong(right);
	}

	public static long sub(Object left, long right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return - right;
	}

	public static long sub(long left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static Long sub(Long left, Long right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left;
		return left - right;
	}

	public static Long sub(Number left, Long right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left.longValue();
		return left.longValue() - right;
	}

	public static Long sub(Long left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right.longValue();
		if (right == null)
			return left;
		return left - right.longValue();
	}

	public static Long sub(String left, Long right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		if (right == null)
			return Long.parseLong(left);
		return Long.parseLong(left) - right;
	}

	public static Long sub(Long left, String right) {
		if (left == null && right == null)
			return null;
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		if (left == null)
			return Long.parseLong(right);
		return left - Long.parseLong(right);
	}

	public static Long sub(Object left, Long right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return right == null ? null : - right;
	}

	public static Long sub(Long left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static float sub(float left, float right) {
		return left - right;
	}

	public static float sub(Number left, float right) {
		if (left == null)
			return - right;
		return left.floatValue() - right;
	}

	public static float sub(float left, Number right) {
		if (right == null)
			return left;
		return left - right.floatValue();
	}

	public static float sub(String left, float right) {
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		return Float.parseFloat(left) - right;
	}

	public static float sub(float left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left - Float.parseFloat(right);
	}

	public static float sub(Object left, float right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return - right;
	}

	public static float sub(float left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static Float sub(Float left, Float right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left;
		return left - right;
	}

	public static Float sub(Number left, Float right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left.floatValue();
		return left.floatValue() - right;
	}

	public static Float sub(Float left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right.floatValue();
		if (right == null)
			return left;
		return left - right.floatValue();
	}

	public static Float sub(String left, Float right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		if (right == null)
			return Float.parseFloat(left);
		return Float.parseFloat(left) - right;
	}

	public static Float sub(Float left, String right) {
		if (left == null && right == null)
			return null;
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		if (left == null)
			return Float.parseFloat(right);
		return left - Float.parseFloat(right);
	}

	public static Float sub(Object left, Float right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return right == null ? null : - right;
	}

	public static Float sub(Float left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static double sub(double left, double right) {
		return left - right;
	}

	public static double sub(Number left, double right) {
		if (left == null)
			return - right;
		return left.doubleValue() - right;
	}

	public static double sub(double left, Number right) {
		if (right == null)
			return left;
		return left - right.doubleValue();
	}

	public static double sub(String left, double right) {
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		return Double.parseDouble(left) - right;
	}

	public static double sub(double left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left - Double.parseDouble(right);
	}

	public static double sub(Object left, double right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return - right;
	}

	public static double sub(double left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static Double sub(Double left, Double right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left;
		return left - right;
	}

	public static Double sub(Number left, Double right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right;
		if (right == null)
			return left.doubleValue();
		return left.doubleValue() - right;
	}

	public static Double sub(Double left, Number right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return - right.doubleValue();
		if (right == null)
			return left;
		return left - right.doubleValue();
	}

	public static Double sub(String left, Double right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return - right;
		if (right == null)
			return Double.parseDouble(left);
		return Double.parseDouble(left) - right;
	}

	public static Double sub(Double left, String right) {
		if (left == null && right == null)
			return null;
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		if (left == null)
			return Double.parseDouble(right);
		return left - Double.parseDouble(right);
	}

	public static Double sub(Object left, Double right) {
		if (left instanceof Number)
			return sub((Number) left, right);
		if (left instanceof String)
			return sub((String) left, right);
		return right == null ? null : - right;
	}

	public static Double sub(Double left, Object right) {
		if (right instanceof Number)
			return sub(left, (Number) right);
		if (right instanceof String)
			return sub(left, (String) right);
		return left;
	}

	public static int mul(byte left, byte right) {
		return left * right;
	}

	public static int mul(Number left, byte right) {
		if (left == null)
			return right;
		return left.byteValue() * right;
	}

	public static int mul(byte left, Number right) {
		return mul(right, left);
	}

	public static int mul(String left, byte right) {
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		return Integer.parseInt(left) * right;
	}

	public static int mul(byte left, String right) {
		return mul(right, left);
	}

	public static int mul(Object left, byte right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static int mul(byte left, Object right) {
		return mul(right, left);
	}

	public static Integer mul(Byte left, Byte right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left * right;
	}

	public static Integer mul(Number left, Byte right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left.byteValue() * right;
	}

	public static Integer mul(Byte left, Number right) {
		return mul(right, left);
	}

	public static Integer mul(String left, Byte right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return right.intValue();
		if (right == null)
			return Integer.parseInt(left);
		return Integer.parseInt(left) * right;
	}

	public static Integer mul(Byte left, String right) {
		return mul(right, left);
	}

	public static Integer mul(Object left, Byte right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right == null ? null : right.intValue();
	}

	public static Integer mul(Byte left, Object right) {
		return mul(right, left);
	}

	public static int mul(short left, short right) {
		return left * right;
	}

	public static int mul(Number left, short right) {
		if (left == null)
			return right;
		return left.shortValue() * right;
	}

	public static int mul(short left, Number right) {
		return mul(right, left);
	}

	public static int mul(String left, short right) {
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		return Integer.parseInt(left) * right;
	}

	public static int mul(short left, String right) {
		return mul(right, left);
	}

	public static int mul(Object left, short right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static int mul(short left, Object right) {
		return mul(right, left);
	}

	public static Integer mul(Short left, Short right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left * right;
	}

	public static Integer mul(Number left, Short right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right.intValue();
		if (right == null)
			return left.intValue();
		return left.shortValue() * right;
	}

	public static Integer mul(Short left, Number right) {
		return mul(right, left);
	}

	public static Integer mul(String left, Short right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return right.intValue();
		if (right == null)
			return Integer.parseInt(left);
		return Integer.parseInt(left) * right;
	}

	public static Integer mul(Short left, String right) {
		return mul(right, left);
	}

	public static Integer mul(Object left, Short right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right == null ? null : right.intValue();
	}

	public static Integer mul(Short left, Object right) {
		return mul(right, left);
	}

	public static int mul(int left, int right) {
		return left * right;
	}

	public static int mul(Number left, int right) {
		if (left == null)
			return right;
		return left.intValue() * right;
	}

	public static int mul(int left, Number right) {
		return mul(right, left);
	}

	public static int mul(String left, int right) {
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		return Integer.parseInt(left) * right;
	}

	public static int mul(int left, String right) {
		return mul(right, left);
	}

	public static int mul(Object left, int right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static int mul(int left, Object right) {
		return mul(right, left);
	}

	public static Integer mul(Integer left, Integer right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		return left * right;
	}

	public static Integer mul(Number left, Integer right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left.intValue();
		return left.intValue() * right;
	}

	public static Integer mul(Integer left, Number right) {
		return mul(right, left);
	}

	public static Integer mul(String left, Integer right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		if (right == null)
			return Integer.parseInt(left);
		return Integer.parseInt(left) * right;
	}

	public static Integer mul(Integer left, String right) {
		return mul(right, left);
	}

	public static Integer mul(Object left, Integer right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static Integer mul(Integer left, Object right) {
		return mul(right, left);
	}

	public static long mul(long left, long right) {
		return left * right;
	}

	public static long mul(Number left, long right) {
		if (left == null)
			return right;
		return left.longValue() * right;
	}

	public static long mul(long left, Number right) {
		return mul(right, left);
	}

	public static long mul(String left, long right) {
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		return Long.parseLong(left) * right;
	}

	public static long mul(long left, String right) {
		return mul(right, left);
	}

	public static long mul(Object left, long right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static long mul(long left, Object right) {
		return mul(right, left);
	}

	public static Long mul(Long left, Long right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		return left * right;
	}

	public static Long mul(Number left, Long right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left.longValue();
		return left.longValue() * right;
	}

	public static Long mul(Long left, Number right) {
		return mul(right, left);
	}

	public static Long mul(String left, Long right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		if (right == null)
			return Long.parseLong(left);
		return Long.parseLong(left) * right;
	}

	public static Long mul(Long left, String right) {
		return mul(right, left);
	}

	public static Long mul(Object left, Long right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static Long mul(Long left, Object right) {
		return mul(right, left);
	}

	public static float mul(float left, float right) {
		return left * right;
	}

	public static float mul(Number left, float right) {
		if (left == null)
			return right;
		return left.floatValue() * right;
	}

	public static float mul(float left, Number right) {
		return mul(right, left);
	}

	public static float mul(String left, float right) {
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		return Float.parseFloat(left) * right;
	}

	public static float mul(float left, String right) {
		return mul(right, left);
	}

	public static float mul(Object left, float right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static float mul(float left, Object right) {
		return mul(right, left);
	}

	public static Float mul(Float left, Float right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		return left * right;
	}

	public static Float mul(Number left, Float right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left.floatValue();
		return left.floatValue() * right;
	}

	public static Float mul(Float left, Number right) {
		return mul(right, left);
	}

	public static Float mul(String left, Float right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		if (right == null)
			return Float.parseFloat(left);
		return Float.parseFloat(left) * right;
	}

	public static Float mul(Float left, String right) {
		return mul(right, left);
	}

	public static Float mul(Object left, Float right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static Float mul(Float left, Object right) {
		return mul(right, left);
	}

	public static double mul(double left, double right) {
		return left * right;
	}

	public static double mul(Number left, double right) {
		if (left == null)
			return right;
		return left.doubleValue() * right;
	}

	public static double mul(double left, Number right) {
		return mul(right, left);
	}

	public static double mul(String left, double right) {
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		return Double.parseDouble(left) * right;
	}

	public static double mul(double left, String right) {
		return mul(right, left);
	}

	public static double mul(Object left, double right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static double mul(double left, Object right) {
		return mul(right, left);
	}

	public static Double mul(Double left, Double right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left;
		return left * right;
	}

	public static Double mul(Number left, Double right) {
		if (left == null && right == null)
			return null;
		if (left == null)
			return right;
		if (right == null)
			return left.doubleValue();
		return left.doubleValue() * right;
	}

	public static Double mul(Double left, Number right) {
		return mul(right, left);
	}

	public static Double mul(String left, Double right) {
		if (left == null && right == null)
			return null;
		if (left == null || ! StringUtils.isNumber(left))
			return right;
		if (right == null)
			return Double.parseDouble(left);
		return Double.parseDouble(left) * right;
	}

	public static Double mul(Double left, String right) {
		return mul(right, left);
	}

	public static Double mul(Object left, Double right) {
		if (left instanceof Number)
			return mul((Number) left, right);
		if (left instanceof String)
			return mul((String) left, right);
		return right;
	}

	public static Double mul(Double left, Object right) {
		return mul(right, left);
	}

	public static int div(byte left, byte right) {
		if (right == 0)
			return 0;
		return left / right;
	}

	public static int div(Number left, byte right) {
		if (left == null || right == 0)
			return right;
		return left.byteValue() / right;
	}

	public static int div(byte left, Number right) {
		if (right == null || right.byteValue() == 0)
			return left;
		return left / right.byteValue();
	}

	public static int div(String left, byte right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0)
			return 0;
		return Integer.parseInt(left) / right;
	}

	public static int div(byte left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left / Integer.parseInt(right);
	}

	public static int div(Object left, byte right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0;
	}

	public static int div(byte left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0;
	}

	public static Integer div(Byte left, Byte right) {
		if (left == null || right == null || right.byteValue() == 0)
			return 0;
		return left / right;
	}

	public static Integer div(Number left, Byte right) {
		if (left == null || right == null || right.byteValue() == 0)
			return 0;
		return left.byteValue() / right;
	}

	public static Integer div(Byte left, Number right) {
		if (left == null || right == null || right.byteValue() == 0)
			return 0;
		return left / right.byteValue();
	}

	public static Integer div(String left, Byte right) {
		if (left == null || right == null || right.byteValue() == 0)
			return 0;
		return Integer.parseInt(left) / right;
	}

	public static Integer div(Byte left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0;
		int value = Integer.parseInt(right);
		if (value == 0)
			return 0;
		return left / value;
	}

	public static Integer div(Object left, Byte right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0;
	}

	public static Integer div(Byte left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0;
	}

	public static int div(short left, short right) {
		if (right == 0)
			return 0;
		return left / right;
	}

	public static int div(Number left, short right) {
		if (left == null || right == 0)
			return right;
		return left.shortValue() / right;
	}

	public static int div(short left, Number right) {
		if (right == null || right.shortValue() == 0)
			return left;
		return left / right.shortValue();
	}

	public static int div(String left, short right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0)
			return 0;
		return Integer.parseInt(left) / right;
	}

	public static int div(short left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left / Integer.parseInt(right);
	}

	public static int div(Object left, short right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0;
	}

	public static int div(short left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0;
	}

	public static Integer div(Short left, Short right) {
		if (left == null || right == null || right.shortValue() == 0)
			return 0;
		return left / right;
	}

	public static Integer div(Number left, Short right) {
		if (left == null || right == null || right.shortValue() == 0)
			return 0;
		return left.shortValue() / right;
	}

	public static Integer div(Short left, Number right) {
		if (left == null || right == null || right.shortValue() == 0)
			return 0;
		return left / right.shortValue();
	}

	public static Integer div(String left, Short right) {
		if (left == null || right == null || right.shortValue() == 0)
			return 0;
		return Integer.parseInt(left) / right;
	}

	public static Integer div(Short left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0;
		int value = Integer.parseInt(right);
		if (value == 0)
			return 0;
		return left / value;
	}

	public static Integer div(Object left, Short right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0;
	}

	public static Integer div(Short left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0;
	}

	public static int div(int left, int right) {
		if (right == 0)
			return 0;
		return left / right;
	}

	public static int div(Number left, int right) {
		if (left == null || right == 0)
			return right;
		return left.intValue() / right;
	}

	public static int div(int left, Number right) {
		if (right == null || right.intValue() == 0)
			return left;
		return left / right.intValue();
	}

	public static int div(String left, int right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0)
			return 0;
		return Integer.parseInt(left) / right;
	}

	public static int div(int left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left / Integer.parseInt(right);
	}

	public static int div(Object left, int right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0;
	}

	public static int div(int left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0;
	}

	public static Integer div(Integer left, Integer right) {
		if (left == null || right == null || right.intValue() == 0)
			return 0;
		return left / right;
	}

	public static Integer div(Number left, Integer right) {
		if (left == null || right == null || right.intValue() == 0)
			return 0;
		return left.intValue() / right;
	}

	public static Integer div(Integer left, Number right) {
		if (left == null || right == null || right.intValue() == 0)
			return 0;
		return left / right.intValue();
	}

	public static Integer div(String left, Integer right) {
		if (left == null || right == null || right.intValue() == 0)
			return 0;
		return Integer.parseInt(left) / right;
	}

	public static Integer div(Integer left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0;
		int value = Integer.parseInt(right);
		if (value == 0)
			return 0;
		return left / value;
	}

	public static Integer div(Object left, Integer right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0;
	}

	public static Integer div(Integer left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0;
	}

	public static long div(long left, long right) {
		if (right == 0L)
			return 0L;
		return left / right;
	}

	public static long div(Number left, long right) {
		if (left == null || right == 0L)
			return right;
		return left.longValue() / right;
	}

	public static long div(long left, Number right) {
		if (right == null || right.longValue() == 0L)
			return left;
		return left / right.longValue();
	}

	public static long div(String left, long right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0L)
			return 0L;
		return Long.parseLong(left) / right;
	}

	public static long div(long left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left / Long.parseLong(right);
	}

	public static long div(Object left, long right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0L;
	}

	public static long div(long left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0L;
	}

	public static Long div(Long left, Long right) {
		if (left == null || right == null || right.longValue() == 0L)
			return 0L;
		return left / right;
	}

	public static Long div(Number left, Long right) {
		if (left == null || right == null || right.longValue() == 0L)
			return 0L;
		return left.longValue() / right;
	}

	public static Long div(Long left, Number right) {
		if (left == null || right == null || right.longValue() == 0L)
			return 0L;
		return left / right.longValue();
	}

	public static Long div(String left, Long right) {
		if (left == null || right == null || right.longValue() == 0L)
			return 0L;
		return Long.parseLong(left) / right;
	}

	public static Long div(Long left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0L;
		long value = Long.parseLong(right);
		if (value == 0L)
			return 0L;
		return left / value;
	}

	public static Long div(Object left, Long right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0L;
	}

	public static Long div(Long left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0L;
	}

	public static float div(float left, float right) {
		if (right == 0F)
			return 0F;
		return left / right;
	}

	public static float div(Number left, float right) {
		if (left == null || right == 0F)
			return right;
		return left.floatValue() / right;
	}

	public static float div(float left, Number right) {
		if (right == null || right.floatValue() == 0F)
			return left;
		return left / right.floatValue();
	}

	public static float div(String left, float right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0F)
			return 0F;
		return Float.parseFloat(left) / right;
	}

	public static float div(float left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left / Float.parseFloat(right);
	}

	public static float div(Object left, float right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0F;
	}

	public static float div(float left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0F;
	}

	public static Float div(Float left, Float right) {
		if (left == null || right == null || right.floatValue() == 0F)
			return 0F;
		return left / right;
	}

	public static Float div(Number left, Float right) {
		if (left == null || right == null || right.floatValue() == 0F)
			return 0F;
		return left.floatValue() / right;
	}

	public static Float div(Float left, Number right) {
		if (left == null || right == null || right.floatValue() == 0F)
			return 0F;
		return left / right.floatValue();
	}

	public static Float div(String left, Float right) {
		if (left == null || right == null || right.floatValue() == 0F)
			return 0F;
		return Float.parseFloat(left) / right;
	}

	public static Float div(Float left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0F;
		float value = Float.parseFloat(right);
		if (value == 0F)
			return 0F;
		return left / value;
	}

	public static Float div(Object left, Float right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0F;
	}

	public static Float div(Float left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0F;
	}

	public static double div(double left, double right) {
		if (right == 0D)
			return 0D;
		return left / right;
	}

	public static double div(Number left, double right) {
		if (left == null || right == 0D)
			return right;
		return left.doubleValue() / right;
	}

	public static double div(double left, Number right) {
		if (right == null || right.doubleValue() == 0D)
			return left;
		return left / right.doubleValue();
	}

	public static double div(String left, double right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0D)
			return 0D;
		return Double.parseDouble(left) / right;
	}

	public static double div(double left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left / Double.parseDouble(right);
	}

	public static double div(Object left, double right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0D;
	}

	public static double div(double left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0D;
	}

	public static Double div(Double left, Double right) {
		if (left == null || right == null || right.doubleValue() == 0D)
			return 0D;
		return left / right;
	}

	public static Double div(Number left, Double right) {
		if (left == null || right == null || right.doubleValue() == 0D)
			return 0D;
		return left.doubleValue() / right;
	}

	public static Double div(Double left, Number right) {
		if (left == null || right == null || right.doubleValue() == 0D)
			return 0D;
		return left / right.doubleValue();
	}

	public static Double div(String left, Double right) {
		if (left == null || right == null || right.doubleValue() == 0D)
			return 0D;
		return Double.parseDouble(left) / right;
	}

	public static Double div(Double left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0D;
		double value = Double.parseDouble(right);
		if (value == 0D)
			return 0D;
		return left / value;
	}

	public static Double div(Object left, Double right) {
		if (left instanceof Number)
			return div((Number) left, right);
		if (left instanceof String)
			return div((String) left, right);
		return 0D;
	}

	public static Double div(Double left, Object right) {
		if (right instanceof Number)
			return div(left, (Number) right);
		if (right instanceof String)
			return div(left, (String) right);
		return 0D;
	}

	public static int mod(byte left, byte right) {
		if (right == 0)
			return 0;
		return left % right;
	}

	public static int mod(Number left, byte right) {
		if (left == null || right == 0)
			return right;
		return left.byteValue() % right;
	}

	public static int mod(byte left, Number right) {
		if (right == null || right.byteValue() == 0)
			return left;
		return left % right.byteValue();
	}

	public static int mod(String left, byte right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0)
			return 0;
		return Integer.parseInt(left) % right;
	}

	public static int mod(byte left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left % Integer.parseInt(right);
	}

	public static int mod(Object left, byte right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0;
	}

	public static int mod(byte left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0;
	}

	public static Integer mod(Byte left, Byte right) {
		if (left == null || right == null || right.byteValue() == 0)
			return 0;
		return left % right;
	}

	public static Integer mod(Number left, Byte right) {
		if (left == null || right == null || right.byteValue() == 0)
			return 0;
		return left.byteValue() % right;
	}

	public static Integer mod(Byte left, Number right) {
		if (left == null || right == null || right.byteValue() == 0)
			return 0;
		return left % right.byteValue();
	}

	public static Integer mod(String left, Byte right) {
		if (left == null || right == null || right.byteValue() == 0)
			return 0;
		return Integer.parseInt(left) % right;
	}

	public static Integer mod(Byte left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0;
		int value = Integer.parseInt(right);
		if (value == 0)
			return 0;
		return left % value;
	}

	public static Integer mod(Object left, Byte right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0;
	}

	public static Integer mod(Byte left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0;
	}

	public static int mod(short left, short right) {
		if (right == 0)
			return 0;
		return left % right;
	}

	public static int mod(Number left, short right) {
		if (left == null || right == 0)
			return right;
		return left.shortValue() % right;
	}

	public static int mod(short left, Number right) {
		if (right == null || right.shortValue() == 0)
			return left;
		return left % right.shortValue();
	}

	public static int mod(String left, short right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0)
			return 0;
		return Integer.parseInt(left) % right;
	}

	public static int mod(short left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left % Integer.parseInt(right);
	}

	public static int mod(Object left, short right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0;
	}

	public static int mod(short left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0;
	}

	public static Integer mod(Short left, Short right) {
		if (left == null || right == null || right.shortValue() == 0)
			return 0;
		return left % right;
	}

	public static Integer mod(Number left, Short right) {
		if (left == null || right == null || right.shortValue() == 0)
			return 0;
		return left.shortValue() % right;
	}

	public static Integer mod(Short left, Number right) {
		if (left == null || right == null || right.shortValue() == 0)
			return 0;
		return left % right.shortValue();
	}

	public static Integer mod(String left, Short right) {
		if (left == null || right == null || right.shortValue() == 0)
			return 0;
		return Integer.parseInt(left) % right;
	}

	public static Integer mod(Short left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0;
		int value = Integer.parseInt(right);
		if (value == 0)
			return 0;
		return left % value;
	}

	public static Integer mod(Object left, Short right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0;
	}

	public static Integer mod(Short left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0;
	}

	public static int mod(int left, int right) {
		if (right == 0)
			return 0;
		return left % right;
	}

	public static int mod(Number left, int right) {
		if (left == null || right == 0)
			return right;
		return left.intValue() % right;
	}

	public static int mod(int left, Number right) {
		if (right == null || right.intValue() == 0)
			return left;
		return left % right.intValue();
	}

	public static int mod(String left, int right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0)
			return 0;
		return Integer.parseInt(left) % right;
	}

	public static int mod(int left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left % Integer.parseInt(right);
	}

	public static int mod(Object left, int right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0;
	}

	public static int mod(int left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0;
	}

	public static Integer mod(Integer left, Integer right) {
		if (left == null || right == null || right.intValue() == 0)
			return 0;
		return left % right;
	}

	public static Integer mod(Number left, Integer right) {
		if (left == null || right == null || right.intValue() == 0)
			return 0;
		return left.intValue() % right;
	}

	public static Integer mod(Integer left, Number right) {
		if (left == null || right == null || right.intValue() == 0)
			return 0;
		return left % right.intValue();
	}

	public static Integer mod(String left, Integer right) {
		if (left == null || right == null || right.intValue() == 0)
			return 0;
		return Integer.parseInt(left) % right;
	}

	public static Integer mod(Integer left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0;
		int value = Integer.parseInt(right);
		if (value == 0)
			return 0;
		return left % value;
	}

	public static Integer mod(Object left, Integer right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0;
	}

	public static Integer mod(Integer left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0;
	}

	public static long mod(long left, long right) {
		if (right == 0L)
			return 0L;
		return left % right;
	}

	public static long mod(Number left, long right) {
		if (left == null || right == 0L)
			return right;
		return left.longValue() % right;
	}

	public static long mod(long left, Number right) {
		if (right == null || right.longValue() == 0L)
			return left;
		return left % right.longValue();
	}

	public static long mod(String left, long right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0L)
			return 0L;
		return Long.parseLong(left) % right;
	}

	public static long mod(long left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left % Long.parseLong(right);
	}

	public static long mod(Object left, long right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0L;
	}

	public static long mod(long left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0L;
	}

	public static Long mod(Long left, Long right) {
		if (left == null || right == null || right.longValue() == 0L)
			return 0L;
		return left % right;
	}

	public static Long mod(Number left, Long right) {
		if (left == null || right == null || right.longValue() == 0L)
			return 0L;
		return left.longValue() % right;
	}

	public static Long mod(Long left, Number right) {
		if (left == null || right == null || right.longValue() == 0L)
			return 0L;
		return left % right.longValue();
	}

	public static Long mod(String left, Long right) {
		if (left == null || right == null || right.longValue() == 0L)
			return 0L;
		return Long.parseLong(left) % right;
	}

	public static Long mod(Long left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0L;
		long value = Long.parseLong(right);
		if (value == 0L)
			return 0L;
		return left % value;
	}

	public static Long mod(Object left, Long right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0L;
	}

	public static Long mod(Long left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0L;
	}

	public static float mod(float left, float right) {
		if (right == 0F)
			return 0F;
		return left % right;
	}

	public static float mod(Number left, float right) {
		if (left == null || right == 0F)
			return right;
		return left.floatValue() % right;
	}

	public static float mod(float left, Number right) {
		if (right == null || right.floatValue() == 0F)
			return left;
		return left % right.floatValue();
	}

	public static float mod(String left, float right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0F)
			return 0F;
		return Float.parseFloat(left) % right;
	}

	public static float mod(float left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left % Float.parseFloat(right);
	}

	public static float mod(Object left, float right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0F;
	}

	public static float mod(float left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0F;
	}

	public static Float mod(Float left, Float right) {
		if (left == null || right == null || right.floatValue() == 0F)
			return 0F;
		return left % right;
	}

	public static Float mod(Number left, Float right) {
		if (left == null || right == null || right.floatValue() == 0F)
			return 0F;
		return left.floatValue() % right;
	}

	public static Float mod(Float left, Number right) {
		if (left == null || right == null || right.floatValue() == 0F)
			return 0F;
		return left % right.floatValue();
	}

	public static Float mod(String left, Float right) {
		if (left == null || right == null || right.floatValue() == 0F)
			return 0F;
		return Float.parseFloat(left) % right;
	}

	public static Float mod(Float left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0F;
		float value = Float.parseFloat(right);
		if (value == 0F)
			return 0F;
		return left % value;
	}

	public static Float mod(Object left, Float right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0F;
	}

	public static Float mod(Float left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0F;
	}

	public static double mod(double left, double right) {
		if (right == 0D)
			return 0D;
		return left % right;
	}

	public static double mod(Number left, double right) {
		if (left == null || right == 0D)
			return right;
		return left.doubleValue() % right;
	}

	public static double mod(double left, Number right) {
		if (right == null || right.doubleValue() == 0D)
			return left;
		return left % right.doubleValue();
	}

	public static double mod(String left, double right) {
		if (left == null || ! StringUtils.isNumber(left) || right == 0D)
			return 0D;
		return Double.parseDouble(left) % right;
	}

	public static double mod(double left, String right) {
		if (right == null || ! StringUtils.isNumber(right))
			return left;
		return left % Double.parseDouble(right);
	}

	public static double mod(Object left, double right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0D;
	}

	public static double mod(double left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0D;
	}

	public static Double mod(Double left, Double right) {
		if (left == null || right == null || right.doubleValue() == 0D)
			return 0D;
		return left % right;
	}

	public static Double mod(Number left, Double right) {
		if (left == null || right == null || right.doubleValue() == 0D)
			return 0D;
		return left.doubleValue() % right;
	}

	public static Double mod(Double left, Number right) {
		if (left == null || right == null || right.doubleValue() == 0D)
			return 0D;
		return left % right.doubleValue();
	}

	public static Double mod(String left, Double right) {
		if (left == null || right == null || right.doubleValue() == 0D)
			return 0D;
		return Double.parseDouble(left) % right;
	}

	public static Double mod(Double left, String right) {
		if (left == null || right == null || ! StringUtils.isNumber(right))
			return 0D;
		double value = Double.parseDouble(right);
		if (value == 0D)
			return 0D;
		return left % value;
	}

	public static Double mod(Object left, Double right) {
		if (left instanceof Number)
			return mod((Number) left, right);
		if (left instanceof String)
			return mod((String) left, right);
		return 0D;
	}

	public static Double mod(Double left, Object right) {
		if (right instanceof Number)
			return mod(left, (Number) right);
		if (right instanceof String)
			return mod(left, (String) right);
		return 0D;
	}

	public static boolean eq(boolean left, boolean right) {
		return left == right;
	}

	public static boolean eq(char left, char right) {
		return left == right;
	}

	public static boolean eq(char left, Character right) {
		return right != null && left == right.charValue();
	}

	public static boolean eq(Character left, char right) {
		return left != null && left.charValue() == right;
	}

	public static boolean eq(char left, String right) {
		return right != null && right.length() == 1 && left == right.charAt(0);
	}

	public static boolean eq(String left, char right) {
		return left != null && left.length() == 1 && left.charAt(0) == right;
	}

	public static boolean eq(char left, Object right) {
		if (right instanceof Character)
			return eq(left, (Character) right);
		if (right instanceof String)
			return eq(left, (String) right);
		return false;
	}

	public static boolean eq(Object left, char right) {
		if (left instanceof Character)
			return eq((Character) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(byte left, byte right) {
		return left == right;
	}

	public static boolean eq(Number left, byte right) {
		return left != null && left.byteValue() == right;
	}

	public static boolean eq(byte left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(String left, byte right) {
		return StringUtils.isNumber(left) && eq(Byte.parseByte(left), right);
	}

	public static boolean eq(byte left, String right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, byte right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(byte left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(Byte left, Byte right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.equals(right);
	}

	public static boolean eq(Number left, Byte right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.byteValue() == right.byteValue();
	}

	public static boolean eq(Byte left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(Byte left, String right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return StringUtils.isNumber(right) && eq(left.byteValue(), Byte.parseByte(right));
	}

	public static boolean eq(String left, Byte right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, Byte right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(Byte left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(short left, short right) {
		return left == right;
	}

	public static boolean eq(Number left, short right) {
		return left != null && left.shortValue() == right;
	}

	public static boolean eq(short left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(String left, short right) {
		return StringUtils.isNumber(left) && eq(Short.parseShort(left), right);
	}

	public static boolean eq(short left, String right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, short right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(short left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(Short left, Short right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.equals(right);
	}

	public static boolean eq(Number left, Short right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.shortValue() == right.shortValue();
	}

	public static boolean eq(Short left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(Short left, String right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return StringUtils.isNumber(right) && eq(left.shortValue(), Short.parseShort(right));
	}

	public static boolean eq(String left, Short right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, Short right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(Short left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(int left, int right) {
		return left == right;
	}

	public static boolean eq(Number left, int right) {
		return left != null && left.intValue() == right;
	}

	public static boolean eq(int left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(String left, int right) {
		return StringUtils.isNumber(left) && eq(Integer.parseInt(left), right);
	}

	public static boolean eq(int left, String right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, int right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(int left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(Integer left, Integer right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.equals(right);
	}

	public static boolean eq(Number left, Integer right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.intValue() == right.intValue();
	}

	public static boolean eq(Integer left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(Integer left, String right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return StringUtils.isNumber(right) && eq(left.intValue(), Integer.parseInt(right));
	}

	public static boolean eq(String left, Integer right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, Integer right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(Integer left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(long left, long right) {
		return left == right;
	}

	public static boolean eq(Number left, long right) {
		return left != null && left.longValue() == right;
	}

	public static boolean eq(long left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(String left, long right) {
		return StringUtils.isNumber(left) && eq(Long.parseLong(left), right);
	}

	public static boolean eq(long left, String right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, long right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(long left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(Long left, Long right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.equals(right);
	}

	public static boolean eq(Number left, Long right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.longValue() == right.longValue();
	}

	public static boolean eq(Long left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(Long left, String right) {
		if (left == null && right == null)
			return true;
		if (left == null)
			return false;
		if (right == null)
			return false;
		return StringUtils.isNumber(right) && eq(left.longValue(), Long.parseLong(right));
	}

	public static boolean eq(String left, Long right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, Long right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(Long left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(float left, float right) {
		return left == right;
	}

	public static boolean eq(Number left, float right) {
		return left != null && left.floatValue() == right;
	}

	public static boolean eq(float left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(String left, float right) {
		return StringUtils.isNumber(left) && eq(Float.parseFloat(left), right);
	}

	public static boolean eq(float left, String right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, float right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(float left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(Float left, Float right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.equals(right);
	}

	public static boolean eq(Number left, Float right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.floatValue() == right.floatValue();
	}

	public static boolean eq(Float left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(Float left, String right) {
		if (left == null && right == null)
			return true;
		if (left == null)
			return false;
		if (right == null)
			return false;
		return StringUtils.isNumber(right) && eq(left.floatValue(), Float.parseFloat(right));
	}

	public static boolean eq(String left, Float right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, Float right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(Float left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(double left, double right) {
		return left == right;
	}

	public static boolean eq(Number left, double right) {
		return left != null && left.doubleValue() == right;
	}

	public static boolean eq(double left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(String left, double right) {
		return StringUtils.isNumber(left) && eq(Double.parseDouble(left), right);
	}

	public static boolean eq(double left, String right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, double right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(double left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(Double left, Double right) {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		return left.equals(right);
	}

	public static boolean eq(Number left, Double right) {
		if (left == null && right == null)
			return true;
		if (left == null)
			return false;
		if (right == null)
			return false;
		return left.doubleValue() == right.doubleValue();
	}

	public static boolean eq(Double left, Number right) {
		return eq(right, left);
	}

	public static boolean eq(Double left, String right) {
		if (left == null && right == null)
			return true;
		if (left == null)
			return false;
		if (right == null)
			return false;
		return StringUtils.isNumber(right) && eq(left.doubleValue(), Double.parseDouble(right));
	}

	public static boolean eq(String left, Double right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, Double right) {
		if (left instanceof Number)
			return eq((Number) left, right);
		if (left instanceof String)
			return eq((String) left, right);
		return false;
	}

	public static boolean eq(Double left, Object right) {
		return eq(right, left);
	}

	public static boolean eq(Object left, Object right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null || right == null) {
			return false;
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
		return ! eq(left, right);
	}

	public static boolean ne(Number left, byte right) {
		return ! eq(left, right);
	}

	public static boolean ne(byte left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, byte right) {
		return ! eq(left, right);
	}

	public static boolean ne(byte left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, byte right) {
		return ! eq(left, right);
	}

	public static boolean ne(byte left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(Byte left, Byte right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, Byte right) {
		return ! eq(left, right);
	}

	public static boolean ne(Byte left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(Byte left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, Byte right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, Byte right) {
		return ! eq(left, right);
	}

	public static boolean ne(Byte left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(short left, short right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, short right) {
		return ! eq(left, right);
	}

	public static boolean ne(short left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, short right) {
		return ! eq(left, right);
	}

	public static boolean ne(short left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, short right) {
		return ! eq(left, right);
	}

	public static boolean ne(short left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(Short left, Short right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, Short right) {
		return ! eq(left, right);
	}

	public static boolean ne(Short left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(Short left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, Short right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, Short right) {
		return ! eq(left, right);
	}

	public static boolean ne(Short left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(int left, int right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, int right) {
		return ! eq(left, right);
	}

	public static boolean ne(int left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, int right) {
		return ! eq(left, right);
	}

	public static boolean ne(int left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, int right) {
		return ! eq(left, right);
	}

	public static boolean ne(int left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(Integer left, Integer right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, Integer right) {
		return ! eq(left, right);
	}

	public static boolean ne(Integer left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(Integer left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, Integer right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, Integer right) {
		return ! eq(left, right);
	}

	public static boolean ne(Integer left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(long left, long right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, long right) {
		return ! eq(left, right);
	}

	public static boolean ne(long left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, long right) {
		return ! eq(left, right);
	}

	public static boolean ne(long left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, long right) {
		return ! eq(left, right);
	}

	public static boolean ne(long left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(Long left, Long right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, Long right) {
		return ! eq(left, right);
	}

	public static boolean ne(Long left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(Long left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, Long right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, Long right) {
		return ! eq(left, right);
	}

	public static boolean ne(Long left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(float left, float right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, float right) {
		return ! eq(left, right);
	}

	public static boolean ne(float left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, float right) {
		return ! eq(left, right);
	}

	public static boolean ne(float left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, float right) {
		return ! eq(left, right);
	}

	public static boolean ne(float left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(Float left, Float right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, Float right) {
		return ! eq(left, right);
	}

	public static boolean ne(Float left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(Float left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, Float right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, Float right) {
		return ! eq(left, right);
	}

	public static boolean ne(Float left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(double left, double right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, double right) {
		return ! eq(left, right);
	}

	public static boolean ne(double left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, double right) {
		return ! eq(left, right);
	}

	public static boolean ne(double left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, double right) {
		return ! eq(left, right);
	}

	public static boolean ne(double left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(Double left, Double right) {
		return ! eq(left, right);
	}

	public static boolean ne(Number left, Double right) {
		return ! eq(left, right);
	}

	public static boolean ne(Double left, Number right) {
		return ! eq(left, right);
	}

	public static boolean ne(Double left, String right) {
		return ! eq(left, right);
	}

	public static boolean ne(String left, Double right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, Double right) {
		return ! eq(left, right);
	}

	public static boolean ne(Double left, Object right) {
		return ! eq(left, right);
	}

	public static boolean ne(Object left, Object right) {
		return ! eq(left, right);
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

	public static int ls(byte left, byte right) {
		return left << right;
	}

	public static int ls(short left, short right) {
		return left << right;
	}

	public static int ls(int left, int right) {
		return left << right;
	}

	public static long ls(long left, long right) {
		return left << right;
	}

	public static int rs(byte left, byte right) {
		return left >> right;
	}

	public static int rs(short left, short right) {
		return left >> right;
	}

	public static int rs(int left, int right) {
		return left >> right;
	}

	public static long rs(long left, long right) {
		return left >> right;
	}

	public static int us(byte left, byte right) {
		return left >>> right;
	}

	public static int us(short left, short right) {
		return left >>> right;
	}

	public static int us(int left, int right) {
		return left >>> right;
	}

	public static long us(long left, long right) {
		return left >>> right;
	}
	
	public boolean is(Object left, String right) {
		return is(left, ClassUtils.forName(importPackages, right));
	}

	public static boolean is(Object left, Class<?> right) {
		return right.isInstance(left);
	}

	public static boolean is(Object left, Object right) {
		if (right instanceof String)
			return is(left, right);
		if (right instanceof Class<?>)
			return is(left, right);
		return right == null ? false : right.getClass().isInstance(left);
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
	
	public static List<Object> list(Object[] left) {
		if (left == null)
			return null;
		return Arrays.asList((Object[]) left);
	}

	public static List<Object> list(Object left) {
		if (left == null)
			return null;
		if (left instanceof Object[]) {
			return list(left);
		}
		return Arrays.asList(new Object[] { left });
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> map(Object[] left) {
		if (left == null)
			return null;
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		for (Object item : (Object[]) left) {
			if (item instanceof Entry) {
				Entry<Object, Object> entry = (Entry<Object, Object>) item;
				map.put(entry.getKey(), entry.getValue());
			} else {
				map.put(item, item);
			}
		}
		return map;
	}

	public static Map<Object, Object> map(Object left) {
		if (left == null)
			return null;
		if (left instanceof Object[]) {
			return map(left);
		}
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		map.put(left, left);
		return map;
	}

	public static <K, V> Map<K, V> map(Entry<K, V> left) {
		if (left == null)
			return null;
		Map<K, V> map = new LinkedHashMap<K, V>();
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

	public static boolean get(boolean[] left, int right) {
		return left[right];
	}

	public static char get(char[] left, int right) {
		return left[right];
	}

	public static byte get(byte[] left, int right) {
		return left[right];
	}

	public static short get(short[] left, int right) {
		return left[right];
	}

	public static int get(int[] left, int right) {
		return left[right];
	}

	public static long get(long[] left, int right) {
		return left[right];
	}

	public static float get(float[] left, int right) {
		return left[right];
	}

	public static double get(double[] left, int right) {
		return left[right];
	}

	public static <T> T get(T[] left, int right) {
		return left[right];
	}

	public static <T> T get(List<T> left, int right) {
		return left.get(right);
	}

	public static <K, V> V get(Map<K, V> left, K right) {
		return left.get(right);
	}

	public static <K, V> Entry<K, V> kv(K left, V right) {
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