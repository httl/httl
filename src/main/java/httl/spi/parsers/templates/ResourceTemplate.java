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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import httl.Engine;
import httl.Resource;
import httl.Template;

/**
 * ResourceTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ResourceTemplate implements Template {

	private final Resource resource;

	public ResourceTemplate(Resource resource) {
		this.resource = resource;
	}

	public String getName() {
		return resource.getName();
	}

	public String getEncoding() {
		return resource.getEncoding();
	}

	public Locale getLocale() {
		return resource.getLocale();
	}

	public long getLastModified() {
		return resource.getLastModified();
	}

	public long getLength() {
		return resource.getLength();
	}

	public String getSource() {
		return resource.getSource();
	}

	public Reader getReader() throws IOException {
		return resource.getReader();
	}

	public InputStream getInputStream() throws IOException {
		return resource.getInputStream();
	}

	public Engine getEngine() {
		return resource.getEngine();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Class<?>> getParameterTypes() {
		return Collections.EMPTY_MAP;
	}

	public Class<?> getReturnType() {
		return String.class;
	}

	public String getCode() {
		return "";
	}

	public int getOffset() {
		return 0;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Class<?>> getContextTypes() {
		return Collections.EMPTY_MAP;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Template> getMacros() {
		return Collections.EMPTY_MAP;
	}

	public boolean isMacro() {
		return false;
	}

	public Object evaluate() {
		throw new UnsupportedOperationException();
	}

	public Object evaluate(Map<String, Object> parameters) throws ParseException {
		throw new UnsupportedOperationException();
	}

	public void render(OutputStream stream) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void render(Map<String, Object> parameters, OutputStream stream)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	public void render(Writer writer) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void render(Map<String, Object> parameters, Writer writer)
			throws IOException {
		throw new UnsupportedOperationException();
	}

}