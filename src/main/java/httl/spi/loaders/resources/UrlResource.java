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
 * UrlResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.UrlLoader#load(String, Locale, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class UrlResource extends InputStreamResource {
	
	private static final long serialVersionUID = 1L;

	private final URL url;
	
	public UrlResource(Engine engine, String name, Locale locale, String encoding, String path) throws IOException {
		super(engine, name, locale, encoding);
		this.url = new URL(path);
	}

	public InputStream getInputStream() throws IOException {
		return url.openStream();
	}

	@Override
	protected URL getUrl() {
		return url;
	}

}