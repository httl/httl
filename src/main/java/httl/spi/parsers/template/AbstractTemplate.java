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
package httl.spi.parsers.template;

import httl.Context;
import httl.Engine;
import httl.Template;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.formatters.MultiFormatter;
import httl.util.StringUtils;
import httl.util.UnsafeByteArrayOutputStream;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Abstract template. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractTemplate implements Template, Serializable {
    
    private static final long serialVersionUID = 8780375327644594903L;

    private static final String NULL_VALUE          = "null.value";

    private static final String TRUE_VALUE          = "true.value";

    private static final String FALSE_VALUE         = "false.value";

    private static final String OUTPUT_ENCODING     = "output.encoding";

    private transient final Engine engine;
    
    private transient final Filter filter;
    
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

	private final Map<String, Template> importMacros;

	private final Map<String, Template> macros;

	@SuppressWarnings("unchecked")
    public AbstractTemplate(Engine engine, Filter filter, 
    		Formatter<?> formatter, Map<Class<?>, Object> functions,
    		Map<String, Template> importMacros) {
		this.engine = engine;
		this.filter = filter;
		this.formatter = (Formatter<Object>) formatter;
		this.importMacros = importMacros;
		this.macros = initMacros(engine, filter, formatter, functions, importMacros);
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
		this.nullValue = engine.getConfig(NULL_VALUE, "");
		this.trueValue = engine.getConfig(TRUE_VALUE, "true");
		this.falseValue = engine.getConfig(FALSE_VALUE, "false");
	}

	protected Map<String, Template> getImportMacros() {
		return importMacros;
	}

	private Map<String, Template> initMacros(Engine engine, Filter filter, 
			Formatter<?> formatter, Map<Class<?>, Object> functions,
			Map<String, Template> importMacros) {
		Map<String, Template> macros = new HashMap<String, Template>();
		Map<String, Class<?>> macroTypes = getMacroTypes();
		if (macroTypes == null || macroTypes.size() == 0) {
			return Collections.unmodifiableMap(macros);
		}
		for (Map.Entry<String, Class<?>> entry : macroTypes.entrySet()) {
			try {
				Template macro = (Template) entry.getValue()
						.getConstructor(Engine.class, Filter.class, Formatter.class, Map.class, Map.class)
						.newInstance(engine, filter, formatter, functions, importMacros);
				macros.put(entry.getKey(), macro);
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		return Collections.unmodifiableMap(macros);
	}
	
	@SuppressWarnings("unchecked")
    private static Formatter<Number> getFormatter(MultiFormatter multi, Class<? extends Number> type, Formatter<Number> defaultFormatter) {
	    Formatter<Number> formatter = multi.get((Class<Number>)type);
	    if (formatter == null) {
	        return defaultFormatter;
	    }
	    return formatter;
	}
	
	public Engine getEngine() {
		return engine;
	}

	public Map<String, Template> getMacros() {
		return macros;
	}
	
	protected abstract Map<String, Class<?>> getMacroTypes();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        String name = getName();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractTemplate other = (AbstractTemplate) obj;
        String name = getName();
        String otherName = other.getName();
        if (name == null) {
            if (otherName != null) return false;
        } else if (!name.equals(otherName)) return false;
        return true;
    }

    @Override
    public String toString() {
        return render(Context.getContext().getParameters());
    }

    protected String toString(UnsafeByteArrayOutputStream output) {
        String encoding = engine.getConfig(OUTPUT_ENCODING);
        if (encoding != null && encoding.length() > 0) {
            try {
                return new String(output.toByteArray(), encoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return new String(output.toByteArray());
        }
    }
    
    protected String filter(String value) {
        if (filter != null)
            return filter.filter(value);
        return value;
    }

    protected String format(Object value) {
        if (formatter != null)
            return formatter.format(value);
        if (value == null)
            return nullValue;
        return StringUtils.toString(value);
    }
    
    protected String format(String value) {
        if (value == null)
            return nullValue;
        return value;
    }
    
    protected String format(boolean value) {
        if (booleanFormatter != null)
            return booleanFormatter.format(value);
        return value ? trueValue : falseValue;
    }
    
    protected String format(byte value) {
        if (byteFormatter != null)
            return byteFormatter.format(value);
        return String.valueOf(value);
    }

    protected String format(char value) {
        if (charFormatter != null)
            return charFormatter.format(value);
        return String.valueOf(value);
    }

    protected String format(short value) {
        if (shortFormatter != null)
            return shortFormatter.format(value);
        return String.valueOf(value);
    }

    protected String format(int value) {
        if (intFormatter != null)
            return intFormatter.format(value);
        return String.valueOf(value);
    }

    protected String format(long value) {
        if (longFormatter != null)
            return longFormatter.format(value);
        return String.valueOf(value);
    }

    protected String format(float value) {
        if (floatFormatter != null)
            return floatFormatter.format(value);
        return String.valueOf(value);
    }
    
    protected String format(double value) {
        if (doubleFormatter != null)
            return doubleFormatter.format(value);
        return String.valueOf(value);
    }
    
    protected String format(Boolean value) {
        if (booleanFormatter != null) 
            return booleanFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.booleanValue() ? trueValue : falseValue;
    }
    
    protected String format(Byte value) {
        if (byteFormatter != null) 
            return byteFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.toString();
    }

    protected String format(Character value) {
        if (charFormatter != null) 
            return charFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.toString();
    }

    protected String format(Short value) {
        if (shortFormatter != null) 
            return shortFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.toString();
    }

    protected String format(Integer value) {
        if (intFormatter != null) 
            return intFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.toString();
    }
    
    protected String format(Long value) {
        if (longFormatter != null) 
            return longFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.toString();
    }
    
    protected String format(Double value) {
        if (doubleFormatter != null) 
            return doubleFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.toString();
    }
    
    protected String format(Number value) {
        if (numberFormatter != null) 
            return numberFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.toString();
    }
    
    protected String format(Date value) {
        if (dateFormatter != null) 
            return dateFormatter.format(value);
        if (value == null)
            return nullValue;
        return value.toString();
    }
    
    protected byte[] serialize(String value) {
        return value == null ? new byte[0] : value.getBytes();
    }

}
