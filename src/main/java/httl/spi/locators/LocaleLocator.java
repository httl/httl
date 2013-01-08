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
package httl.spi.locators;

import httl.spi.Locator;
import httl.util.LocaleUtils;

import java.util.Locale;

/**
 * LocaleLocator. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.loaders.AbstractLoader#setLocator(Locator)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class LocaleLocator implements Locator {

	public String root(String suffix) {
		return null;
	}

	public String relocate(String name, Locale locale) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("resource name == null");
		}
		return LocaleUtils.appendLocale(name, locale);
	}

}