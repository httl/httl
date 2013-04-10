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
import java.util.jar.JarFile;

/**
 * JarResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.JarLoader#load(String, Locale, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class JarResource extends InputStreamResource {

	private static final long serialVersionUID = 1L;

	private final File file;

	public JarResource(Engine engine, String name, Locale locale, String encoding, File file) {
		super(engine, name, locale, encoding);
		this.file = file;
	}

	@SuppressWarnings("resource")
	public InputStream getInputStream() throws IOException {
		// 注：JarFile与File的设计是不一样的，File相当于C#的FileInfo，只持有信息，
		// 而JarFile构造时即打开流，所以每次读取数据时，重新new新的实例，而不作为属性字段持有。
		JarFile jarFile = new JarFile(file);
		return jarFile.getInputStream(jarFile.getEntry(getName()));
	}

	public long getLastModified() {
		return file.lastModified();
	}

	public long getLength() {
		try {
			JarFile jarFile = new JarFile(file);
			try {
				return jarFile.getEntry(getName()).getSize();
			} finally {
				jarFile.close();
			}
		} catch (IOException e) {
			return super.getLength();
		}
	}

}