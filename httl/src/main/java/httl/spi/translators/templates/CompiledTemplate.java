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
package httl.spi.translators.templates;

import httl.Context;
import httl.Engine;
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Switcher;
import httl.spi.formatters.MultiFormatter;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * CompiledTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class CompiledTemplate extends AbstractTemplate {
	
	private final Compiler compiler;

	private final Switcher<Filter> filterSwitcher;
	
	private final Switcher<Formatter<Object>> formatterSwitcher;

	private final Filter filter;

	private final MultiFormatter formatter;

	private final Map<String, Template> importMacros;

	private final Map<String, Template> macros;

	public CompiledTemplate(Engine engine, Interceptor interceptor, Compiler compiler,
			Switcher<Filter> filterSwitcher, Switcher<Formatter<Object>> formatterSwitcher, 
			Filter filter, Formatter<Object> formatter, 
			Converter<Object, Object> mapConverter, Converter<Object, Object> outConverter,
			Map<Class<?>, Object> functions, Map<String, Template> importMacros,
			Resource resource, Template parent, Node root) {
		super(resource, root, parent);
		super.setMapConverter(mapConverter);
		super.setOutConverter(outConverter);
		super.setInterceptor(interceptor);
		this.compiler = compiler;
		this.filterSwitcher = filterSwitcher;
		this.formatterSwitcher = formatterSwitcher;
		this.filter = filter;
		this.formatter = toMultiFormatter(formatter);
		this.importMacros = importMacros;
		this.macros = initMacros(engine, interceptor, filterSwitcher, formatterSwitcher, 
				filter, formatter, mapConverter, outConverter, functions, importMacros, 
				resource, parent, root);
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

	@Override
	protected void doRender(Context context) throws Exception {
		if (context.getOut() instanceof OutputStream) {
			doRender(context, (OutputStream) context.getOut());
		} else {
			doRender(context, (Writer) context.getOut());
		}
	}

	protected abstract void doRender(Context context, OutputStream stream) throws Exception;

	protected abstract void doRender(Context context, Writer writer) throws Exception;

	protected Map<String, Template> getImportMacros() {
		return importMacros;
	}

	private Map<String, Template> initMacros(Engine engine, Interceptor interceptor, 
			Switcher<Filter> filterSwitcher, Switcher<Formatter<Object>> formatterSwitcher, 
			Filter filter, Formatter<Object> formatter, 
			Converter<Object, Object> mapConverter, Converter<Object, Object> outConverter,
			Map<Class<?>, Object> functions, Map<String, Template> importMacros,
			Resource resource, Template parent, Node root) {
		Map<String, Template> macros = new HashMap<String, Template>();
		Map<String, Class<?>> macroTypes = getMacroTypes();
		if (macroTypes == null || macroTypes.size() == 0) {
			return Collections.unmodifiableMap(macros);
		}
		for (Map.Entry<String, Class<?>> entry : macroTypes.entrySet()) {
			try {
				Template macro = (Template) entry.getValue()
						.getConstructor(Engine.class, Interceptor.class, Compiler.class, Switcher.class, Switcher.class, Filter.class, 
								Formatter.class, Converter.class, Converter.class, Map.class, Map.class, Resource.class, Template.class, Node.class)
						.newInstance(engine, interceptor, compiler, filterSwitcher, formatterSwitcher, filter, formatter, 
								mapConverter, outConverter, functions, importMacros, resource, parent, root);
				macros.put(entry.getKey(), macro);
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		return Collections.unmodifiableMap(macros);
	}

	public Map<String, Template> getMacros() {
		return macros;
	}
	
	protected abstract Map<String, Class<?>> getMacroTypes();

}