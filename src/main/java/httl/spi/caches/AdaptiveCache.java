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
package httl.spi.caches;

import httl.internal.util.ConcurrentLinkedHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AdaptiveCache. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setCache(java.util.Map)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class AdaptiveCache<K, V> implements ConcurrentMap<K, V> {

	private ConcurrentMap<K, V> cache;

	/**
	 * httl.properties: cache.capacity=1000
	 */
	public void setCacheCapacity(int capacity) {
		if (capacity > 0) {
			cache = new ConcurrentLinkedHashMap<K, V>(capacity);
		} else {
			cache = new ConcurrentHashMap<K, V>();
		}
	}
	
	public void init() {
		if (cache == null) {
			setCacheCapacity(0);
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

	public V get(Object key) {
		return cache.get(key);
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

	public V putIfAbsent(K key, V value) {
		return cache.putIfAbsent(key, value);
	}

	public boolean remove(Object key, Object value) {
		return cache.remove(key, value);
	}

	public boolean replace(K key, V oldValue, V newValue) {
		return cache.replace(key, oldValue, newValue);
	}

	public V replace(K key, V value) {
		return cache.replace(key, value);
	}

}