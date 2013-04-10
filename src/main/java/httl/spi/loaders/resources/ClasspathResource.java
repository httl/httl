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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

/**
 * ClasspathResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.ClasspathLoader#load(String, Locale, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ClasspathResource extends InputStreamResource {

	private static final long serialVersionUID = 2499229996487593996L;
	
	private final String path;
	
	public ClasspathResource(Engine engine, String name, String encoding, String path, Locale locale) {
		super(engine, name, locale, encoding);
		this.path = path;
	}

	public InputStream getInputStream() throws IOException {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
	}

	@Override
	protected URL getUrl() {
		return Thread.currentThread().getContextClassLoader().getResource(path);
	}

}