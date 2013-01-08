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
package httl.spi;

import java.util.Locale;

/**
 * Resource Locator. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.loaders.AbstractLoader#setLocator(Locator)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Locator {

	/**
	 * relocate root.
	 * 
	 * @param suffix - resource suffix
	 * @return relocated root
	 */
	String root(String suffix);

	/**
	 * relocate path.
	 * 
	 * @param name - origin name
	 * @return relocated name
	 */
	String relocate(String name, Locale locale);

}