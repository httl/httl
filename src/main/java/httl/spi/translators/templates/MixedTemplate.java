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

import httl.Resource;
import httl.Template;
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
	
	private final Translator compiledTranslator;

	private Template compiledTemplate;

	public MixedTemplate(Template template, Translator translator, Resource resource, Map<String, Class<?>> types) {
		super(template);
		this.compiledTranslator = translator;
		this.resource = resource;
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
	public void render(Object parameters, Object stream)
			throws IOException, ParseException {
		if (compiledTemplate != null) {
			compiledTemplate.render(parameters, stream);
			return;
		}
		Map<String, Object> map = (Map<String, Object>) parameters;
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
							compiledTemplate = compiledTranslator.translate(resource, types);
						}
					} finally {
						lock.unlock();
					}
					compiledTemplate.render(parameters, stream);
					return;
				}
			}
		}
		super.render(parameters, stream);
	}

}