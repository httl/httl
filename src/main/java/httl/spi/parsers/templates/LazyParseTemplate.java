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

import httl.Resource;
import httl.Template;
import httl.internal.util.DelegateMap;
import httl.internal.util.TypeMap;
import httl.spi.Converter;
import httl.spi.Parser;

import java.io.IOException;
import java.text.ParseException;
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

	private final Parser parser;

	private final Converter<Object, Object> mapConverter;

	private volatile Template template;

	public LazyParseTemplate(Parser parser, Resource resource, Map<String, Class<?>> parameterTypes, Converter<Object, Object> mapConverter) {
		super(resource);
		this.parameterTypes = parameterTypes;
		this.parser = parser;
		this.mapConverter = mapConverter;
	}
	
	private void init(Object context) throws IOException, ParseException {
		if (template == null) {
			synchronized (this) {
				if (template == null) {
					Map<String, Class<?>> types = context == null ? null : new TypeMap(convertMap(context));
					if (parameterTypes != null) {
						types = types == null ? parameterTypes : new DelegateMap<String, Class<?>>(types, parameterTypes);
					}
					template = parser.parse(getResource(), types);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> convertMap(Object context) throws IOException, ParseException {
		if (mapConverter != null && context != null && ! (context instanceof Map)) {
			context = mapConverter.convert(context);
		}
		if (context == null || context instanceof Map) {
			return (Map<String, Object>) context;
		} else {
			throw new IllegalArgumentException("No such Converter to convert the " + context.getClass().getName() + " to Map.");
		}
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

	@Override
	public void render(Object context, Object out) throws IOException, ParseException {
		init(context);
		template.render(context, out);
	}

}
