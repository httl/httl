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
package httl.web.webx;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.service.template.TemplateContext;

/**
 * ContextMap. (Integration, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ContextMap implements Map<String, Object> {
	
	private final TemplateContext templateContext;

	public ContextMap(TemplateContext templateContext) {
		if (templateContext == null) {
			throw new IllegalArgumentException("templateContext == null");
		}
		this.templateContext = templateContext;
	}

	public void clear() {
	}

	public boolean containsKey(Object key) {
		return templateContext.containsKey((String) key);
	}

	public boolean containsValue(Object value) {
		return false;
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>();
		Set<String> keySet = templateContext.keySet();
		if (keySet != null) {
			for (String key : keySet) {
				entries.add(new ContextEntry(key));
			}
		}
		return entries;
	}

	public Object get(Object key) {
		return templateContext.get((String)key);
	}

	public boolean isEmpty() {
		return templateContext.keySet() == null ? true : templateContext.keySet().size() == 0;
	}

	public Set<String> keySet() {
		return templateContext.keySet();
	}

	public Object put(String key, Object value) {
		Object old = get(key);
		templateContext.put(key, value);
		return old;
	}

	public void putAll(Map<? extends String, ? extends Object> map) {
		if (map != null) {
			for (Map.Entry<? extends String, ? extends Object> entry : map.entrySet()) {
				put(entry.getKey(), entry.getValue());
			}
		}
	}

	public Object remove(Object key) {
		Object old = get(key);
		templateContext.remove((String)key);
		return old;
	}

	public int size() {
		return templateContext.keySet().size();
	}

	public Collection<Object> values() {
		Set<Object> values = new HashSet<Object>();
		Set<String> keySet = templateContext.keySet();
		if (keySet != null) {
			for (String key : keySet) {
				values.add(get(key));
			}
		}
		return values;
	}

	private class ContextEntry implements Entry<String, Object> {

		private final String key;
		
		private volatile Object value;

		public ContextEntry(String key){
			this.key = key;
			this.value = ContextMap.this.get(key);
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public Object setValue(Object value) {
			this.value = value;
			return ContextMap.this.put(key, value);
		}

	}

}