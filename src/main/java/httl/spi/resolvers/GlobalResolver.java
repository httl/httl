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
package httl.spi.resolvers;

import httl.spi.Resolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GlobalResolver. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class GlobalResolver implements Resolver {

	private static volatile Map<String, Object> global;

	public static Map<String, Object> getGlobal() {
		if (global == null) {
			synchronized (GlobalResolver.class) {
				if (global == null) {
					global = new ConcurrentHashMap<String, Object>();
				}
			}
		}
		return global;
	}

	public static Object put(String key, Object value) {
		return getGlobal().put(key, value);
	}

	public Object get(String key) {
		return global == null ? null : global.get(key);
	}

}