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
package httl.spi.functions;

import httl.Context;
import httl.Resource;
import httl.Template;
import httl.spi.Configurable;
import httl.spi.runtime.Cycle;
import httl.util.ClassUtils;
import httl.util.DateUtils;
import httl.util.IOUtils;
import httl.util.NumberUtils;
import httl.util.StringUtils;
import httl.util.UrlUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


/**
 * DefaultFunction. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#addFunctions(Object...)
 * @see httl.Engine#setFunctions(Object...)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DefaultFunction implements Configurable {
    
    private static final Random RANDOM = new Random();
    
    private String dateFormat;

    private String numberFormat;

    protected String[] importPackages;

    public void configure(Map<String, String> config) {
        String format = config.get(DATE_FORMAT);
        if (format != null && format.trim().length() > 0) {
            format = format.trim();
            new SimpleDateFormat(format).format(new Date());
            this.dateFormat = format;
        }
        format = config.get(NUMBER_FORMAT);
        if (format != null && format.trim().length() > 0) {
            format = format.trim();
            new DecimalFormat(format).format(0);
            this.numberFormat = format;
        }
        String packages = config.get(IMPORT_PACKAGES);
        if (packages != null && packages.trim().length() > 0) {
            importPackages = packages.trim().split("\\s*\\,\\s*");
        }
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

    public static String include(String name) throws IOException, ParseException {
        return include(name, null);
    }
    
    public static String include(String name, String encoding) throws IOException, ParseException {
        return parse(name, encoding).render();
    }
    
    public static String read(String name) throws IOException, ParseException {
        return read(name, null);
    }
    
    public static String read(String name, String encoding) throws IOException {
        return IOUtils.readToString(load(name, encoding).getSource());
    }
    
    public static Template parse(String name) throws IOException, ParseException {
        return parse(name, null);
    }
    
    public static Template parse(String name, String encoding) throws IOException, ParseException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("include template name == null");
        }
        Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("include context template == null");
        }
        if (encoding == null || encoding.length() == 0) {
            encoding = template.getEncoding();
        }
        name = UrlUtils.relativeUrl(name, template.getName());
        return template.getEngine().getTemplate(name, encoding);
    }
    
    public static Resource load(String name) throws IOException, ParseException {
        return load(name, null);
    }
    
    public static Resource load(String name, String encoding) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("display template name == null");
        }
        Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("display context template == null");
        }
        if (encoding == null || encoding.length() == 0) {
            encoding = template.getEncoding();
        }
        name = UrlUtils.relativeUrl(name, template.getName());
        return template.getEngine().getResource(name, encoding);
    }
    
    public static Object evaluate(String expr) throws ParseException {
        Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("display context template == null");
        }
        return template.getEngine().getExpression(expr, template.getParameterTypes(), 0).evaluate(Context.getContext().getParameters());
    }
    
    public static String escapeString(String value) {
        return StringUtils.escapeString(value);
    }

    public static String escapeHtml(String value) {
        return StringUtils.escapeHtml(value);
    }
    
    public static boolean toBoolean(String value) {
        return Boolean.parseBoolean(value);
    }
    
    public static char toChar(String value) {
        return value == null || value.length() == 0 ? '\0' : value.charAt(0);
    }
    
    public static byte toByte(String value) {
        return Byte.parseByte(value);
    }

    public static short toShort(String value) {
        return Short.parseShort(value);
    }

    public static int toInt(String value) {
        return Integer.parseInt(value);
    }

    public static long toLong(String value) {
        return Long.parseLong(value);
    }

    public static float toFloat(String value) {
        return Float.parseFloat(value);
    }

    public static double toDouble(String value) {
        return Double.parseDouble(value);
    }
    
    public static Class<?> toClass(String value) {
        return ClassUtils.forName(value);
    }
    
    public Object[] toArray(Collection<?> value, String type) {
        Class<?> cls = ClassUtils.forName(importPackages, type);
        return value.toArray((Object[])Array.newInstance(cls, 0));
    }
    
    public Date toDate(String value) {
        try {
            return DateUtils.toDate(value, dateFormat);
        } catch (Exception e) {
            try {
                return DateUtils.toDate(value, "yyyy-MM-dd");
            } catch (Exception e2) {
                return DateUtils.toDate(value, "yyyy-MM-dd HH:mm:ss");
            }
        }
    }
    
    public static Date toDate(String value, String format) {
        return DateUtils.toDate(value, format);
    }
    
    public String toString(Date value) {
        return DateUtils.formatDate(value, dateFormat);
    }
    
    public static String format(Date value, String format) {
        return DateUtils.formatDate(value, format);
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
        return NumberUtils.formatNumber(value, format);
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
