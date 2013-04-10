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
import httl.internal.util.UnsafeByteArrayOutputStream;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Switcher;

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
public abstract class OutputStreamTemplate extends CompiledTemplate {

	public OutputStreamTemplate(Engine engine, Interceptor interceptor, Compiler compiler, 
			Switcher<Filter> filterSwitcher, Switcher<Formatter<Object>> formatterSwitcher, 
			Filter filter, Formatter<Object> formatter, 
			Converter<Object, Object> mapConverter, Converter<Object, Object> outConverter,
			Map<Class<?>, Object> functions, Map<String, Template> importMacros,
			Resource resource, Template template, Node root){
		super(engine, interceptor, compiler, filterSwitcher, formatterSwitcher, filter, formatter, 
				mapConverter, outConverter, functions, importMacros, resource, template, root);
	}

	@Override
	public Object evaluate(Object parameters) throws ParseException {
		UnsafeByteArrayOutputStream output = new UnsafeByteArrayOutputStream();
		try {
			render(parameters, output);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return output.toByteArray();
	}

	protected void doRender(Context context, Writer writer) throws Exception {
		throw new UnsupportedOperationException("Unsupported out type " + OutputStream.class.getName() 
				+ " in compiled " + Writer.class.getName() + " template.");
	}

}