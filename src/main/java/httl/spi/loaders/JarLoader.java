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
package httl.spi.loaders;

import httl.Resource;
import httl.spi.Loader;
import httl.spi.loaders.resources.JarResource;
import httl.internal.util.UrlUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarFile;

/**
 * JarLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class JarLoader extends AbstractLoader {
	
	private File file;
	
	public void setTemplateDirectory(String directory) {
		file = new File(directory);
	}

	private File getAndCheckFile() {
		if (file == null) {
			throw new IllegalStateException("jar loader file == null. Please add config in your httl.properties: template.directory=foo.jar");
		}
		return file;
	}
	
	protected List<String> doList(String directory, String suffix) throws IOException {
		JarFile jarFile = new JarFile(getAndCheckFile());
		try {
			return UrlUtils.listJar(jarFile, suffix);
		} finally {
			jarFile.close();
		}
	}
	
	public Resource doLoad(String name, Locale locale, String encoding, String path) throws IOException {
		return new JarResource(getEngine(), name, locale, encoding, getAndCheckFile());
	}

	public boolean doExists(String name, Locale locale, String path) throws IOException {
		if (file != null && file.exists()) {
			JarFile jarFile = new JarFile(file);
			try {
				return jarFile.getEntry(name) != null;
			} finally {
				jarFile.close();
			}
		}
		return false; 
	}

}