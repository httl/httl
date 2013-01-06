/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.interceptors;

import httl.Context;
import httl.Engine;
import httl.Template;
import httl.spi.Interceptor;
import httl.spi.Rendition;
import httl.spi.methods.FileMethod;
import httl.spi.parsers.templates.RenditionTemplate;
import httl.util.UrlUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;

/**
 * Extends Interceptor. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.parsers.AbstractParser#setInterceptor(Interceptor)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ExtendsInterceptor implements Interceptor {

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
	}

	/**
	 * httl.properties: extends.default=default.httl
	 */
	public void setExtendsDefault(String extendsDefault) {
		this.extendsDefault = UrlUtils.cleanName(extendsDefault);
	}

	/**
	 * httl.properties: extends.variable=layout
	 */
	public void setExtendsVariable(String extendsVariable) {
		this.extendsVariable = extendsVariable;
	}

	/**
	 * httl.properties: extends.nested=nested
	 */
	public void setExtendsNested(String extendsNested) {
		this.extendsNested = extendsNested;
	}
	
	private String relativeUrl(String extendsPath, String templatePath) throws MalformedURLException {
		if (StringUtils.isNotEmpty(extendsPath)) {
			if (StringUtils.isNotEmpty(extendsDirectory)) {
				return extendsDirectory + UrlUtils.relativeUrl(extendsPath, templatePath);
			} else {
				return UrlUtils.relativeUrl(extendsPath, templatePath);
			}
		}
		return extendsPath;
	}

	public void render(Context context, Rendition rendition) throws IOException, ParseException {
		Template template = context.getTemplate();
		String templateName = template.getName();
		String extendsName = null;
		// extends.varibale=layout
		// 如果上下文中有指定要继承的模板，则自动继承它。
		// 注意：此模板是从继承模板目录中查找的，即实际为：template.directory + extends.directory +　context.get(extends.varibale)
		if (StringUtils.isNotEmpty(extendsVariable)) {
			extendsName = (String) context.get(extendsVariable);
			if (StringUtils.isNotEmpty(extendsName)) {
				extendsName = relativeUrl(extendsName, templateName);
				context.put(extendsVariable, ""); // 已继承则移除，防止父模板又拿到。
			}
		}
		// extends.directory=layouts
		// 如果自动继承目录存在，则继承其中的同名模板。
		// 注意：同名模板是从继承模板目录中查找的，即实际为：template.directory + extends.directory + template.name
		if (StringUtils.isEmpty(extendsName) 
				&& StringUtils.isNotEmpty(extendsDirectory)
				&& ! templateName.startsWith(extendsDirectory)) {
			String name = extendsDirectory + templateName;
			if (engine.hasResource(name)) {
				extendsName = name;
			}
		}
		// extends.default=default.httl
		// 如果同名继承模板不存在时，则继承默认模板。
		// 注意：默认模板是从继承模板目录中查找的，即实际为：template.directory + extends.directory +　extends.default
		if (StringUtils.isEmpty(extendsName) 
				&& StringUtils.isNotEmpty(extendsDefault)
				&& ! templateName.endsWith(extendsDefault)
				// 允许没有extends.directory时，直接在当前模板目录中查找extends.default模板。
				&& (StringUtils.isEmpty(extendsDirectory)
						|| ! templateName.startsWith(extendsDirectory))) {
			String name = relativeUrl(extendsDefault, templateName);
			if (engine.hasResource(name)) {
				extendsName = name;
			}
		}
		if (StringUtils.isNotEmpty(extendsName)) {
			// extends.nested=nested
			if (StringUtils.isNotEmpty(extendsNested)) {
				context.put(extendsNested, new RenditionTemplate(template, rendition));
			}
			Template extend = fileMethod.$extends(extendsName, template.getLocale(), template.getEncoding());
			Object output = Context.getContext().getOutput();
			if (output instanceof OutputStream) {
				extend.render((OutputStream) output);
			} else {
				extend.render((Writer) output);
			}
		} else {
			rendition.render(context);
		}
	}

}
