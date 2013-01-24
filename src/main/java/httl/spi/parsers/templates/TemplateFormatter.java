/*
 * Copyright 2011-2012 HTTL Team.
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
package httl.spi.parsers.templates;

import httl.Engine;
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.spi.Formatter;
import httl.spi.formatters.MultiFormatter;
import httl.util.DateUtils;
import httl.util.IOUtils;
import httl.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;

/**
 * TemplateFormatter. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class TemplateFormatter {

	private static final String NULL_VALUE		  = "null.value";

	private static final String TRUE_VALUE		  = "true.value";

	private static final String FALSE_VALUE		 = "false.value";

	private static final String OUTPUT_ENCODING	 = "output.encoding";

	private transient final Formatter<Object> formatter;
	
	private transient final Formatter<Boolean> booleanFormatter;
	
	private transient final Formatter<Number> byteFormatter;
	
	private transient final Formatter<Character> charFormatter;
	
	private transient final Formatter<Number> shortFormatter;
	
	private transient final Formatter<Number> intFormatter;
	
	private transient final Formatter<Number> longFormatter;
	
	private transient final Formatter<Number> floatFormatter;
	
	private transient final Formatter<Number> doubleFormatter;

	private transient final Formatter<Number> numberFormatter;

	private transient final Formatter<Date> dateFormatter;

	private transient final String nullValue;

	private transient final String trueValue;

	private transient final String falseValue;

	private transient final char[] nullValueChars;

	private transient final char[] trueValueChars;

	private transient final char[] falseValueChars;

	private transient final byte[] nullValueBytes;

	private transient final byte[] trueValueBytes;

	private transient final byte[] falseValueBytes;

	private transient final String outputEncoding;
	
	@SuppressWarnings("unchecked")
	public TemplateFormatter(Engine engine, Formatter<?> formatter) {
		this.formatter = (Formatter<Object>) formatter;
		this.outputEncoding = engine.getProperty(OUTPUT_ENCODING, String.class);
		if (formatter instanceof MultiFormatter) {
			MultiFormatter multi = (MultiFormatter) formatter;
			this.numberFormatter = multi.get(Number.class);
			this.booleanFormatter = multi.get(Boolean.class);
			this.byteFormatter = getFormatter(multi, Byte.class, numberFormatter);
			this.charFormatter = multi.get(Character.class);
			this.shortFormatter = getFormatter(multi, Short.class, numberFormatter);
			this.intFormatter = getFormatter(multi, Integer.class, numberFormatter);
			this.longFormatter = getFormatter(multi, Long.class, numberFormatter);
			this.floatFormatter = getFormatter(multi, Float.class, numberFormatter);
			this.doubleFormatter = getFormatter(multi, Double.class, numberFormatter);
			this.dateFormatter = multi.get(Date.class);
		} else {
			this.numberFormatter = null;
			this.booleanFormatter = null;
			this.byteFormatter = null;
			this.charFormatter = null;
			this.shortFormatter = null;
			this.intFormatter = null;
			this.longFormatter = null;
			this.floatFormatter = null;
			this.doubleFormatter = null;
			this.dateFormatter = null;
		}
		this.nullValue = engine.getProperty(NULL_VALUE, "");
		this.trueValue = engine.getProperty(TRUE_VALUE, "true");
		this.falseValue = engine.getProperty(FALSE_VALUE, "false");
		this.nullValueChars = this.nullValue.toCharArray();
		this.trueValueChars = this.trueValue.toCharArray();
		this.falseValueChars = this.falseValue.toCharArray();
		this.nullValueBytes = toBytes(this.nullValue);
		this.trueValueBytes = toBytes(this.trueValue);
		this.falseValueBytes = toBytes(this.falseValue);
	}
	
	@SuppressWarnings("unchecked")
	private static Formatter<Number> getFormatter(MultiFormatter multi, Class<? extends Number> type, Formatter<Number> defaultFormatter) {
		Formatter<Number> formatter = multi.get((Class<Number>)type);
		if (formatter == null) {
			return defaultFormatter;
		}
		return formatter;
	}
	
	public String toString(boolean value) {
		if (booleanFormatter != null)
			return booleanFormatter.toString(value);
		return value ? trueValue : falseValue;
	}
	
	public String toString(byte value) {
		if (byteFormatter != null)
			return byteFormatter.toString(value);
		return String.valueOf(value);
	}

	public String toString(char value) {
		if (charFormatter != null)
			return charFormatter.toString(value);
		return String.valueOf(value);
	}

	public String toString(short value) {
		if (shortFormatter != null)
			return shortFormatter.toString(value);
		return String.valueOf(value);
	}

	public String toString(int value) {
		if (intFormatter != null)
			return intFormatter.toString(value);
		return String.valueOf(value);
	}

	public String toString(long value) {
		if (longFormatter != null)
			return longFormatter.toString(value);
		return String.valueOf(value);
	}

	public String toString(float value) {
		if (floatFormatter != null)
			return floatFormatter.toString(value);
		return String.valueOf(value);
	}
	
	public String toString(double value) {
		if (doubleFormatter != null)
			return doubleFormatter.toString(value);
		return String.valueOf(value);
	}
	
	public String toString(Boolean value) {
		if (value == null)
			return nullValue;
		if (booleanFormatter != null) 
			return booleanFormatter.toString(value);
		return value.booleanValue() ? trueValue : falseValue;
	}
	
	public String toString(Byte value) {
		if (value == null)
			return nullValue;
		if (byteFormatter != null) 
			return byteFormatter.toString(value);
		return value.toString();
	}

	public String toString(Character value) {
		if (value == null)
			return nullValue;
		if (charFormatter != null) 
			return charFormatter.toString(value);
		return value.toString();
	}

	public String toString(Short value) {
		if (value == null)
			return nullValue;
		if (shortFormatter != null) 
			return shortFormatter.toString(value);
		return value.toString();
	}

	public String toString(Integer value) {
		if (value == null)
			return nullValue;
		if (intFormatter != null) 
			return intFormatter.toString(value);
		return value.toString();
	}
	
	public String toString(Long value) {
		if (value == null)
			return nullValue;
		if (longFormatter != null) 
			return longFormatter.toString(value);
		return value.toString();
	}

	public String toString(Float value) {
		if (value == null)
			return nullValue;
		if (floatFormatter != null) 
			return floatFormatter.toString(value);
		return value.toString();
	}

	public String toString(Double value) {
		if (value == null)
			return nullValue;
		if (doubleFormatter != null) 
			return doubleFormatter.toString(value);
		return value.toString();
	}
	
	public String toString(Number value) {
		if (value == null)
			return nullValue;
		if (value instanceof Byte)
			return toString((Byte) value);
		if (value instanceof Short)
			return toString((Short) value);
		if (value instanceof Integer)
			return toString((Integer) value);
		if (value instanceof Long)
			return toString((Long) value);
		if (value instanceof Float)
			return toString((Float) value);
		if (value instanceof Double)
			return toString((Double) value);
		if (numberFormatter != null) 
			return numberFormatter.toString(value);
		return value.toString();
	}

	public String toString(Date value) {
		if (value == null)
			return nullValue;
		if (dateFormatter != null) 
			return dateFormatter.toString(value);
		return value.toString();
	}

	public String toString(byte[] value) {
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

	public String toString(char[] value) {
		if (value == null)
			return nullValue;
		return String.valueOf(value);
	}

	public String toString(String value) {
		if (value == null)
			return nullValue;
		return value;
	}

	public String toString(Template e) throws ParseException, IOException {
		return toString(e.evaluate());
	}

	public String toString(Expression e) throws ParseException, IOException {
		return toString(e.evaluate());
	}

	public String toString(Resource e) throws IOException {
		return IOUtils.readToString(e.getReader());
	}

	public String toString(Object value) throws ParseException, IOException {
		if (value == null)
			return nullValue;
		if (value instanceof String)
			return (String) value;
		if (value instanceof Boolean)
			return toString((Boolean) value);
		if (value instanceof Character)
			return toString((Character) value);
		if (value instanceof Number)
			return toString((Number) value);
		if (value instanceof Date)
			return toString((Date) value);
		if (value instanceof byte[])
			return toString((byte[]) value);
		if (value instanceof Template)
			return toString((Template) value);
		if (value instanceof Expression)
			return toString((Expression) value);
		if (value instanceof Resource)
			return toString((Resource) value);
		if (formatter != null)
			return formatter.toString(value);
		return StringUtils.toString(value);
	}

	public char[] toChars(boolean value) {
		if (booleanFormatter != null)
			return booleanFormatter.toChars(value);
		return value ? trueValueChars : falseValueChars;
	}
	
	public char[] toChars(byte value) {
		if (byteFormatter != null)
			return byteFormatter.toChars(value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(char value) {
		if (charFormatter != null)
			return charFormatter.toChars(value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(short value) {
		if (shortFormatter != null)
			return shortFormatter.toChars(value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(int value) {
		if (intFormatter != null)
			return intFormatter.toChars(value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(long value) {
		if (longFormatter != null)
			return longFormatter.toChars(value);
		return String.valueOf(value).toCharArray();
	}

	public char[] toChars(float value) {
		if (floatFormatter != null)
			return floatFormatter.toChars(value);
		return String.valueOf(value).toCharArray();
	}
	
	public char[] toChars(double value) {
		if (doubleFormatter != null)
			return doubleFormatter.toChars(value);
		return String.valueOf(value).toCharArray();
	}
	
	public char[] toChars(Boolean value) {
		if (value == null)
			return nullValueChars;
		if (booleanFormatter != null) 
			return booleanFormatter.toChars(value);
		return value.booleanValue() ? trueValueChars : falseValueChars;
	}
	
	public char[] toChars(Byte value) {
		if (value == null)
			return nullValueChars;
		if (byteFormatter != null) 
			return byteFormatter.toChars(value);
		return value.toString().toCharArray();
	}

	public char[] toChars(Character value) {
		if (value == null)
			return nullValueChars;
		if (charFormatter != null) 
			return charFormatter.toChars(value);
		return value.toString().toCharArray();
	}

	public char[] toChars(Short value) {
		if (value == null)
			return nullValueChars;
		if (shortFormatter != null) 
			return shortFormatter.toChars(value);
		return value.toString().toCharArray();
	}

	public char[] toChars(Integer value) {
		if (value == null)
			return nullValueChars;
		if (intFormatter != null) 
			return intFormatter.toChars(value);
		return value.toString().toCharArray();
	}
	
	public char[] toChars(Long value) {
		if (value == null)
			return nullValueChars;
		if (longFormatter != null) 
			return longFormatter.toChars(value);
		return value.toString().toCharArray();
	}
	
	public char[] toChars(Double value) {
		if (value == null)
			return nullValueChars;
		if (doubleFormatter != null) 
			return doubleFormatter.toChars(value);
		return value.toString().toCharArray();
	}
	
	public char[] toChars(Number value) {
		if (value == null)
			return nullValueChars;
		if (value instanceof Byte)
			return toChars((Byte) value);
		if (value instanceof Short)
			return toChars((Short) value);
		if (value instanceof Integer)
			return toChars((Integer) value);
		if (value instanceof Long)
			return toChars((Long) value);
		if (value instanceof Float)
			return toChars((Float) value);
		if (value instanceof Double)
			return toChars((Double) value);
		if (numberFormatter != null) 
			return numberFormatter.toChars(value);
		return value.toString().toCharArray();
	}

	public char[] toChars(Date value) {
		if (value == null)
			return nullValueChars;
		if (dateFormatter != null) 
			return dateFormatter.toChars(value);
		return value.toString().toCharArray();
	}

	public char[] toChars(byte[] value) {
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

	public char[] toChars(char[] value) {
		if (value == null)
			return nullValueChars;
		return value;
	}

	public char[] toChars(String value) {
		if (value == null)
			return nullValueChars;
		return value.toCharArray();
	}

	public char[] toChars(Template e) throws ParseException, IOException {
		return toChars(e.evaluate());
	}

	public char[] toChars(Expression e) throws ParseException, IOException {
		return toChars(e.evaluate());
	}

	public char[] toChars(Resource e) throws IOException {
		return IOUtils.readToChars(e.getReader());
	}

	public char[] toChars(Object value) throws ParseException, IOException {
		if (value == null)
			return nullValueChars;
		if (value instanceof char[])
			return (char[]) value;
		if (value instanceof Boolean)
			return toChars((Boolean) value);
		if (value instanceof Character)
			return toChars((Character) value);
		if (value instanceof Number)
			return toChars((Number) value);
		if (value instanceof Date)
			return toChars((Date) value);
		if (value instanceof String)
			return toChars((String) value);
		if (value instanceof Template)
			return toChars((Template) value);
		if (value instanceof Expression)
			return toChars((Expression) value);
		if (value instanceof Resource)
			return toChars((Resource) value);
		if (formatter != null)
			return formatter.toChars(value);
		return toChars(StringUtils.toString(value));
	}

	public byte[] toBytes(boolean value) {
		if (booleanFormatter != null)
			return booleanFormatter.toBytes(value);
		return value ? trueValueBytes : falseValueBytes;
	}
	
	public byte[] toBytes(byte value) {
		if (byteFormatter != null)
			return byteFormatter.toBytes(value);
		return toBytes(String.valueOf(value));
	}

	public byte[] toBytes(char value) {
		if (charFormatter != null)
			return charFormatter.toBytes(value);
		return toBytes(String.valueOf(value));
	}

	public byte[] toBytes(short value) {
		if (shortFormatter != null)
			return shortFormatter.toBytes(value);
		return toBytes(String.valueOf(value));
	}

	public byte[] toBytes(int value) {
		if (intFormatter != null)
			return intFormatter.toBytes(value);
		return toBytes(String.valueOf(value));
	}

	public byte[] toBytes(long value) {
		if (longFormatter != null)
			return longFormatter.toBytes(value);
		return toBytes(String.valueOf(value));
	}

	public byte[] toBytes(float value) {
		if (floatFormatter != null)
			return floatFormatter.toBytes(value);
		return toBytes(String.valueOf(value));
	}
	
	public byte[] toBytes(double value) {
		if (doubleFormatter != null)
			return doubleFormatter.toBytes(value);
		return toBytes(String.valueOf(value));
	}
	
	public byte[] toBytes(Boolean value) {
		if (value == null)
			return nullValueBytes;
		if (booleanFormatter != null) 
			return booleanFormatter.toBytes(value);
		return value.booleanValue() ? trueValueBytes : falseValueBytes;
	}
	
	public byte[] toBytes(Byte value) {
		if (value == null)
			return nullValueBytes;
		if (byteFormatter != null) 
			return byteFormatter.toBytes(value);
		return toBytes(value.toString());
	}

	public byte[] toBytes(Character value) {
		if (value == null)
			return nullValueBytes;
		if (charFormatter != null) 
			return charFormatter.toBytes(value);
		return toBytes(value.toString());
	}

	public byte[] toBytes(Short value) {
		if (value == null)
			return nullValueBytes;
		if (shortFormatter != null) 
			return shortFormatter.toBytes(value);
		return toBytes(value.toString());
	}

	public byte[] toBytes(Integer value) {
		if (value == null)
			return nullValueBytes;
		if (intFormatter != null) 
			return intFormatter.toBytes(value);
		return toBytes(value.toString());
	}
	
	public byte[] toBytes(Long value) {
		if (value == null)
			return nullValueBytes;
		if (longFormatter != null) 
			return longFormatter.toBytes(value);
		return toBytes(value.toString());
	}
	
	public byte[] toBytes(Double value) {
		if (value == null)
			return nullValueBytes;
		if (doubleFormatter != null) 
			return doubleFormatter.toBytes(value);
		return toBytes(value.toString());
	}
	
	public byte[] toBytes(Number value) {
		if (value == null)
			return nullValueBytes;
		if (value instanceof Byte)
			return toBytes((Byte) value);
		if (value instanceof Short)
			return toBytes((Short) value);
		if (value instanceof Integer)
			return toBytes((Integer) value);
		if (value instanceof Long)
			return toBytes((Long) value);
		if (value instanceof Float)
			return toBytes((Float) value);
		if (value instanceof Double)
			return toBytes((Double) value);
		if (numberFormatter != null) 
			return numberFormatter.toBytes(value);
		return toBytes(value.toString());
	}

	public byte[] toBytes(Date value) {
		if (value == null)
			return nullValueBytes;
		if (dateFormatter != null) 
			return dateFormatter.toBytes(value);
		return toBytes(DateUtils.format(value));
	}

	public byte[] toBytes(byte[] value) {
		if (value == null)
			return nullValueBytes;
		return value;
	}

	public byte[] toBytes(char[] value) {
		if (value == null)
			return nullValueBytes;
		return toBytes(String.valueOf(value));
	}

	public byte[] toBytes(String value) {
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

	public byte[] toBytes(Template e) throws ParseException, IOException {
		return toBytes(e.evaluate());
	}

	public byte[] toBytes(Expression e) throws ParseException, IOException {
		return toBytes(e.evaluate());
	}

	public byte[] toBytes(Resource e) throws IOException {
		return IOUtils.readToBytes(e.getInputStream());
	}

	public byte[] toBytes(Object value) throws ParseException, IOException {
		if (value == null)
			return nullValueBytes;
		if (value instanceof byte[])
			return (byte[]) value;
		if (value instanceof Boolean)
			return toBytes((Boolean) value);
		if (value instanceof Character)
			return toBytes((Character) value);
		if (value instanceof Number)
			return toBytes((Number) value);
		if (value instanceof Date)
			return toBytes((Date) value);
		if (value instanceof String)
			return toBytes((String) value);
		if (value instanceof Template)
			return toBytes((Template) value);
		if (value instanceof Expression)
			return toBytes((Expression) value);
		if (value instanceof Resource)
			return toBytes((Resource) value);
		if (formatter != null)
			return formatter.toBytes(value);
		return toBytes(StringUtils.toString(value));
	}

}