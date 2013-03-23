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

import httl.Engine;
import httl.Resource;
import httl.Template;
import httl.internal.util.DelegateMap;
import httl.internal.util.TypeMap;
import httl.spi.Converter;
import httl.spi.Translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

/**
 * LazyParseTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class LazyParseTemplate extends ResourceTemplate {

	private final Map<String, Class<?>> parameterTypes;

	private final Translator translator;

	private final Converter<Object, Object> mapConverter;

	private volatile Template template;

	public LazyParseTemplate(Translator translator, Resource resource, Map<String, Class<?>> parameterTypes, Converter<Object, Object> mapConverter) {
		super(resource);
		this.parameterTypes = parameterTypes;
		this.translator = translator;
		this.mapConverter = mapConverter;
	}
	
	private void init(Object context) throws IOException, ParseException {
		if (template == null) {
			synchronized (this) {
				if (template == null) {
					Map<String, Class<?>> types = context == null ? null : new TypeMap(convertMap(context));
					types = new DelegateMap<String, Class<?>>(types, parameterTypes);
					template = translator.translate(getResource(), types);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> convertMap(Object context) throws IOException, ParseException {
		if (mapConverter != null && context != null && ! (context instanceof Map)) {
			context = mapConverter.convert(context, null);
		}
		if (context == null || context instanceof Map) {
			return (Map<String, Object>) context;
		} else {
			throw new IllegalArgumentException("No such Converter to convert the " + context.getClass().getName() + " to Map.");
		}
	}

	public Object evaluate() throws ParseException {
		return evaluate(null);
	}

	@Override
	public Object evaluate(Object context) throws ParseException {
		try {
			init(context);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return template.evaluate(context);
	}

	public void render(Object out) throws IOException, ParseException {
		render(null, out);
	}

	@Override
	public void render(Object context, Object out) throws IOException, ParseException {
		init(context);
		template.render(context, out);
	}

	public String getName() {
		if (template == null)
			return super.getName();
		return template.getName();
	}

	public String getEncoding() {
		if (template == null)
			return super.getEncoding();
		return template.getEncoding();
	}

	public Locale getLocale() {
		if (template == null)
			return super.getLocale();
		return template.getLocale();
	}

	public long getLastModified() {
		if (template == null)
			return super.getLastModified();
		return template.getLastModified();
	}

	public long getLength() {
		if (template == null)
			return super.getLength();
		return template.getLength();
	}

	public String getSource() {
		if (template == null)
			return super.getSource();
		return template.getSource();
	}

	public Reader getReader() throws IOException {
		if (template == null)
			return super.getReader();
		return template.getReader();
	}

	public Map<String, Class<?>> getVariableTypes() {
		if (template == null)
			return parameterTypes == null ? super.getVariableTypes() : parameterTypes;
		return template.getVariableTypes();
	}

	public Class<?> getReturnType() {
		if (template == null)
			return super.getReturnType();
		return template.getReturnType();
	}

	public InputStream getInputStream() throws IOException {
		if (template == null)
			return super.getInputStream();
		return template.getInputStream();
	}

	public String getCode() {
		if (template == null)
			return super.getCode();
		return template.getCode();
	}

	public Map<String, Class<?>> getExportTypes() {
		if (template == null)
			return super.getExportTypes();
		return template.getExportTypes();
	}

	public int getOffset() {
		if (template == null)
			return super.getOffset();
		return template.getOffset();
	}

	public Map<String, Template> getMacros() {
		if (template == null)
			return super.getMacros();
		return template.getMacros();
	}

	public boolean isMacro() {
		if (template == null)
			return super.isMacro();
		return template.isMacro();
	}

	public Engine getEngine() {
		if (template == null)
			return super.getEngine();
		return template.getEngine();
	}

}
