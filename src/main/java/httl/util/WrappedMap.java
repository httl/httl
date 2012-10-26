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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WrappedMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = -4228345059528934631L;

	private final Map<K, V> parent;

	public WrappedMap(Map<K, V> parent) {
		this.parent = Collections.unmodifiableMap(parent);
	}

	public V get(Object key) {
		V value = super.get(key);
		return value == null && parent != null ? parent.get(key) : value;
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return (Set<Map.Entry<K, V>>) CollectionUtils.merge(super.entrySet(), parent == null ? null : parent.entrySet());
	}

	public Set<K> keySet() {
		return (Set<K>) CollectionUtils.merge(super.keySet(), parent == null ? null : parent.keySet());
	}

	public Collection<V> values() {
		return CollectionUtils.merge(super.values(), parent == null ? null : parent.values());
	}
	
	public boolean containsKey(Object key) {
		return super.containsKey(key) || (parent != null && parent.containsValue(key));
	}

	public boolean containsValue(Object value) {
		return super.containsValue(value) || (parent != null && parent.containsValue(value));
	}

	public boolean isEmpty() {
		return super.isEmpty() && (parent == null || parent.isEmpty());
	}

	public int size() {
		return super.size() + (parent == null ? 0 : parent.size());
	}

}
