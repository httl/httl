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
package httl.spi.parsers.templates;

import httl.Context;
import httl.Engine;
import httl.Template;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Switcher;
import httl.spi.converters.BeanMapConverter;
import httl.spi.formatters.MultiFormatter;
import httl.internal.util.UnsafeByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractTemplate implements Template, Serializable {
	
	private static final long serialVersionUID = 8780375327644594903L;

	private transient final Engine engine;

	private transient final Compiler compiler;

	private transient final Interceptor interceptor;

	private transient final Switcher<Filter> filterSwitcher;
	
	private transient final Switcher<Formatter<Object>> formatterSwitcher;

	private transient final Filter filter;

	private transient final Converter<Object, Object> mapConverter;

	private transient final Converter<Object, Object> outConverter;
	
	private final MultiFormatter formatter;

	private final Map<String, Template> importMacros;

	private final Map<String, Template> macros;

	public AbstractTemplate(Engine engine, Interceptor interceptor, Compiler compiler,
			Switcher<Filter> filterSwitcher, Switcher<Formatter<Object>> formatterSwitcher, 
			Filter filter, Formatter<Object> formatter, 
			Converter<Object, Object> mapConverter, Converter<Object, Object> outConverter,
			Map<Class<?>, Object> functions, Map<String, Template> importMacros) {
		this.engine = engine;
		this.compiler = compiler;
		this.interceptor = interceptor;
		this.filterSwitcher = filterSwitcher;
		this.formatterSwitcher = formatterSwitcher;
		this.filter = filter;
		this.mapConverter = mapConverter;
		this.outConverter = outConverter;
		this.formatter = toMultiFormatter(formatter);
		this.importMacros = importMacros;
		this.macros = initMacros(engine, interceptor, filterSwitcher, formatterSwitcher, 
				filter, formatter, mapConverter, outConverter, functions, importMacros);
	}

	protected Interceptor getInterceptor() {
		return interceptor;
	}

	protected MultiFormatter getFormatter(Context context, String key) {
		Object value = context.get(key);
		if (value instanceof Formatter) {
			return toMultiFormatter((Formatter<?>) value);
		}
		return formatter;
	}
	
	private MultiFormatter toMultiFormatter(Formatter<?> formatter) {
		if (formatter instanceof MultiFormatter) {
			return (MultiFormatter) formatter;
		}
		return new MultiFormatter(formatter);
	}

	protected MultiFormatter switchFormatter(String location, MultiFormatter defaultFormatter) {
		if (formatterSwitcher != null) {
			return toMultiFormatter(formatterSwitcher.switchover(location, defaultFormatter));
		}
		return defaultFormatter;
	}

	protected Filter getFilter(Context context, String key) {
		Object value = context.get(key);
		if (value instanceof Filter) {
			return (Filter) value;
		}
		return filter;
	}

	protected Filter switchFilter(String location, Filter defaultFilter) {
		if (filterSwitcher != null) {
			return filterSwitcher.switchover(location, defaultFilter);
		}
		return defaultFilter;
	}

	protected String doFilter(Filter filter, String key, String value) {
		if (filter != null)
			return filter.filter(key, value);
		return value;
	}

	protected char[] doFilter(Filter filter, String key, char[] value) {
		if (filter != null)
			return filter.filter(key, value);
		return value;
	}

	protected byte[] doFilter(Filter filter, String key, byte[] value) {
		if (filter != null)
			return filter.filter(key, value);
		return value;
	}

	protected Template getMacro(Context context, String key, Template defaultValue) {
		Object value = context.get(key);
		if (value instanceof Template) {
			return (Template) value;
		}
		return defaultValue;
	}

	private volatile Class<?> parameterType;

	public Class<?> getRootType() {
		if (parameterType == null) {
			synchronized (this) {
				if (parameterType == null) {
					try {
						parameterType = BeanMapConverter.getBeanClass(getName(), getVariableTypes(), compiler, null);
					} catch (ParseException e) {
						parameterType = void.class;
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			}
		}
		return parameterType;
	}

	public Reader getReader() throws IOException {
		return new StringReader(getSource());
	}

	public InputStream getInputStream() throws IOException {
		return new UnsafeByteArrayInputStream(getSource().getBytes(getEncoding()));
	}

	public Object evaluate() throws ParseException {
		return evaluate(null);
	}

	public void render(Object out) throws IOException, ParseException {
		render(null, out);
	}

	public Object evaluate(Object context) throws ParseException {
		if (context instanceof Context) {
			throw new IllegalArgumentException("Don't use the " + Context.class.getName() + " type as evaluate() parameters, it implicitly delivery.");
		}
		return evaluate(convertMap(context));
	}

	public void render(Object context, Object out) throws IOException, ParseException {
		if (context instanceof Context) {
			throw new IllegalArgumentException("Don't use the " + Context.class.getName() + " type as render() parameters, it implicitly delivery.");
		}
		out = convertOut(out);
		if (out == null) {
			throw new IllegalArgumentException("out == null");
		} else if (out instanceof OutputStream) {
			render(convertMap(context), (OutputStream) out);
		} else if (out instanceof Writer) {
			render(convertMap(context), (Writer) out);
		} else {
			throw new IllegalArgumentException("No such Converter to convert the " + out.getClass().getName() + " to OutputStream or Writer.");
		}
	}
	
	private Object convertOut(Object out) throws IOException, ParseException {
		if (outConverter != null && out != null
				&& ! (out instanceof OutputStream) 
				&& ! (out instanceof Writer)) {
			return outConverter.convert(out, null);
		}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> convertMap(Object context) throws ParseException {
		if (mapConverter != null && context != null && ! (context instanceof Map)) {
			try {
				context = mapConverter.convert(context, getRootType());
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		if (context == null || context instanceof Map) {
			return (Map<String, Object>) context;
		} else {
			throw new IllegalArgumentException("No such Converter to convert the " + context.getClass().getName() + " to Map.");
		}
	}

	protected abstract Object evaluate(Map<String, Object> parameters) throws ParseException;
	
	protected abstract void render(Map<String, Object> parameters, OutputStream stream) throws IOException, ParseException;
	
	protected abstract void render(Map<String, Object> parameters, Writer writer) throws IOException, ParseException;
	
	protected Map<String, Template> getImportMacros() {
		return importMacros;
	}

	private Map<String, Template> initMacros(Engine engine, Interceptor interceptor, 
			Switcher<Filter> filterSwitcher, Switcher<Formatter<Object>> formatterSwitcher, 
			Filter filter, Formatter<Object> formatter, 
			Converter<Object, Object> mapConverter, Converter<Object, Object> outConverter,
			Map<Class<?>, Object> functions, Map<String, Template> importMacros) {
		Map<String, Template> macros = new HashMap<String, Template>();
		Map<String, Class<?>> macroTypes = getMacroTypes();
		if (macroTypes == null || macroTypes.size() == 0) {
			return Collections.unmodifiableMap(macros);
		}
		for (Map.Entry<String, Class<?>> entry : macroTypes.entrySet()) {
			try {
				Template macro = (Template) entry.getValue()
						.getConstructor(Engine.class, Interceptor.class, Compiler.class, Switcher.class, Switcher.class, Filter.class, Formatter.class, Converter.class, Converter.class, Map.class, Map.class)
						.newInstance(engine, interceptor, compiler, filterSwitcher, formatterSwitcher, filter, formatter, mapConverter, outConverter, functions, importMacros);
				macros.put(entry.getKey(), macro);
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		return Collections.unmodifiableMap(macros);
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
		return getName();
	}

}