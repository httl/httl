/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.locators;

import java.util.Locale;

import httl.spi.Locator;

/**
 * MultiLocator. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.loaders.AbstractLoader#setLocator(Locator)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiLocator implements Locator {
	
	private Locator[] locators;

	public void setLocators(Locator[] locators) {
		this.locators = locators;
	}

	public String root(String suffix) {
		if (locators == null) {
			return null;
		}
		if (locators.length == 1) {
			return locators[0].root(suffix);
		}
		for (Locator localor : locators) {
			String root = localor.root(suffix);
			if (root != null) {
				return root;
			}
		}
		return null;
	}

	public String relocate(String path, Locale locale) {
		if (locators == null) {
			return path;
		}
		if (locators.length == 1) {
			return locators[0].relocate(path, locale);
		}
		for (Locator localor : locators) {
			path = localor.relocate(path, locale);
		}
		return path;
	}

}
