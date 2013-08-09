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

import httl.Node;
import httl.Resource;
import httl.Template;
import httl.spi.Converter;
import httl.spi.Logger;
import httl.spi.Translator;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MixedTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MixedTemplate extends ProxyTemplate {

	private final Map<String, Class<?>> types = new ConcurrentHashMap<String, Class<?>>();

	private final ReentrantLock lock = new ReentrantLock();

	private final Resource resource;

	private final Node root;

	private final Translator compiledTranslator;

	private final Converter<Object, Object> mapConverter;

	private final Logger logger;

	private volatile Template compiledTemplate;
	
	private volatile boolean firstWarn = true;

	public MixedTemplate(Template template, Resource resource, Node root, Map<String, Class<?>> types, 
			Translator translator, Converter<Object, Object> mapConverter, Logger logger) {
		super(template);
		this.compiledTranslator = translator;
		this.mapConverter = mapConverter;
		this.logger = logger;
		this.resource = resource;
		this.root = root;
		if (types != null) {
			this.types.putAll(types);
		}
		VariableVisitor visitor = new VariableVisitor(null, false);
		try {
			template.accept(visitor);
		} catch (Exception e) {
		}
		this.types.putAll(visitor.getVariables());
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> convertMap(Object parameters) throws IOException, ParseException {
		if (mapConverter != null && parameters != null && ! (parameters instanceof Map)) {
			parameters = mapConverter.convert(parameters, null);
		}
		if (parameters == null || parameters instanceof Map) {
			return (Map<String, Object>) parameters;
		} else {
			throw new IllegalArgumentException("No such Converter to convert the " + parameters.getClass().getName() + " to Map.");
		}
	}

	public void render(Object parameters, Object stream)
			throws IOException, ParseException {
		if (compiledTemplate != null) {
			compiledTemplate.render(parameters, stream);
			return;
		}
		Map<String, Object> map = convertMap(parameters);
		if (map != null) {
			boolean compilable = true;
			for (String key : getVariables().keySet()) {
				if (! types.containsKey(key)) {
					Object value = map.get(key);
					if (value != null) {
						types.put(key, value.getClass());
					} else {
						compilable = false;
					}
				}
			}
			if (compilable) {
				if (lock.tryLock()) {
					lock.lock();
					try {
						if (compiledTemplate == null) {
							try {
								compiledTemplate = compiledTranslator.translate(resource, root, types);
							} catch (ParseException e) {
								if (firstWarn && logger != null && logger.isWarnEnabled()) {
									firstWarn = false;
									logger.warn(e.getMessage(), e);
								}
							}
						}
					} finally {
						lock.unlock();
					}
					if (compiledTemplate != null) {
						compiledTemplate.render(parameters, stream);
					}
					return;
				}
			}
		}
		super.render(parameters, stream);
	}

}