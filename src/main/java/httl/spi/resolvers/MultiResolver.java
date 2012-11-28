/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.resolvers;

import httl.spi.Resolver;

public class MultiResolver implements Resolver {
	
	private Resolver[] resolvers;
	
	public void setResolvers(Resolver[] resolvers) {
		this.resolvers = resolvers;
	}

	public String getProperty(String key) {
		if (resolvers == null || resolvers.length == 0) {
			return null;
		}
		if (resolvers.length == 1) {
			return resolvers[0].getProperty(key);
		}
		for (int i = resolvers.length - 1; i >= 0; i --) {
			Resolver resolver = resolvers[i];
			String value = resolver.getProperty(key);
			if (value != null && value.length() > 0) {
				return value;
			}
		}
		return null;
	}

}
