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
package httl.spi.formatters;

import httl.Resource;
import httl.Template;
import httl.ast.Expression;
import httl.internal.util.ClassComparator;
import httl.internal.util.ClassUtils;
import httl.internal.util.DateUtils;
import httl.internal.util.IOUtils;
import httl.internal.util.StringUtils;
import httl.spi.Formatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MultiFormatter. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setFormatter(Formatter)
 * @see httl.spi.translators.InterpretedTranslator#setFormatter(Formatter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiFormatter implements Formatter<Object> {

	private final Map<Class<?>, Formatter<?>> formatters = new ConcurrentHashMap<Class<?>, Formatter<?>>();

	private Map<Class<?>, Formatter<?>> sortedFormatters;

	private Formatter<Object> formatter;
	
	private Formatter<Boolean> booleanFormatter;
	
	private Formatter<Number> byteFormatter;
	
	private Formatter<Character> charFormatter;
	
	private Formatter<Number> shortFormatter;
	
	private Formatter<Number> intFormatter;
	
	private Formatter<Number> longFormatter;
	
	private Formatter<Number> floatFormatter;
	
	private Formatter<Number> doubleFormatter;

	private Formatter<Number> numberFormatter;

	private Formatter<Date> dateFormatter;

	private String nullValue;

	private String trueValue;

	private String falseValue;

	private char[] nullValueChars;

	private char[] trueValueChars;

	private char[] falseValueChars;

	private byte[] nullValueBytes;

	private byte[] trueValueBytes;

	private byte[] falseValueBytes;

	private String outputEncoding;

	public MultiFormatter() {
	}

	public MultiFormatter(Formatter<?> formatter) {
		setFormatters(new Formatter<?>[] { formatter });
		init();
	}

	public void init() {
		if (nullValue == null) {
			setNullValue("");
		}
		if (trueValue == null) {
			setTrueValue("true");
		}
		if (falseValue == null) {
			setFalseValue("false");
		}
		this.numberFormatter = get(Number.class);
		this.booleanFormatter = get(Boolean.class);
		this.byteFormatter = getFormatter(Byte.class, numberFormatter);
		this.charFormatter = get(Character.class);
		this.shortFormatter = getFormatter(Short.class, numberFormatter);
		this.intFormatter = getFormatter(Integer.class, numberFormatter);
		this.longFormatter = getFormatter(Long.class, numberFormatter);
		this.floatFormatter = getFormatter(Float.class, numberFormatter);
		this.doubleFormatter = getFormatter(Double.class, numberFormatter);
		this.dateFormatter = get(Date.class);
	}

	/**
	 * httl.properties: output.encoding=UTF-8
	 */
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	/**
	 * httl.properties: null.value=null
	 */
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
		this.nullValueChars = nullValue.toCharArray();
		this.nullValueBytes = toBytes(nullValue, nullValue);
	}

	/**
	 * httl.properties: true.value=true
	 */
	public void setTrueValue(String trueValue) {
		this.trueValue = trueValue;
		this.trueValueChars = trueValue.toCharArray();
		this.trueValueBytes = toBytes(trueValue, trueValue);
	}

	/**
	 * httl.properties: false.value=false
	 */
	public void setFalseValue(String falseValue) {
		this.falseValue = falseValue;
		this.falseValueChars = falseValue.toCharArray();
		this.falseValueBytes = toBytes(falseValue, falseValue);
	}

	/**
	 * Add and copy the MultiFormatter.
	 * 
	 * @param formatters
	 */
	public MultiFormatter add(Formatter<?>... formatters) {
		if (formatter != null) {
			MultiFormatter copy = new MultiFormatter();
			copy.formatters.putAll(this.formatters);
			copy.setFormatters(formatters);
			return copy;
		}
		return this;
	}

	/**
	 * Remove and copy the MultiFormatter.
	 * 
	 * @param formatters
	 */
	public MultiFormatter remove(Formatter<?>... formatters) {
		if (formatter != null) {
			MultiFormatter copy = new MultiFormatter();
			copy.formatters.putAll(this.formatters);
			if (formatters != null && formatters.length > 0) {
				for (Formatter<?> formatter : formatters) {
					if (formatter != null) {
						Class<?> type = ClassUtils.getGenericClass(formatter.getClass());
						if (type != null) {
							this.formatters.remove(type);
						}
					}
				}
			}
			return copy;
		}
		return this;
	}

	/**
	 * httl.properties: formatters+=httl.spi.formatters.NumberFormatter
	 */
	public void setFormatters(Formatter<?>[] formatters) {
		if (formatters != null && formatters.length > 0) {
			for (Formatter<?> formatter : formatters) {
				if (formatter != null) {
					Class<?> type = ClassUtils.getGenericClass(formatter.getClass());
					if (type != null) {
						this.formatters.put(type, formatter);
					}
				}
			}
			Map<Class<?>, Formatter<?>> sorted = new TreeMap<Class<?>, Formatter<?>>(ClassComparator.COMPARATOR);
			sorted.putAll(this.formatters);
			this.sortedFormatters = Collections.unmodifiableMap(sorted);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> Formatter<T> get(Class<T> type) {
		return (Formatter)formatters.get((Class)type);
	}

	@SuppressWarnings("unchecked")
	private Formatter<Number> getFormatter(Class<? extends Number> type, Formatter<Number> defaultFormatter) {
		Formatter<Number> formatter = get((Class<Number>)type);
		if (formatter == null) {
			return defaultFormatter;
		}
		return formatter;
	}
	
	public String toString(String key, boolean value) {
		if (booleanFormatter != null)
			return booleanFormatter.toString(key, value);
		return value ? trueValue : falseValue;
	}
	
	public String toString(String key, byte value) {
		if (byteFormatter != null)
			return byteFormatter.toString(key, value);
		return String.valueOf(value);
	}

	public String toString(String key, char value) {
		if (charFormatter != null)
			return charFormatter.toString(key, value);
		return String.valueOf(value);
	}

	public String toString(String key, short value) {
		if (shortFormatter != null)
			return shortFormatter.toString(key, value);
		return String.valueOf(value);
	}

	public String toString(String key, int value) {
		if (intFormatter != null)
			return intFormatter.toString(key, value);
		return String.valueOf(value);
	}

	public String toString(String key, long value) {
		if (longFormatter != null)
			return longFormatter.toString(key, value);
		return String.valueOf(value);
	}

	public String toString(String key, float value) {
		if (floatFormatter != null)
			return floatFormatter.toString(key, value);
		return String.valueOf(value);
	}
	
	public String toString(String key, double value) {
		if (doubleFormatter != null)
			return doubleFormatter.toString(key, value);
		return String.valueOf(value);
	}
	
	public String toString(String key, Boolean value) {
		if (value == null)
			return nullValue;
		if (booleanFormatter != null) 
			return booleanFormatter.toString(key, value);
		return value.booleanValue() ? trueValue : falseValue;
	}
	
	public String toString(String key, Byte value) {
		if (value == null)
			return nullValue;
		if (byteFormatter != null) 
			return byteFormatter.toString(key, value);
		return value.toString();
	}

	public String toString(String key, Character value) {
		if (value == null)
			return nullValue;
		if (charFormatter != null) 
			return charFormatter.toString(key, value);
		return value.toString();
	}

	public String toString(String key, Short value) {
		if (value == null)
			return nullValue;
		if (shortFormatter != null) 
			return shortFormatter.toString(key, value);
		return value.toString();
	}

	public String toString(String key, Integer value) {
		if (value == null)
			return nullValue;
		if (intFormatter != null) 
			return intFormatter.toString(key, value);
		return value.toString();
	}
	
	public String toString(String key, Long value) {
		if (value == null)
			return nullValue;
		if (longFormatter != null) 
			return longFormatter.toString(key, value);
		return value.toString();
	}

	public String toString(String key, Float value) {
		if (value == null)
			return nullValue;
		if (floatFormatter != null) 
			return floatFormatter.toString(key, value);
		return value.toString();
	}

	public String toString(String key, Double value) {
		if (value == null)
			return nullValue;
		if (doubleFormatter != null) 
			return doubleFormatter.toString(key, value);
		return value.toString();
	}
	
	public String toString(String key, Number value) {
		if (value == null)
			return nullValue;
		if (value instanceof Byte)
			return toString(key, (Byte) value);
		if (value instanceof Short)
			return toString(key, (Short) value);
		if (value instanceof Integer)
			return toString(key, (Integer) value);
		if (value instanceof Long)
			return toString(key, (Long) value);
		if (value instanceof Float)
			return toString(key, (Float) value);
		if (value instanceof Double)
			return toString(key, (Double) value);
		if (numberFormatter != null) 
			return numberFormatter.toString(key, value);
		return value.toString();
	}

	public String toString(String key, Date value) {
		if (value == null)
			return nullValue;
		if (dateFormatter != null) 
			return dateFormatter.toString(key, value);
		return value.toString();
	}

	public String toString(String key, byte[] value) {
		if (value == null)
			return nullValue;
		if (value.length == 0)
			return "";
		if (outputEncoding == null)
			return new String(value);
		try {
			return new String(value, outputEncoding);
		} catch (UnsupportedEncodingException e) {
			return new String(value);
		}
	}

	public String toString(String key, char[] value) {
		if (value == null)
			return nullValue;
		return String.valueOf(value);
	}

	public String toString(String key, String value) {
		if (value == null)
			return nullValue;
		return value;
	}

	public String toString(String key, Template value) {
		try {
			return toString(key, value.evaluate());
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public String toString(String key, Resource value) {
		try {
			return IOUtils.readToString(value.getReader());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public String toString(String key, Object value) {
		if (value == null)
			return nullValue;
		if (value instanceof String)
			return (String) value;
		if (value instanceof Boolean)
			return toString(key, (Boolean) value);
		if (value instanceof Character)
			return toString(key, (Character) value);
		if (value instanceof Number)
			return toString(key, (Number) value);
		if (value instanceof Date)
			return toString(key, (Date) value);
		if (value instanceof byte[])
			return toString(key, (byte[]) value);
		if (value instanceof Template)
			return toString(key, (Template) value);
		if (value instanceof Resource)
			return toString(key, (Resource) value);
		if (formatter != null)
			return formatter.toString(key, value);
		Class<?> cls = value.getClass();
		Formatter<Object> formatter = (Formatter<Object>) formatters.get(cls);
		if (formatter != null) {
			return formatter.toString(key, value);
		} else if (sortedFormatters != null) {
			for (Map.Entry<Class<?>, Formatter<?>> entry : sortedFormatters.entrySet()) {
				if (entry.getKey().isAssignableFrom(cls)) {
					formatter = (Formatter<Object>) entry.getValue();
					formatters.put(cls, formatter);
					return formatter.toString(key, value);
				}
			}
		}
		return StringUtils.toString(value);
	}

	public char[] toChars(String key, boolean value) {
		if (booleanFormatter != null)
			return booleanFormatter.toChars(key, value);
		return value ? trueValueChars : falseValueChars;
	}
	
	public char[] toChars(String key, byte value) {
		if (byteFormatter != null)
			return byteFormatter.toChars(key, value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(String key, char value) {
		if (charFormatter != null)
			return charFormatter.toChars(key, value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(String key, short value) {
		if (shortFormatter != null)
			return shortFormatter.toChars(key, value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(String key, int value) {
		if (intFormatter != null)
			return intFormatter.toChars(key, value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(String key, long value) {
		if (longFormatter != null)
			return longFormatter.toChars(key, value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(String key, float value) {
		if (floatFormatter != null)
			return floatFormatter.toChars(key, value);
		return String.valueOf(value).toCharArray();
	}
	
	public char[] toChars(String key, double value) {
		if (doubleFormatter != null)
			return doubleFormatter.toChars(key, value);
		return String.valueOf(value).toCharArray();
	}
	
	public char[] toChars(String key, Boolean value) {
		if (value == null)
			return nullValueChars;
		if (booleanFormatter != null) 
			return booleanFormatter.toChars(key, value);
		return value.booleanValue() ? trueValueChars : falseValueChars;
	}
	
	public char[] toChars(String key, Byte value) {
		if (value == null)
			return nullValueChars;
		if (byteFormatter != null) 
			return byteFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}

	public char[] toChars(String key, Character value) {
		if (value == null)
			return nullValueChars;
		if (charFormatter != null) 
			return charFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}

	public char[] toChars(String key, Short value) {
		if (value == null)
			return nullValueChars;
		if (shortFormatter != null) 
			return shortFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}

	public char[] toChars(String key, Integer value) {
		if (value == null)
			return nullValueChars;
		if (intFormatter != null) 
			return intFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}
	
	public char[] toChars(String key, Long value) {
		if (value == null)
			return nullValueChars;
		if (longFormatter != null) 
			return longFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}

	public char[] toChars(String key, Float value) {
		if (value == null)
			return nullValueChars;
		if (floatFormatter != null) 
			return floatFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}
	
	public char[] toChars(String key, Double value) {
		if (value == null)
			return nullValueChars;
		if (doubleFormatter != null) 
			return doubleFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}
	
	public char[] toChars(String key, Number value) {
		if (value == null)
			return nullValueChars;
		if (value instanceof Byte)
			return toChars(key, (Byte) value);
		if (value instanceof Short)
			return toChars(key, (Short) value);
		if (value instanceof Integer)
			return toChars(key, (Integer) value);
		if (value instanceof Long)
			return toChars(key, (Long) value);
		if (value instanceof Float)
			return toChars(key, (Float) value);
		if (value instanceof Double)
			return toChars(key, (Double) value);
		if (numberFormatter != null) 
			return numberFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}

	public char[] toChars(String key, Date value) {
		if (value == null)
			return nullValueChars;
		if (dateFormatter != null) 
			return dateFormatter.toChars(key, value);
		return value.toString().toCharArray();
	}

	public char[] toChars(String key, byte[] value) {
		if (value == null)
			return nullValueChars;
		if (value.length == 0)
			return new char[0];
		if (outputEncoding == null)
			return new String(value).toCharArray();
		try {
			return new String(value, outputEncoding).toCharArray();
		} catch (UnsupportedEncodingException e) {
			return new String(value).toCharArray();
		}
	}

	public char[] toChars(String key, char[] value) {
		if (value == null)
			return nullValueChars;
		return value;
	}

	public char[] toChars(String key, String value) {
		if (value == null)
			return nullValueChars;
		return value.toCharArray();
	}

	public char[] toChars(String key, Template value) {
		try {
			return toChars(key, value.evaluate());
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public char[] toChars(String key, Resource value) {
		try {
			return IOUtils.readToChars(value.getReader());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public char[] toChars(String key, Object value) {
		if (value == null)
			return nullValueChars;
		if (value instanceof char[])
			return (char[]) value;
		if (value instanceof Boolean)
			return toChars(key, (Boolean) value);
		if (value instanceof Character)
			return toChars(key, (Character) value);
		if (value instanceof Number)
			return toChars(key, (Number) value);
		if (value instanceof Date)
			return toChars(key, (Date) value);
		if (value instanceof String)
			return toChars(key, (String) value);
		if (value instanceof Template)
			return toChars(key, (Template) value);
		if (value instanceof Expression)
			return toChars(key, (Expression) value);
		if (value instanceof Resource)
			return toChars(key, (Resource) value);
		if (formatter != null)
			return formatter.toChars(key, value);
		Class<?> cls = value.getClass();
		Formatter<Object> formatter = (Formatter<Object>) formatters.get(cls);
		if (formatter != null) {
			return formatter.toChars(key, value);
		} else if (sortedFormatters != null) {
			for (Map.Entry<Class<?>, Formatter<?>> entry : sortedFormatters.entrySet()) {
				if (entry.getKey().isAssignableFrom(cls)) {
					formatter = (Formatter<Object>) entry.getValue();
					formatters.put(cls, formatter);
					return formatter.toChars(key, value);
				}
			}
		}
		return toChars(key, StringUtils.toString(value));
	}

	public byte[] toBytes(String key, boolean value) {
		if (booleanFormatter != null)
			return booleanFormatter.toBytes(key, value);
		return value ? trueValueBytes : falseValueBytes;
	}
	
	public byte[] toBytes(String key, byte value) {
		if (byteFormatter != null)
			return byteFormatter.toBytes(key, value);
		return toBytes(key, String.valueOf(value));
	}

	public byte[] toBytes(String key, char value) {
		if (charFormatter != null)
			return charFormatter.toBytes(key, value);
		return toBytes(key, String.valueOf(value));
	}

	public byte[] toBytes(String key, short value) {
		if (shortFormatter != null)
			return shortFormatter.toBytes(key, value);
		return toBytes(key, String.valueOf(value));
	}

	public byte[] toBytes(String key, int value) {
		if (intFormatter != null)
			return intFormatter.toBytes(key, value);
		return toBytes(key, String.valueOf(value));
	}

	public byte[] toBytes(String key, long value) {
		if (longFormatter != null)
			return longFormatter.toBytes(key, value);
		return toBytes(key, String.valueOf(value));
	}

	public byte[] toBytes(String key, float value) {
		if (floatFormatter != null)
			return floatFormatter.toBytes(key, value);
		return toBytes(key, String.valueOf(value));
	}
	
	public byte[] toBytes(String key, double value) {
		if (doubleFormatter != null)
			return doubleFormatter.toBytes(key, value);
		return toBytes(key, String.valueOf(value));
	}
	
	public byte[] toBytes(String key, Boolean value) {
		if (value == null)
			return nullValueBytes;
		if (booleanFormatter != null) 
			return booleanFormatter.toBytes(key, value);
		return value.booleanValue() ? trueValueBytes : falseValueBytes;
	}
	
	public byte[] toBytes(String key, Byte value) {
		if (value == null)
			return nullValueBytes;
		if (byteFormatter != null) 
			return byteFormatter.toBytes(key, value);
		return toBytes(key, value.toString());
	}

	public byte[] toBytes(String key, Character value) {
		if (value == null)
			return nullValueBytes;
		if (charFormatter != null) 
			return charFormatter.toBytes(key, value);
		return toBytes(key, value.toString());
	}

	public byte[] toBytes(String key, Short value) {
		if (value == null)
			return nullValueBytes;
		if (shortFormatter != null) 
			return shortFormatter.toBytes(key, value);
		return toBytes(key, value.toString());
	}

	public byte[] toBytes(String key, Integer value) {
		if (value == null)
			return nullValueBytes;
		if (intFormatter != null) 
			return intFormatter.toBytes(key, value);
		return toBytes(key, value.toString());
	}
	
	public byte[] toBytes(String key, Long value) {
		if (value == null)
			return nullValueBytes;
		if (longFormatter != null) 
			return longFormatter.toBytes(key, value);
		return toBytes(key, value.toString());
	}

	public byte[] toBytes(String key, Float value) {
		if (value == null)
			return nullValueBytes;
		if (floatFormatter != null) 
			return floatFormatter.toBytes(key, value);
		return toBytes(key, value.toString());
	}
	
	public byte[] toBytes(String key, Double value) {
		if (value == null)
			return nullValueBytes;
		if (doubleFormatter != null) 
			return doubleFormatter.toBytes(key, value);
		return toBytes(key, value.toString());
	}
	
	public byte[] toBytes(String key, Number value) {
		if (value == null)
			return nullValueBytes;
		if (value instanceof Byte)
			return toBytes(key, (Byte) value);
		if (value instanceof Short)
			return toBytes(key, (Short) value);
		if (value instanceof Integer)
			return toBytes(key, (Integer) value);
		if (value instanceof Long)
			return toBytes(key, (Long) value);
		if (value instanceof Float)
			return toBytes(key, (Float) value);
		if (value instanceof Double)
			return toBytes(key, (Double) value);
		if (numberFormatter != null) 
			return numberFormatter.toBytes(key, value);
		return toBytes(key, value.toString());
	}

	public byte[] toBytes(String key, Date value) {
		if (value == null)
			return nullValueBytes;
		if (dateFormatter != null) 
			return dateFormatter.toBytes(key, value);
		return toBytes(key, DateUtils.format(value));
	}

	public byte[] toBytes(String key, byte[] value) {
		if (value == null)
			return nullValueBytes;
		return value;
	}

	public byte[] toBytes(String key, char[] value) {
		if (value == null)
			return nullValueBytes;
		return toBytes(key, String.valueOf(value));
	}

	public byte[] toBytes(String key, String value) {
		if (value == null)
			return nullValueBytes;
		if (value.length() == 0)
			return new byte[0];
		if (outputEncoding == null)
			return value.getBytes();
		try {
			return value.getBytes(outputEncoding);
		} catch (UnsupportedEncodingException e) {
			return value.getBytes();
		}
	}

	public byte[] toBytes(String key, Template value) {
		try {
			return toBytes(key, value.evaluate());
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public byte[] toBytes(String key, Resource value) {
		try {
			return IOUtils.readToBytes(value.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public byte[] toBytes(String key, Object value) {
		if (value == null)
			return nullValueBytes;
		if (value instanceof byte[])
			return (byte[]) value;
		if (value instanceof Boolean)
			return toBytes(key, (Boolean) value);
		if (value instanceof Character)
			return toBytes(key, (Character) value);
		if (value instanceof Number)
			return toBytes(key, (Number) value);
		if (value instanceof Date)
			return toBytes(key, (Date) value);
		if (value instanceof String)
			return toBytes(key, (String) value);
		if (value instanceof Template)
			return toBytes(key, (Template) value);
		if (value instanceof Expression)
			return toBytes(key, (Expression) value);
		if (value instanceof Resource)
			return toBytes(key, (Resource) value);
		if (formatter != null)
			return formatter.toBytes(key, value);
		Class<?> cls = value.getClass();
		Formatter<Object> formatter = (Formatter<Object>) formatters.get(cls);
		if (formatter != null) {
			return formatter.toBytes(key, value);
		} else if (sortedFormatters != null) {
			for (Map.Entry<Class<?>, Formatter<?>> entry : sortedFormatters.entrySet()) {
				if (entry.getKey().isAssignableFrom(cls)) {
					formatter = (Formatter<Object>) entry.getValue();
					formatters.put(cls, formatter);
					return formatter.toBytes(key, value);
				}
			}
		}
		return toBytes(key, StringUtils.toString(value));
	}

}