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
package httl.spi.loaders.resources;

import httl.Engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

/**
 * StringResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.StringLoader#load(String, Locale, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class StringResource extends AbstractResource {
	
	private static final long serialVersionUID = 1L;
	
	private final String source;
	
	public StringResource(Engine engine, String name, Locale locale, String encoding, String source) {
		super(engine, name, locale, encoding);
		this.source = source;
	}
	
	public StringResource(Engine engine, String name, Locale locale, String encoding, long lastModified, String source) {
		super(engine, name, locale, encoding, lastModified);
		this.source = source;
	}

	@Override
	public long getLength() {
		return source.length();
	}
	
	public Reader getReader() throws IOException {
		return new StringReader(source);
	}

	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(source.getBytes(getEncoding()));
	}

}