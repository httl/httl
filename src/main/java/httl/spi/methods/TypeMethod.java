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

import httl.spi.Compiler;
import httl.spi.Formatter;
import httl.spi.converters.BeanMapConverter;
import httl.internal.util.ClassUtils;
import httl.internal.util.CollectionUtils;
import httl.internal.util.DateUtils;
import httl.internal.util.LocaleUtils;
import httl.internal.util.NumberUtils;
import httl.internal.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * TypeMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class TypeMethod {

	private Formatter<Object> formatter;

	private TimeZone timeZone;

	private String dateFormat;

	private String numberFormat;

	private String outputEncoding;

	private String[] importPackages;

	private final BeanMapConverter mapConverter = new BeanMapConverter();

	public void setCompiler(Compiler compiler) {
		this.mapConverter.setCompiler(compiler);
	}
	
	/**
	 * httl.properties: formatter=httl.spi.formatters.DateFormatter
	 */
	public void setFormatter(Formatter<Object> formatter) {
		this.formatter = formatter;
	}

	/**
	 * httl.properties: time.zone=+8
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = TimeZone.getTimeZone(timeZone);
	}

	/**
	 * httl.properties: date.format=yyyy-MM-dd HH:mm:ss
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * httl.properties: number.format=###,##0.###
	 */
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	/**
	 * httl.properties: output.encoding=UTF-8
	 */
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	/**
	 * httl.properties: import.packages=java.util
	 */
	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	public static Locale toLocale(String name) {
		return LocaleUtils.getLocale(name);
	}

	public static boolean toBoolean(Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return value == null ? false : toBoolean(String.valueOf(value));
	}

	public static char toChar(Object value) {
		if (value instanceof Character) {
			return (Character) value;
		}
		return value == null ? '\0' : toChar(String.valueOf(value));
	}

	public static byte toByte(Object value) {
		if (value instanceof Number) {
			return ((Number) value).byteValue();
		}
		return value == null ? 0 : toByte(String.valueOf(value));
	}

	public static short toShort(Object value) {
		if (value instanceof Number) {
			return ((Number) value).shortValue();
		}
		return value == null ? 0 : toShort(String.valueOf(value));
	}

	public static int toInt(Object value) {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return value == null ? 0 : toInt(String.valueOf(value));
	}

	public static long toLong(Object value) {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return value == null ? 0 : toLong(String.valueOf(value));
	}

	public static float toFloat(Object value) {
		if (value instanceof Number) {
			return ((Number) value).floatValue();
		}
		return value == null ? 0 : toFloat(String.valueOf(value));
	}

	public static double toDouble(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		return value == null ? 0 : toDouble(String.valueOf(value));
	}

	public static Class<?> toClass(Object value) {
		if (value instanceof Class) {
			return (Class<?>) value;
		}
		return value == null ? null : toClass(String.valueOf(value));
	}

	public static boolean toBoolean(String value) {
		return StringUtils.isEmpty(value) ? false : Boolean.parseBoolean(value);
	}

	public static char toChar(String value) {
		return StringUtils.isEmpty(value) ? '\0' : value.charAt(0);
	}

	public static byte toByte(String value) {
		return StringUtils.isEmpty(value) ? 0 : Byte.parseByte(value);
	}

	public static short toShort(String value) {
		return StringUtils.isEmpty(value) ? 0 : Short.parseShort(value);
	}

	public static int toInt(String value) {
		return StringUtils.isEmpty(value) ? 0 : Integer.parseInt(value);
	}

	public static long toLong(String value) {
		return StringUtils.isEmpty(value) ? 0 : Long.parseLong(value);
	}

	public static float toFloat(String value) {
		return StringUtils.isEmpty(value) ? 0 : Float.parseFloat(value);
	}

	public static double toDouble(String value) {
		return StringUtils.isEmpty(value) ? 0 : Double.parseDouble(value);
	}

	public static Class<?> toClass(String value) {
		return StringUtils.isEmpty(value) ? null : ClassUtils.forName(value);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> toMap(Object value) throws IOException, ParseException {
		if (value == null) {
			return null;
		}
		if (value instanceof Map) {
			return (Map<String, Object>) value;
		}
		return (Map<String, Object>) mapConverter.convert(value, null);
	}

	public <T> T[] toArray(Collection<T> values, String type) {
		return toArray(values, StringUtils.isEmpty(type) ? null : ClassUtils.forName(importPackages, type));
	}

	public static <T> T[] toArray(Collection<T> values) {
		return toArray(values, (Class<?>) null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<T> values, Class<?> type) {
		if (type == null) {
			if (CollectionUtils.isEmpty(values)) {
				type = Object.class;
			} else {
				type = values.iterator().next().getClass();
			}
		}
		if (values == null) {
			return (T[]) Array.newInstance(type, 0);
		}
		return (T[]) values.toArray((Object[])Array.newInstance(type, values.size()));
	}

	public static <T> List<T> toList(Collection<T> values) {
		if (values instanceof List) {
			return (List<T>) values;
		}
		return new ArrayList<T>(values);
	}

	@SuppressWarnings("unchecked")
	public static List<Object> toList(Object[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Object> list = new ArrayList<Object>(values.length);
		for (Object value : values) {
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Boolean> toList(boolean[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Boolean> list = new ArrayList<Boolean>(values.length);
		for (boolean value : values) {
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Character> toList(char[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Character> list = new ArrayList<Character>(values.length);
		for (char value : values) {
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Byte> toList(byte[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Byte> list = new ArrayList<Byte>(values.length);
		for (byte value : values) {
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Short> toList(short[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Short> list = new ArrayList<Short>(values.length);
		for (short value : values) {
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> toList(int[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Integer> list = new ArrayList<Integer>(values.length);
		for (int value : values) {
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Long> toList(long[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Long> list = new ArrayList<Long>(values.length);
		for (long value : values) {
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Float> toList(float[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Float> list = new ArrayList<Float>(values.length);
		for (float value : values) {
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Double> toList(double[] values) {
		if (values == null) {
			return Collections.EMPTY_LIST;
		}
		List<Double> list = new ArrayList<Double>(values.length);
		for (double value : values) {
			list.add(value);
		}
		return list;
	}

	public Date toDate(String value) {
		try {
			return StringUtils.isEmpty(value) ? null : DateUtils.parse(value, dateFormat, timeZone);
		} catch (Exception e) {
			try {
				return DateUtils.parse(value, "yyyy-MM-dd");
			} catch (Exception e2) {
				return DateUtils.parse(value, "yyyy-MM-dd HH:mm:ss");
			}
		}
	}

	public Date toDate(String value, String format) {
		return StringUtils.isEmpty(value) ? null : DateUtils.parse(value, format, timeZone);
	}

	public static Date toDate(String value, String format, String timeZone) {
		return StringUtils.isEmpty(value) ? null : DateUtils.parse(value, format, timeZone == null ? null : TimeZone.getTimeZone(timeZone));
	}

	public String toString(Date value) {
		return value == null ? null : DateUtils.format(value, dateFormat, timeZone);
	}

	public String format(Date value, String format) {
		return value == null ? null : DateUtils.format(value, format, timeZone);
	}

	public static String format(Date value, String format, String timeZone) {
		return value == null ? null : DateUtils.format(value, format, timeZone == null ? null : TimeZone.getTimeZone(timeZone));
	}

	public static String toString(boolean value) {
		return String.valueOf(value);
	}

	public static String toString(char value) {
		return String.valueOf(value);
	}

	public String toString(byte value) {
		return format(Byte.valueOf(value), numberFormat);
	}

	public String toString(short value) {
		return format(Short.valueOf(value), numberFormat);
	}

	public String toString(int value) {
		return format(Integer.valueOf(value), numberFormat);
	}

	public String toString(long value) {
		return format(Long.valueOf(value), numberFormat);
	}

	public String toString(float value) {
		return format(Float.valueOf(value), numberFormat);
	}

	public String toString(double value) {
		return format(Double.valueOf(value), numberFormat);
	}

	public String toString(Number value) {
		return format(value, numberFormat);
	}

	public String toString(byte[] value) {
		try {
			return value == null ? null : (outputEncoding == null 
					? new String(value) : new String(value, outputEncoding));
		} catch (UnsupportedEncodingException e) {
			return new String(value);
		}
	}

	public String toString(Object value) {
		if (value == null)
			return null;
		if (value instanceof String)
			return (String) value;
		if (value instanceof Number)
			return toString((Number) value);
		if (value instanceof Date)
			return toString((Date) value);
		if (value instanceof byte[])
			return toString((byte[]) value);
		if (formatter != null)
			return formatter.toString("", value);
		return StringUtils.toString(value);
	}

	public static String format(byte value, String format) {
		return format(Byte.valueOf(value), format);
	}

	public static String format(short value, String format) {
		return format(Short.valueOf(value), format);
	}

	public static String format(int value, String format) {
		return format(Integer.valueOf(value), format);
	}

	public static String format(long value, String format) {
		return format(Long.valueOf(value), format);
	}

	public static String format(float value, String format) {
		return format(Float.valueOf(value), format);
	}

	public static String format(double value, String format) {
		return format(Double.valueOf(value), format);
	}

	public static String format(Number value, String format) {
		return value == null ? null : NumberUtils.format(value, format);
	}

}