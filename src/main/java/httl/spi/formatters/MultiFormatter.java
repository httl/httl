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
package httl.spi.formatters;

import httl.spi.Formatter;
import httl.util.ClassUtils;
import httl.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MultiFormatter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.parsers.AbstractParser#setFormatter(Formatter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiFormatter implements Formatter<Object> {
	
	private final Map<Class<?>, Formatter<?>> formatters = new ConcurrentHashMap<Class<?>, Formatter<?>>();

	private String outputEncoding;

	/**
	 * httl.properties: output.encoding=UTF-8
	 */
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

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
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Formatter<T> get(Class<T> type) {
		return (Formatter)formatters.get((Class)type);
	}

	@SuppressWarnings("unchecked")
	public String toString(Object value) {
		if (value == null) {
			Formatter<?> formatter = formatters.get(Void.class);
			if (formatter != null) {
				return formatter.toString(null);
			}
			return "";
		} else {
			Formatter<Object> formatter = (Formatter<Object>) formatters.get(value.getClass());
			if (formatter != null) {
				return formatter.toString(value);
			}
			return StringUtils.toString(value);
		}
	}

	@SuppressWarnings("unchecked")
	public char[] toChars(Object value) {
		if (value == null) {
			Formatter<?> formatter = formatters.get(Void.class);
			if (formatter != null) {
				return formatter.toChars(null);
			}
			return new char[0];
		} else {
			Formatter<Object> formatter = (Formatter<Object>) formatters.get(value.getClass());
			if (formatter != null) {
				return formatter.toChars(value);
			}
			return StringUtils.toString(value).toCharArray();
		}
	}

	@SuppressWarnings("unchecked")
	public byte[] toBytes(Object value) {
		if (value == null) {
			Formatter<?> formatter = formatters.get(Void.class);
			if (formatter != null) {
				return formatter.toBytes(null);
			}
			return new byte[0];
		} else {
			Formatter<Object> formatter = (Formatter<Object>) formatters.get(value.getClass());
			if (formatter != null) {
				return formatter.toBytes(value);
			}
			String str = StringUtils.toString(value);
			if (str == null) {
				return new byte[0];
			}
			if (outputEncoding == null) {
				return str.getBytes();
			}
			try {
				return str.getBytes(outputEncoding);
			} catch (UnsupportedEncodingException e) {
				return str.getBytes();
			}
		}
	}

}