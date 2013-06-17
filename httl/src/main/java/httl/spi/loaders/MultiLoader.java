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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MultiLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiLoader implements Loader {

	private Loader[] loaders;
	
	public void setLoaders(Loader[] loaders) {
		this.loaders = loaders;
	}

	public Resource load(String name, Locale locale, String encoding) throws IOException {
		if (loaders.length == 1) {
			return loaders[0].load(name, locale, encoding);
		}
		for (Loader loader : loaders) {
			try {
				if (loader.exists(name, locale)) {
					return loader.load(name, locale, encoding);
				}
			} catch (Exception e) {
			}
		}
		throw new FileNotFoundException("No such template file: " + name);
	}

	public List<String> list(String suffix) throws IOException {
		if (loaders.length == 1) {
			return loaders[0].list(suffix);
		}
		List<String> all = new ArrayList<String>();
		for (Loader loader : loaders) {
			try {
				List<String> list = loader.list(suffix);
				if (list != null && list.size() > 0) {
					all.addAll(list);
				}
			} catch (Exception e) {
			}
		}
		return all;
	}

	public boolean exists(String name, Locale locale) {
		if (loaders.length == 1) {
			return loaders[0].exists(name, locale);
		}
		for (Loader loader : loaders) {
			try {
				if (loader.exists(name, locale)) {
					return true;
				}
			} catch (Exception e) {
			}
		}
		return false;
	}

}