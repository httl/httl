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
import java.util.HashSet;
import java.util.Map;
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
		V value = doGet(key);
		if (value != null) {
			return value;
		}
		return parent == null ? null : parent.get(key);
	}
	
	protected V doGet(Object key) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> entrySet = null;
		if (writable != null) {
			if (current == null && parent == null) {
				return writable.entrySet();
			}
			if (entrySet == null) {
				entrySet = new HashSet<Map.Entry<K, V>>();
			}
			entrySet.addAll(writable.entrySet());
		}
		if (current != null) {
			if (writable == null && parent == null) {
				return current.entrySet();
			}
			if (entrySet == null) {
				entrySet = new HashSet<Map.Entry<K, V>>();
			}
			entrySet.addAll(current.entrySet());
		}
		if (parent != null) {
			if (current == null && writable == null) {
				return parent.entrySet();
			}
			if (entrySet == null) {
				entrySet = new HashSet<Map.Entry<K, V>>();
			}
			entrySet.addAll(parent.entrySet());
		}
		return entrySet == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(entrySet);
	}

	@SuppressWarnings("unchecked")
	public Set<K> keySet() {
		Set<K> keySet = null;
		if (writable != null) {
			if (current == null && parent == null) {
				return writable.keySet();
			}
			if (keySet == null) {
				keySet = new HashSet<K>();
			}
			keySet.addAll(writable.keySet());
		}
		if (current != null) {
			if (writable == null && parent == null) {
				return current.keySet();
			}
			if (keySet == null) {
				keySet = new HashSet<K>();
			}
			keySet.addAll(current.keySet());
		}
		if (parent != null) {
			if (current == null && writable == null) {
				return parent.keySet();
			}
			if (keySet == null) {
				keySet = new HashSet<K>();
			}
			keySet.addAll(parent.keySet());
		}
		return keySet == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(keySet);
	}

	@SuppressWarnings("unchecked")
	public Collection<V> values() {
		Collection<V> values = null;
		if (writable != null) {
			if (current == null && parent == null) {
				return writable.values();
			}
			if (values == null) {
				values = new HashSet<V>();
			}
			values.addAll(writable.values());
		}
		if (current != null) {
			if (writable == null && parent == null) {
				return current.values();
			}
			if (values == null) {
				values = new HashSet<V>();
			}
			values.addAll(current.values());
		}
		if (parent != null) {
			if (current == null && writable == null) {
				return parent.values();
			}
			if (values == null) {
				values = new HashSet<V>();
			}
			values.addAll(parent.values());
		}
		return values == null ? Collections.EMPTY_SET : Collections.unmodifiableCollection(values);
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

}
