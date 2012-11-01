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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WrappedMap<K, V> implements Map<K, V>, Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<K, V> parent;
	
	private final Map<K, V> current;

	public WrappedMap(Map<K, V> parent) {
		this(parent, new HashMap<K, V>());
	}

	public WrappedMap(Map<K, V> parent, Map<K, V> current) {
		if (current == null) {
			throw new IllegalArgumentException("wrapped map == null");
		}
		this.parent = parent == null ? null : Collections.unmodifiableMap(parent);
		this.current = current;
	}

	public V get(Object key) {
		V value = current.get(key);
		return value == null && parent != null ? parent.get(key) : value;
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return (Set<Map.Entry<K, V>>) CollectionUtils.merge(current.entrySet(), parent == null ? null : parent.entrySet());
	}

	public Set<K> keySet() {
		return (Set<K>) CollectionUtils.merge(current.keySet(), parent == null ? null : parent.keySet());
	}

	public Collection<V> values() {
		return CollectionUtils.merge(current.values(), parent == null ? null : parent.values());
	}
	
	public boolean containsKey(Object key) {
		return current.containsKey(key) || (parent != null && parent.containsValue(key));
	}

	public boolean containsValue(Object value) {
		return current.containsValue(value) || (parent != null && parent.containsValue(value));
	}

	public boolean isEmpty() {
		return current.isEmpty() && (parent == null || parent.isEmpty());
	}

	public int size() {
		return current.size() + (parent == null ? 0 : parent.size());
	}

	public void clear() {
		current.clear();
	}

	public V put(K key, V value) {
		return current.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		current.putAll(m);
	}

	public V remove(Object key) {
		return current.remove(key);
	}

}
