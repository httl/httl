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

import httl.Context;
import httl.Engine;
import httl.Template;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Listener;
import httl.spi.Switcher;
import httl.util.UnsafeByteArrayOutputStream;
import httl.util.WriterOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

/**
 * OutputStream Template. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class OutputStreamTemplate extends AbstractTemplate {

	private static final long serialVersionUID = 7127901461769617745L;

	public OutputStreamTemplate(Engine engine, Interceptor interceptor, 
			Switcher<Filter> switcher, Filter filter, Formatter<?> formatter, 
			Converter<Object, Object> mapConverter, Converter<Object, Object> outConverter,
			Map<Class<?>, Object> functions, Map<String, Template> importMacros){
		super(engine, interceptor, switcher, filter, formatter, mapConverter, outConverter, functions, importMacros);
	}
	
	public Class<?> getReturnType() {
		return byte[].class;
	}

	public Object evaluate(Map<String, Object> parameters) throws ParseException {
		UnsafeByteArrayOutputStream output = new UnsafeByteArrayOutputStream();
		try {
			render(parameters, output);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return output.toByteArray();
	}

	public void render(Map<String, Object> parameters, Writer writer) throws IOException, ParseException {
		OutputStream output = new WriterOutputStream(writer);
		render(parameters, output);
		output.flush();
	}

	public void render(Map<String, Object> parameters, OutputStream stream) throws IOException, ParseException {
		if (stream == null) 
			throw new IllegalArgumentException("output == null");
		if (Context.getContext().getTemplate() == this)
			throw new IllegalStateException("The template " + getName() + " can not be recursive rendering the self template.");
		Context context = Context.pushContext(this, parameters, stream);
		try {
			Interceptor interceptor = getInterceptor();
			if (interceptor != null) {
				interceptor.render(context, new Listener() {
					public void render(Context context) throws IOException, ParseException {
						_render(context, (OutputStream) context.getOut());
					}
				});
			} else {
				_render(context, stream);
			}
		} finally {
			Context.popContext();
		}
	}

	private void _render(Context context, OutputStream stream) throws IOException, ParseException {
		try {
			doRender(context, stream);
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

	protected abstract void doRender(Context context, OutputStream stream) throws Exception;

}