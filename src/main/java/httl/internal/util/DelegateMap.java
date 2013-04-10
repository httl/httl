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
package httl.internal.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * DelegateMap (Tool, Prototype, NotThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DelegateMap<K, V> implements Map<K, V>, Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<K, V> parent;
	
	private final Map<K, V> current;

	private Map<K, V> writable;

	public DelegateMap(Map<K, V> parent, Map<K, V> current) {
		this.parent = parent;
		this.current = current;
	}

	public V get(Object key) {
		if (writable != null) {
			V value = writable.get(key);
			if (value != null) {
				return value;
			}
		}
		if (current != null) {
			V value = current.get(key);
			if (value != null) {
				return value;
			}
		}
		return parent == null ? null : parent.get(key);
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return new DelegateSet<Map.Entry<K, V>>() {
			@Override
			protected Collection<java.util.Map.Entry<K, V>> getCollection(Map<K, V> map) {
				return map.entrySet();
			}
		};
	}
	
	public Set<K> keySet() {
		return new DelegateSet<K>() {
			@Override
			protected Collection<K> getCollection(Map<K, V> map) {
				return map.keySet();
			}
		};
	}

	public Collection<V> values() {
		return new DelegateSet<V>() {
			@Override
			protected Collection<V> getCollection(Map<K, V> map) {
				return map.values();
			}
		};
	}

	public int size() {
		int size = 0;
		if (writable != null) {
			size += writable.size();
		}
		if (current != null) {
			size += current.size();
		}
		if (parent != null) {
			size += parent.size();
		}
		return size;
	}

	public boolean containsKey(Object key) {
		if (writable != null && writable.containsKey(key)) {
			return true;
		}
		if (current != null && current.containsKey(key)) {
			return true;
		}
		return parent != null && parent.containsKey(key);
	}

	public boolean containsValue(Object value) {
		if (writable != null && writable.containsValue(value)) {
			return true;
		}
		if (current != null && current.containsValue(value)) {
			return true;
		}
		return parent != null && parent.containsValue(value);
	}

	public boolean isEmpty() {
		if (writable != null && ! writable.isEmpty()) {
			return false;
		}
		if (current != null && ! current.isEmpty()) {
			return false;
		}
		return parent == null || parent.isEmpty();
	}

	public V put(K key, V value) {
		if (writable == null) {
			writable = new HashMap<K, V>();
		}
		return writable.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> map) {
		if (writable == null) {
			writable = new HashMap<K, V>();
		}
		writable.putAll(map);
	}

	public V remove(Object key) {
		if (writable != null) {
			return writable.remove(key);
		}
		return null;
	}

	public void clear() {
		if (writable != null) {
			writable.clear();
		}
	}

	private abstract class DelegateSet<T> extends AbstractSet<T> {

		@Override
		public Iterator<T> iterator() {
			return new DelegateIterator();
		}

		@Override
		public int size() {
			return DelegateMap.this.size();
		}
		
		protected abstract Collection<T> getCollection(Map<K, V> map);

		private class DelegateIterator implements Iterator<T> {
			
			private int level = 0;
			
			private Iterator<T> iterator;
			
			private Iterator<T> getIterator() {
				if (iterator == null || ! iterator.hasNext()) {
					if (level < 1 && parent != null) {
						level = 1;
						iterator = DelegateSet.this.getCollection(parent).iterator();
					} else if (level < 2 && current != null) {
						level = 2;
						iterator = DelegateSet.this.getCollection(current).iterator();
					} else if (level < 3 && writable != null) {
						level = 3;
						iterator = DelegateSet.this.getCollection(writable).iterator();
					}
				}
				return iterator;
			}

			public boolean hasNext() {
				Iterator<T> iterator = getIterator();
				return iterator == null ? false : iterator.hasNext();
			}

			public T next() {
				Iterator<T> iterator = getIterator();
				if (iterator == null)
					throw new NoSuchElementException();
				return iterator.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		}

	}
	
}