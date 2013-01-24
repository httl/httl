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
import httl.util.StringUtils;

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

	private transient final String outputEncoding;
	
	@SuppressWarnings("unchecked")
	public TemplateFormatter(Engine engine, Formatter<?> formatter) {
		this.formatter = (Formatter<Object>) formatter;
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
		this.outputEncoding = engine.getProperty(OUTPUT_ENCODING, String.class);
	}

	@SuppressWarnings("unchecked")
	private static Formatter<Number> getFormatter(MultiFormatter multi, Class<? extends Number> type, Formatter<Number> defaultFormatter) {
		Formatter<Number> formatter = multi.get((Class<Number>)type);
		if (formatter == null) {
			return defaultFormatter;
		}
		return formatter;
	}
	
	public String format(boolean value) {
		if (booleanFormatter != null)
			return booleanFormatter.toString(value);
		return value ? trueValue : falseValue;
	}
	
	public String format(byte value) {
		if (byteFormatter != null)
			return byteFormatter.toString(value);
		return String.valueOf(value);
	}

	public String format(char value) {
		if (charFormatter != null)
			return charFormatter.toString(value);
		return String.valueOf(value);
	}

	public String format(short value) {
		if (shortFormatter != null)
			return shortFormatter.toString(value);
		return String.valueOf(value);
	}

	public String format(int value) {
		if (intFormatter != null)
			return intFormatter.toString(value);
		return String.valueOf(value);
	}

	public String format(long value) {
		if (longFormatter != null)
			return longFormatter.toString(value);
		return String.valueOf(value);
	}

	public String format(float value) {
		if (floatFormatter != null)
			return floatFormatter.toString(value);
		return String.valueOf(value);
	}
	
	public String format(double value) {
		if (doubleFormatter != null)
			return doubleFormatter.toString(value);
		return String.valueOf(value);
	}
	
	public String format(Boolean value) {
		if (value == null)
			return nullValue;
		if (booleanFormatter != null) 
			return booleanFormatter.toString(value);
		return value.booleanValue() ? trueValue : falseValue;
	}
	
	public String format(Byte value) {
		if (value == null)
			return nullValue;
		if (byteFormatter != null) 
			return byteFormatter.toString(value);
		return value.toString();
	}

	public String format(Character value) {
		if (value == null)
			return nullValue;
		if (charFormatter != null) 
			return charFormatter.toString(value);
		return value.toString();
	}

	public String format(Short value) {
		if (value == null)
			return nullValue;
		if (shortFormatter != null) 
			return shortFormatter.toString(value);
		return value.toString();
	}

	public String format(Integer value) {
		if (value == null)
			return nullValue;
		if (intFormatter != null) 
			return intFormatter.toString(value);
		return value.toString();
	}
	
	public String format(Long value) {
		if (value == null)
			return nullValue;
		if (longFormatter != null) 
			return longFormatter.toString(value);
		return value.toString();
	}
	
	public String format(Double value) {
		if (value == null)
			return nullValue;
		if (doubleFormatter != null) 
			return doubleFormatter.toString(value);
		return value.toString();
	}
	
	public String format(Number value) {
		if (value == null)
			return nullValue;
		if (value instanceof Byte)
			return format((Byte) value);
		if (value instanceof Short)
			return format((Short) value);
		if (value instanceof Integer)
			return format((Integer) value);
		if (value instanceof Long)
			return format((Long) value);
		if (value instanceof Float)
			return format((Float) value);
		if (value instanceof Double)
			return format((Double) value);
		if (numberFormatter != null) 
			return numberFormatter.toString(value);
		return value.toString();
	}

	public String format(Date value) {
		if (value == null)
			return nullValue;
		if (dateFormatter != null) 
			return dateFormatter.toString(value);
		return value.toString();
	}

	public String format(byte[] value) {
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

	public String format(String value) {
		if (value == null)
			return nullValue;
		return value;
	}

	public String format(Template e) throws ParseException {
		return format(e.evaluate());
	}

	public String format(Expression e) throws ParseException {
		return format(e.evaluate());
	}

	public String format(Resource e) {
		return e.getSource();
	}

	public String format(Object value) throws ParseException {
		if (value == null)
			return nullValue;
		if (value instanceof String)
			return (String) value;
		if (value instanceof Boolean)
			return format((Boolean) value);
		if (value instanceof Character)
			return format((Character) value);
		if (value instanceof Number)
			return format((Number) value);
		if (value instanceof Date)
			return format((Date) value);
		if (value instanceof byte[])
			return format((byte[]) value);
		if (value instanceof Template)
			return format((Template) value);
		if (value instanceof Expression)
			return format((Expression) value);
		if (value instanceof Resource)
			return format((Resource) value);
		if (formatter != null)
			return formatter.toString(value);
		return StringUtils.toString(value);
	}

	public byte[] serialize(String value) {
		if (value == null)
			return null;
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

}