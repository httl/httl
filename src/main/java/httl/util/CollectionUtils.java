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
package httl.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class CollectionUtils {

	public static <T> Collection<T> merge(Collection<T> c1, Collection<T> c2) {
		if (c1 == null || c1.size() == 0) {
			return c2;
		}
		if (c2 == null || c2.size() == 0) {
			return c1;
		}
		Collection<T> all = new HashSet<T>(c1.size() + c2.size());
		all.addAll(c1);
		all.addAll(c2);
		return all;
	}
	
	public static <K, V> void putIfAbsent(Map<K, V> from, Map<K, ? super V> to) {
		if (from == null || to == null) {
			return;
		}
		for (Map.Entry<K, V> entry : from.entrySet()) {
			if (! to.containsKey(entry.getKey())) {
				to.put(entry.getKey(), entry.getValue());
			}
		}
	}

}
