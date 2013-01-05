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
import java.text.ParseException;

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

	public void render(Context context, Rendition rendition) throws IOException, ParseException {
		String name = null;
		if (extendsVariable != null && extendsVariable.length() > 0) {
			name = (String) context.get(extendsVariable);
		}
		if (extendsDirectory != null && extendsDirectory.length() > 0) {
			String directory = "/".equals(extendsDirectory) ? "" : extendsDirectory;
			if (name != null && name.length() > 0) {
				name = directory + name;
			} else {
				Template template = context.getTemplate();
				name = directory + template.getName();
				if (! engine.hasResource(name)) {
					if (extendsDefault != null && extendsDefault.length() > 0
							&& engine.hasResource(directory + extendsDefault)) {
						name = directory + extendsDefault;
					} else {
						name = null;
					}
				}
			}
		}
		if (name != null && name.length() > 0) {
			Template template = context.getTemplate();
			if (extendsNested != null && extendsNested.length() > 0) {
				context.put(extendsNested, new RenditionTemplate(template, rendition));
			}
			Template extend = fileMethod.$extends(name, template.getLocale(), template.getEncoding());
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
