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
package httl.spi.formatters;

import httl.spi.Configurable;
import httl.spi.Formatter;
import httl.util.ClassUtils;
import httl.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * MultiFormatter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setFormatter(Formatter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiFormatter implements Formatter<Object>, Configurable {
    
    private final Map<Class<?>, Formatter<?>> templateFormatters = new ConcurrentHashMap<Class<?>, Formatter<?>>();
    
    public void configure(Map<String, String> config) {
        String value = config.get(FORMATTERS);
        if (value != null && value.trim().length() > 0) {
            String[] values = value.trim().split("[\\s\\,]+");
            Formatter<?>[] formatters = new Formatter<?>[values.length];
            for (int i = 0; i < values.length; i ++) {
                formatters[i] = (Formatter<?>) ClassUtils.newInstance(values[i]);
                if (formatters[i] instanceof Configurable) {
                    ((Configurable)formatters[i]).configure(config);
                }
            }
            add(formatters);
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Formatter<T> get(Class<T> type) {
        return (Formatter)templateFormatters.get((Class)type);
    }
    
    public void add(Formatter<?>... formatters) {
        if (formatters != null && formatters.length > 0) {
            for (Formatter<?> formatter : formatters) {
                if (formatter != null) {
                    Class<?> type = ClassUtils.getGenericClass(formatter.getClass());
                    if (type != null) {
                        templateFormatters.put(type, formatter);
                    }
                }
            }
        }
    }
    
    public void remove(Formatter<?>... formatters) {
        if (formatters != null && formatters.length > 0) {
            for (Formatter<?> formatter : formatters) {
                if (formatter != null) {
                    Class<?> type = ClassUtils.getGenericClass(formatter.getClass());
                    if (type != null) {
                        if (templateFormatters.get(type) == formatter) {
                            templateFormatters.remove(type);
                        }
                    }
                }
            }
        }
    }
    
    public void clear() {
        templateFormatters.clear();
    }

    @SuppressWarnings("unchecked")
    public String format(Object value) {
        if (value == null) {
            Formatter<?> formatter = templateFormatters.get(Void.class);
            if (formatter != null) {
                return formatter.format(null);
            }
            return null;
        } else {
            Formatter<Object> formatter = (Formatter<Object>) templateFormatters.get(value.getClass());
            if (formatter != null) {
                return formatter.format(value);
            }
            return StringUtils.toString(value);
        }
    }
    
    public Class<? extends Object>[] getSupported() {
        return new Class<?>[]{ Object.class };
    }
    
}
