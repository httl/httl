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

import java.util.HashMap;
import java.util.Map;

/**
 * MultiResolver. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiResolver implements Resolver {
	
	private Resolver[] resolvers;

	private Map<String, Resolver> resolverMap;

	public void setResolvers(Resolver[] resolvers) {
		this.resolvers = resolvers;
		this.resolverMap = new HashMap<String, Resolver>();
		String suffix = Resolver.class.getSimpleName();
		for (Resolver resolver : resolvers) {
			String name = resolver.getClass().getSimpleName();
			if (name.endsWith(suffix)) {
				name = name.substring(0, name.length() - suffix.length());
			}
			name = name.substring(0, 1).toLowerCase() + name.substring(1);
			resolverMap.put(name, resolver);
		}
	}

	public Object get(String key) {
		if (resolvers == null || resolvers.length == 0) {
			return null;
		}
		for (Resolver resolver : resolvers) {
			Object value = resolver.get(key);
			if (value != null) {
				return value;
			}
		}
		if (resolverMap != null) {
			return resolverMap.get(key);
		}
		return null;
	}

}