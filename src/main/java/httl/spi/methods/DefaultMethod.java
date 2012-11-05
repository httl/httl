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
package httl.spi.methods;

import httl.Context;
import httl.Engine;
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.util.ClassUtils;
import httl.util.DateUtils;
import httl.util.IOUtils;
import httl.util.MD5;
import httl.util.NumberUtils;
import httl.util.StringUtils;
import httl.util.UrlUtils;
import httl.util.WrappedMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

/**
 * DefaultMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DefaultMethod {

    private static final Random RANDOM = new Random();

    private Engine engine;

    private TimeZone timeZone;

    private String dateFormat;

    private String numberFormat;

    private String outputEncoding;

	private String[] importPackages;

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setTimeZone(String timeZone) {
    	this.timeZone = TimeZone.getTimeZone(timeZone);
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

    public void setImportPackages(String[] importPackages) {
        this.importPackages = importPackages;
    }

    public static Date now() {
        return new Date();
    }

    public static int random() {
        return RANDOM.nextInt();
    }

    public static UUID uuid() {
        return UUID.randomUUID();
    }

    public String include(String name) throws IOException, ParseException {
        return include(name, null, null);
    }

    public String include(String name, String encoding) throws IOException, ParseException {
    	return include(name, encoding, null);
    }

    public String include(String name, Map<String, Object> parameters) throws IOException, ParseException {
    	return include(name, null, parameters);
    }

    public String include(String name, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
        Template template = parse(name, encoding);
        Map<String, Object> map = Context.getContext().getParameters();
        if (parameters != null) {
        	map = new WrappedMap<String, Object>(map, parameters);
        }
        return template.render(map);
    }

    public String read(String name) throws IOException, ParseException {
        return read(name, null);
    }

    public String read(String name, String encoding) throws IOException {
        return IOUtils.readToString(load(name, encoding).getSource());
    }

    public Object evaluate(String expr) throws ParseException {
        return translate(expr).evaluate(Context.getContext().getParameters());
    }

    public Object render(String source) throws IOException, ParseException {
        Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("display context template == null");
        }
        String name = template.getName() + "$" + MD5.getMD5(source);
        engine.addResource(name, source);
        return engine.getTemplate(name).render(Context.getContext().getParameters());
    }

    public Template parse(String name) throws IOException, ParseException {
        return parse(name, null);
    }

    public Template parse(String name, String encoding) throws IOException, ParseException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("include template name == null");
        }
        String macro = null;
		int i = name.indexOf('#');
        if (i > 0) {
        	macro = name.substring(i + 1);
        	name = name.substring(0, i);
        }
        Template template = Context.getContext().getTemplate();
        if (template != null) {
            if (encoding == null || encoding.length() == 0) {
                encoding = template.getEncoding();
            }
            name = UrlUtils.relativeUrl(name, template.getName());
        }
        template = engine.getTemplate(name, encoding);
        if (macro != null && macro.length() > 0) {
			return template.getMacros().get(macro);
		}
        return template;
    }

    public Resource load(String name) throws IOException, ParseException {
        return load(name, null);
    }

    public Resource load(String name, String encoding) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("display template name == null");
        }
        Template template = Context.getContext().getTemplate();
        if (template != null) {
            if (encoding == null || encoding.length() == 0) {
                encoding = template.getEncoding();
            }
            name = UrlUtils.relativeUrl(name, template.getName());
        }
        return engine.getResource(name, encoding);
    }

    public Expression translate(String expr) throws ParseException {
    	Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("display context template == null");
        }
    	return engine.getExpression(expr, template.getParameterTypes());
    }

    public static String escapeString(String value) {
        return StringUtils.escapeString(value);
    }

    public static String unescapeString(String value) {
        return StringUtils.unescapeString(value);
    }

    public static String escapeHtml(String value) {
        return StringUtils.escapeHtml(value);
    }

    public static String unescapeHtml(String value) {
        return StringUtils.unescapeHtml(value);
    }

    public static String escapeXml(String value) {
        return StringUtils.escapeXml(value);
    }

    public static String unescapeXml(String value) {
        return StringUtils.unescapeXml(value);
    }

    public String escapeUrl(String value) {
    	return escapeUrl(value, outputEncoding);
    }

    public static String escapeUrl(String value, String encoding) {
        try {
			return value == null ? null : URLEncoder.encode(value, encoding);
		} catch (UnsupportedEncodingException e) {
			return value;
		}
    }

    public String unescapeUrl(String value) {
    	return unescapeUrl(value, outputEncoding);
    }

    public static String unescapeUrl(String value, String encoding) {
        try {
			return value == null ? null : URLDecoder.decode(value, encoding);
		} catch (UnsupportedEncodingException e) {
			return value;
		}
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
        return value == null || value.length() == 0 ? false : Boolean.parseBoolean(value);
    }

    public static char toChar(String value) {
        return value == null || value.length() == 0 ? '\0' : value.charAt(0);
    }

    public static byte toByte(String value) {
        return value == null || value.length() == 0 ? 0 : Byte.parseByte(value);
    }

    public static short toShort(String value) {
        return value == null || value.length() == 0 ? 0 : Short.parseShort(value);
    }

    public static int toInt(String value) {
        return value == null || value.length() == 0 ? 0 : Integer.parseInt(value);
    }

    public static long toLong(String value) {
        return value == null || value.length() == 0 ? 0 : Long.parseLong(value);
    }

    public static float toFloat(String value) {
        return value == null || value.length() == 0 ? 0 : Float.parseFloat(value);
    }

    public static double toDouble(String value) {
        return value == null || value.length() == 0 ? 0 : Double.parseDouble(value);
    }

    public static Class<?> toClass(String value) {
        return value == null || value.length() == 0 ? null : ClassUtils.forName(value);
    }

    public Object[] toArray(Collection<?> value, String type) {
        Class<?> cls = ClassUtils.forName(importPackages, type);
        return value.toArray((Object[])Array.newInstance(cls, 0));
    }

    public Date toDate(String value) {
        try {
            return value == null || value.length() == 0 ? null : DateUtils.parse(value, dateFormat, timeZone);
        } catch (Exception e) {
            try {
                return DateUtils.parse(value, "yyyy-MM-dd");
            } catch (Exception e2) {
                return DateUtils.parse(value, "yyyy-MM-dd HH:mm:ss");
            }
        }
    }

    public Date toDate(String value, String format) {
        return value == null || value.length() == 0 ? null : DateUtils.parse(value, format, timeZone);
    }

    public static Date toDate(String value, String format, String timeZone) {
        return value == null || value.length() == 0 ? null : DateUtils.parse(value, format, timeZone == null ? null : TimeZone.getTimeZone(timeZone));
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

    public static Cycle toCycle(Collection<?> values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(Object[] values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(boolean[] values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(char[] values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(byte[] values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(short[] values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(int[] values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(long[] values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(float[] values) {
        return new Cycle(values);
    }

    public static Cycle toCycle(double[] values) {
        return new Cycle(values);
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

    public static String repeat(String value, int count) {
        if (value == null || value.length() == 0 || count <= 0) {
            return value;
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < count; i ++) {
            buf.append(value);
        }
        return buf.toString();
    }

}
