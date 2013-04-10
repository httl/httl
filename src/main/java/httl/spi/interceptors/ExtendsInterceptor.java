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
package httl.spi.interceptors;

import httl.Context;
import httl.Engine;
import httl.Template;
import httl.spi.Interceptor;
import httl.spi.Listener;
import httl.spi.methods.FileMethod;
import httl.spi.translators.templates.ListenerTemplate;
import httl.internal.util.Optional;
import httl.internal.util.StringUtils;
import httl.internal.util.UrlUtils;

import java.io.IOException;
import java.text.ParseException;

/**
 * Extends Interceptor. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setInterceptor(Interceptor)
 * @see httl.spi.translators.InterpretedTranslator#setInterceptor(Interceptor)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ExtendsInterceptor extends FirstInterceptor {

	private final FileMethod fileMethod = new FileMethod();
	
	private Engine engine;

	private String extendsDirectory;

	private String extendsDefault;

	private String extendsNested;

	private String extendsVariable;

	/**
	 * httl.properties: engine=httl.spi.engines.DefaultEngine
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
		fileMethod.setEngine(engine);
	}

	/**
	 * httl.properties: extends.directory=layouts
	 */
	public void setExtendsDirectory(String extendsDirectory) {
		this.extendsDirectory = UrlUtils.cleanDirectory(extendsDirectory);
		if ("/".equals(this.extendsDirectory)) {
			this.extendsDirectory = null;
		}
		fileMethod.setExtendsDirectory(extendsDirectory);
	}

	/**
	 * httl.properties: extends.default=default.httl
	 */
	@Optional
	public void setExtendsDefault(String extendsDefault) {
		this.extendsDefault = extendsDefault;
	}

	/**
	 * httl.properties: extends.variable=layout
	 */
	@Optional
	public void setExtendsVariable(String extendsVariable) {
		this.extendsVariable = extendsVariable;
	}

	/**
	 * httl.properties: extends.nested=nested
	 */
	public void setExtendsNested(String extendsNested) {
		this.extendsNested = extendsNested;
	}

	public void doRender(Context context, Listener listener) throws IOException, ParseException {
		String extendsName = null;
		// extends.varibale=layout
		// 如果上下文中有指定要继承的模板，则自动继承它。
		// 注意：此模板是从继承模板目录中查找的，即实际为：template.directory + extends.directory +　context.get(extends.varibale)
		if (StringUtils.isNotEmpty(extendsVariable)) {
			extendsName = (String) context.get(extendsVariable);
		}
		// extends.default=default.httl
		// 如果默认模板存在，则继承默认模板。
		// 注意：默认模板是从继承模板目录中查找的，即实际为：template.directory + extends.directory +　extends.default
		Template template = context.getTemplate();
		if (StringUtils.isEmpty(extendsName) && StringUtils.isNotEmpty(extendsDefault)) {
			String templateName = template.getName();
			String name = UrlUtils.relativeUrl(extendsDefault, templateName);
			if (StringUtils.isNotEmpty(extendsDirectory)) {
				name = extendsDirectory + name;
			}
			if (! name.equals(templateName) && engine.hasResource(name)) {
				extendsName = extendsDefault;
			}
		}
		if (StringUtils.isNotEmpty(extendsName)) {
			// extends.nested=nested
			Object oldNested = null;
			if (StringUtils.isNotEmpty(extendsNested)) {
				oldNested = context.put(extendsNested, new ListenerTemplate(template, listener));
			}
			try {
				Template extend = fileMethod.$extends(extendsName, template.getLocale(), template.getEncoding());
				extend.render(context.getOut());
			} finally {
				if (StringUtils.isNotEmpty(extendsNested)) {
					if (oldNested != null) {
						context.put(extendsNested, oldNested);
					} else {
						context.remove(extendsNested);
					}
				}
			}
		} else {
			listener.render(context);
		}
	}

}