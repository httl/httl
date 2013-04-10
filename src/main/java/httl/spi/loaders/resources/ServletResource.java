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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.servlet.ServletContext;

/**
 * ServletResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.ServletLoader#load(String, Locale, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ServletResource extends InputStreamResource {

	private static final long serialVersionUID = 2499229996487593996L;
	
	private final String path;
	
	private final transient ServletContext servletContext;

	public ServletResource(Engine engine, String name, Locale locale, String encoding, String path, ServletContext servletContext) {
		super(engine, name, locale, encoding);
		this.path = path;
		this.servletContext = servletContext;
	}

	public InputStream getInputStream() throws IOException {
		return servletContext.getResourceAsStream(path);
	}

	@Override
	public File getFile() {
		return new File(servletContext.getRealPath(path));
	}

}