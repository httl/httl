/*
 * Copyright 1999-2012 Alibaba Group.
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
package httl.spi.caches;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AdaptiveCache<K, V> implements Map<K, V> {

	private Map<K, V> cache;

	public void setCacheCapacity(int capacity) {
		if (capacity > 0) {
			LruCache<K, V> lruCache = new LruCache<K, V>();
			lruCache.setCacheCapacity(capacity);
			cache = lruCache;
		} else {
			cache = new ConcurrentHashMap<K, V>();
		}
    }
	
	public void init() {
		if (cache == null) {
			cache = new ConcurrentHashMap<K, V>();
		}
	}

	public void clear() {
		cache.clear();
	}

	public boolean containsKey(Object key) {
		return cache.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return cache.containsValue(value);
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return cache.entrySet();
	}

	public boolean equals(Object o) {
		return cache.equals(o);
	}

	public V get(Object key) {
		return cache.get(key);
	}

	public int hashCode() {
		return cache.hashCode();
	}

	public boolean isEmpty() {
		return cache.isEmpty();
	}

	public Set<K> keySet() {
		return cache.keySet();
	}

	public V put(K key, V value) {
		return cache.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		cache.putAll(m);
	}

	public V remove(Object key) {
		return cache.remove(key);
	}

	public int size() {
		return cache.size();
	}

	public Collection<V> values() {
		return cache.values();
	}

}
