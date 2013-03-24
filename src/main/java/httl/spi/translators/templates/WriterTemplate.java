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
import httl.internal.util.UnsafeStringWriter;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Listener;
import httl.spi.Switcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

/**
 * Writer Template. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class WriterTemplate extends AbstractTemplate {
	
	private static final long serialVersionUID = 7127901461769617745L;

	public WriterTemplate(Engine engine, Interceptor interceptor, Compiler compiler, 
			Switcher<Filter> filterSwitcher, Switcher<Formatter<Object>> formatterSwitcher, 
			Filter filter, Formatter<Object> formatter, 
			Converter<Object, Object> mapConverter, Converter<Object, Object> outConverter,
			Map<Class<?>, Object> functions, Map<String, Template> importMacros,
			Resource resource, Template template, Node root){
		super(engine, interceptor, compiler, filterSwitcher, formatterSwitcher, filter, formatter, 
				mapConverter, outConverter, functions, importMacros, resource, template, root);
	}

	public Object evaluate(Map<String, Object> parameters) throws ParseException {
		UnsafeStringWriter writer = new UnsafeStringWriter();
		try {
			render(parameters, writer);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return writer.toString();
	}

	public void render(Map<String, Object> parameters, OutputStream stream) throws IOException, ParseException {
		Writer writer = new OutputStreamWriter(stream);
		render(parameters, writer);
		writer.flush();
	}

	public void render(Map<String, Object> parameters, Writer writer) throws IOException, ParseException {
		if (writer == null) 
		 	throw new IllegalArgumentException("writer == null");
		if (Context.getContext().getTemplate() == this)
			throw new IllegalStateException("The template " + getName() + " can not be recursive rendering the self template.");
		Context context = Context.pushContext(parameters).setOut(writer).setTemplate(this);
		try {
			Interceptor interceptor = getInterceptor();
			if (interceptor != null) {
				interceptor.render(context, new Listener() {
					public void render(Context context) throws IOException, ParseException {
						_render(context, (Writer) context.getOut());
					}
				});
			} else {
				_render(context, writer);
			}
		} finally {
			Context.popContext();
		}
	}

	private void _render(Context context, Writer writer) throws IOException, ParseException {
		try {
			doRender(context, writer);
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (IOException e) {
			throw (IOException) e;
		} catch (ParseException e) {
			throw (ParseException) e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	protected abstract void doRender(Context context, Writer writer) throws Exception;

}
