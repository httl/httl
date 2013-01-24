/*
 * Copyright 2011-2012 HTTL Team.
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
package httl.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

// FIXME reflect to bytecode wrapper.
public class BeanMap implements Map<String, Object> {

	private final Object bean;

	public BeanMap(Object bean) {
		if (bean == null)
			throw new IllegalArgumentException("bean == null");
		this.bean = bean;
	}

	public Object get(Object key) {
		return ClassUtils.getProperty(bean, (String) key);
	}

	public boolean containsKey(Object key) {
		return ClassUtils.getGetter(bean, (String) key) != null;
	}

	public boolean containsValue(Object value) {
		return ClassUtils.getBeanProperties(bean).containsValue(value);
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return ClassUtils.getBeanProperties(bean).entrySet();
	}

	public Set<String> keySet() {
		return ClassUtils.getBeanProperties(bean).keySet();
	}

	public Collection<Object> values() {
		return ClassUtils.getBeanProperties(bean).values();
	}

	public boolean isEmpty() {
		return size() > 0;
	}

	public int size() {
		return ClassUtils.getBeanProperties(bean).size();
	}

	public Object put(String key, Object value) {
		return null;
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
	}

	public Object remove(Object key) {
		return null;
	}

	public void clear() {
	}

}