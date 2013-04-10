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
package httl.spi.methods;

import httl.Context;
import httl.Engine;
import httl.Resource;
import httl.Template;
import httl.internal.util.IOUtils;
import httl.internal.util.StringUtils;
import httl.internal.util.UrlUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

/**
 * FileMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class FileMethod {

	private Engine engine;

	private String extendsDirectory;

	/**
	 * httl.properties: engine=httl.spi.engines.DefaultEngine
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
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

	public Template $extends(String name) throws IOException, ParseException {
		return $extends(name, (Locale) null, (String) null);
	}

	public Template $extends(String name, String encoding) throws IOException, ParseException {
		return $extends(name, (Locale) null, encoding);
	}

	public Template $extends(String name, Locale locale) throws IOException, ParseException {
		return $extends(name, locale, (String) null);
	}

	public Template $extends(String name, Locale locale, String encoding) throws IOException, ParseException {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("include template name == null");
		}
		String macro = null;
		int i = name.indexOf('#');
		if (i > 0) {
			macro = name.substring(i + 1);
			name = name.substring(0, i);
		}
		Template template = Context.getContext().getTemplate();
		if (template != null) {
			if (StringUtils.isEmpty(encoding)) {
				encoding = template.getEncoding();
			}
			name = UrlUtils.relativeUrl(name, template.getName());
			if (locale == null) {
				locale = template.getLocale();
			}
		}
		if (StringUtils.isNotEmpty(extendsDirectory)) {
			name = extendsDirectory + name;
		}
		Template extend = engine.getTemplate(name, locale, encoding);
		if (StringUtils.isNotEmpty(macro)) {
			extend = extend.getMacros().get(macro);
		}
		if (template != null) {
			if (template == extend) {
				throw new IllegalStateException("The template " + template.getName() + " can not be recursive extending the self template.");
			}
			Context.getContext().putAll(template.getMacros());
		}
		return extend;
	}

	public Template $extends(String name, Map<String, Object> parameters) throws IOException, ParseException {
		return $extends(name, null, null, parameters);
	}

	public Template $extends(String name, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
		return $extends(name, null, encoding, parameters);
	}

	public Template $extends(String name, Locale locale, Map<String, Object> parameters) throws IOException, ParseException {
		return $extends(name, locale, null, parameters);
	}

	public Template $extends(String name, Locale locale, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
		if (parameters != null) {
			Context.getContext().putAll(parameters);
		}
		return $extends(name, locale, encoding);
	}

	public Template render(Resource resource) throws IOException, ParseException {
		return render(IOUtils.readToString(resource.getReader()));
	}

	public Template render(byte[] source) throws IOException, ParseException {
		Template template = Context.getContext().getTemplate();
		if (template == null) {
			throw new IllegalArgumentException("display context template == null");
		}
		String encoding = template.getEncoding();
		return render(encoding == null ? new String(source) : new String(source, encoding));
	}

	public Template render(char[] source) throws IOException, ParseException {
		return render(new String(source));
	}

	public Template render(String source) throws IOException, ParseException {
		Template template = Context.getContext().getTemplate();
		if (template == null) {
			throw new IllegalArgumentException("display context template == null");
		}
		return engine.parseTemplate(source);
	}

	public Template include(String name) throws IOException, ParseException {
		return include(name, (Locale) null, (String) null);
	}
	
	public Template include(String name, String encoding) throws IOException, ParseException {
		return include(name, (Locale) null, encoding);
	}

	public Template include(String name, Locale locale) throws IOException, ParseException {
		return include(name, locale, (String) null);
	}

	public Template include(String name, Locale locale, String encoding) throws IOException, ParseException {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("include template name == null");
		}
		String macro = null;
		int i = name.indexOf('#');
		if (i > 0) {
			macro = name.substring(i + 1);
			name = name.substring(0, i);
		}
		Template template = Context.getContext().getTemplate();
		if (template != null) {
			if (StringUtils.isEmpty(encoding)) {
				encoding = template.getEncoding();
			}
			name = UrlUtils.relativeUrl(name, template.getName());
			if (locale == null) {
				locale = template.getLocale();
			}
		}
		Template include = engine.getTemplate(name, locale, encoding);
		if (StringUtils.isNotEmpty(macro)) {
			include = include.getMacros().get(macro);
		}
		if (template != null && template == include) {
			throw new IllegalStateException("The template " + template.getName() + " can not be recursive including the self template.");
		}
		return include;
	}

	public Template include(String name, Map<String, Object> parameters) throws IOException, ParseException {
		return include(name, null, null, parameters);
	}
	
	public Template include(String name, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
		return include(name, null, encoding, parameters);
	}
	
	public Template include(String name, Locale locale, Map<String, Object> parameters) throws IOException, ParseException {
		return include(name, locale, null, parameters);
	}
	
	public Template include(String name, Locale locale, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
		if (parameters != null) {
			Context.getContext().putAll(parameters);
		}
		return include(name, locale, encoding);
	}

	public Resource read(String name) throws IOException, ParseException {
		return read(name, null, null);
	}

	public Resource read(String name, String encoding) throws IOException {
		return read(name, null, encoding);
	}

	public Resource read(String name, Locale locale) throws IOException {
		return read(name, locale, null);
	}

	public Resource read(String name, Locale locale, String encoding) throws IOException {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("display template name == null");
		}
		Template template = Context.getContext().getTemplate();
		if (template != null) {
			if (StringUtils.isEmpty(encoding)) {
				encoding = template.getEncoding();
			}
			name = UrlUtils.relativeUrl(name, template.getName());
			if (locale == null) {
				locale = template.getLocale();
			}
		}
		return engine.getResource(name, locale, encoding);
	}

}