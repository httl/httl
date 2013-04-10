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
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.Visitor;
import httl.internal.util.DelegateMap;
import httl.internal.util.TypeMap;
import httl.spi.Converter;
import httl.spi.Translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * LazyParseTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class LazyTemplate implements Template {

	private final Map<String, Class<?>> parameterTypes;

	private final Resource resource;

	private final Translator translator;

	private final Converter<Object, Object> mapConverter;

	private volatile Template template;

	public LazyTemplate(Translator translator, Resource resource, Map<String, Class<?>> parameterTypes, Converter<Object, Object> mapConverter) {
		this.translator = translator;
		this.resource = resource;
		this.parameterTypes = parameterTypes;
		this.mapConverter = mapConverter;
	}

	private void init(Object context) throws IOException, ParseException {
		if (template == null) {
			synchronized (this) {
				if (template == null) {
					Map<String, Class<?>> types = context == null ? null : new TypeMap(convertMap(context));
					types = new DelegateMap<String, Class<?>>(types, parameterTypes);
					template = translator.translate(resource, types);
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

	public void render(Object context, Object out) throws IOException, ParseException {
		init(context);
		template.render(context, out);
	}

	public String getName() {
		if (template == null)
			return resource.getName();
		return template.getName();
	}

	public String getEncoding() {
		if (template == null)
			return resource.getEncoding();
		return template.getEncoding();
	}

	public Locale getLocale() {
		if (template == null)
			return resource.getLocale();
		return template.getLocale();
	}

	public long getLastModified() {
		if (template == null)
			return resource.getLastModified();
		return template.getLastModified();
	}

	public long getLength() {
		if (template == null)
			return resource.getLength();
		return template.getLength();
	}

	public String getSource() throws IOException {
		if (template == null)
			return resource.getSource();
		return template.getSource();
	}

	public Reader getReader() throws IOException {
		if (template == null)
			return resource.getReader();
		return template.getReader();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Class<?>> getVariables() {
		if (template == null)
			return parameterTypes == null ? Collections.EMPTY_MAP : parameterTypes;
		return template.getVariables();
	}

	public InputStream getInputStream() throws IOException {
		if (template == null)
			return resource.getInputStream();
		return template.getInputStream();
	}

	public int getOffset() {
		if (template == null)
			return 0;
		return template.getOffset();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Template> getMacros() {
		if (template == null)
			return Collections.EMPTY_MAP;
		return template.getMacros();
	}

	public boolean isMacro() {
		if (template == null)
			return false;
		return template.isMacro();
	}

	public Engine getEngine() {
		if (template == null)
			return resource.getEngine();
		return template.getEngine();
	}

	public void accept(Visitor visitor) throws IOException, ParseException {
		if (template != null) {
			template.accept(visitor);
		}
	}

	public Template getParent() {
		if (template == null)
			return null;
		return template.getParent();
	}

	@SuppressWarnings("unchecked")
	public List<Node> getChildren() {
		if (template == null)
			return Collections.EMPTY_LIST;
		return template.getChildren();
	}

}