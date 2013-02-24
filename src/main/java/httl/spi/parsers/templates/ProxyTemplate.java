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
package httl.spi.parsers.templates;

import httl.Engine;
import httl.Template;
import httl.internal.util.UnsafeByteArrayOutputStream;
import httl.internal.util.UnsafeStringWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

/**
 * ProxyTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ProxyTemplate implements Template {

	private final Template template;

	public ProxyTemplate(Template template) {
		this.template = template;
	}

	public Object evaluate() throws ParseException {
		return evaluate(null);
	}

	public Object evaluate(Object parameters)
			throws ParseException {
		if (byte[].class.equals(getReturnType())) {
			UnsafeByteArrayOutputStream output = new UnsafeByteArrayOutputStream();
			try {
				render(parameters, output);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return output.toByteArray();
		} else {
			UnsafeStringWriter writer = new UnsafeStringWriter();
			try {
				render(parameters, writer);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return writer.toString();
		}
	}

	public void render(Object stream) throws IOException, ParseException {
		render(null, stream);
	}

	public void render(Object parameters, Object stream)
			throws IOException, ParseException {
		template.render(parameters, stream);
	}

	public void render(Map<String, Object> parameters, Writer writer)
			throws IOException, ParseException {
		template.render(parameters, writer);
	}

	public String getName() {
		return template.getName();
	}

	public String getEncoding() {
		return template.getEncoding();
	}

	public Locale getLocale() {
		return template.getLocale();
	}

	public long getLastModified() {
		return template.getLastModified();
	}

	public long getLength() {
		return template.getLength();
	}

	public String getSource() {
		return template.getSource();
	}

	public Reader getReader() throws IOException {
		return template.getReader();
	}

	public Class<?> getRootType() {
		return template.getRootType();
	}

	public Map<String, Class<?>> getVariableTypes() {
		return template.getVariableTypes();
	}

	public Class<?> getReturnType() {
		return template.getReturnType();
	}

	public String getCode() {
		return template.getCode();
	}

	public InputStream getInputStream() throws IOException {
		return template.getInputStream();
	}

	public int getOffset() {
		return template.getOffset();
	}

	public Engine getEngine() {
		return template.getEngine();
	}

	public Map<String, Class<?>> getExportTypes() {
		return template.getExportTypes();
	}

	public Template getMacro(String name) {
		return template.getMacro(name);
	}

	public Map<String, Template> getMacros() {
		return template.getMacros();
	}

	public boolean isMacro() {
		return template.isMacro();
	}

}
