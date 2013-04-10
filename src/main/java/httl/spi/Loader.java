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
package httl.spi;

import httl.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Resource Loader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Loader {

	/**
	 * list resource names.
	 * 
	 * @param suffix resource suffix
	 * @return resource names.
	 */
	List<String> list(String suffix) throws IOException;

	/**
	 * is exists resource.
	 * 
	 * @param name - resource name
	 * @param locale - resource locale
	 * @return exists
	 */
	boolean exists(String name, Locale locale);

	/**
	 * load resource.
	 * 
	 * @param name - resource name
	 * @param locale - resource locale
	 * @param encoding - resource encoding
	 * @return resource
	 */
	Resource load(String name, Locale locale, String encoding) throws IOException;

}