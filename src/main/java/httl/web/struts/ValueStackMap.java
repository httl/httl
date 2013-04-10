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
package httl.web.struts;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.opensymphony.xwork2.util.ValueStack;

/**
 * ValueStackMap. (Integration, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ValueStackMap implements Map<String, Object> {
	
	private final ValueStack valueStack;

	public ValueStackMap(ValueStack valueStack) {
		if (valueStack == null) {
			throw new IllegalArgumentException("valueStack == null");
		}
		this.valueStack = valueStack;
	}

	public Object get(Object key) {
		if ("request".equals(key) || "response".equals(key)) {
			return null;
		}
		return valueStack.findValue((String) key);
	}

	public Object put(String key, Object value) {
		Object old = get(key);
		valueStack.set(key, value);
		return old;
	}

	public void putAll(Map<? extends String, ? extends Object> map) {
		if (map != null) {
			for (Map.Entry<? extends String, ? extends Object> entry : map.entrySet()) {
				valueStack.set(entry.getKey(), entry.getValue());
			}
		}
	}

	public Object remove(Object key) {
		return put((String) key, null);
	}

	public void clear() {
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return valueStack.getContext() == null ? null : valueStack.getContext().entrySet();
	}

	public Set<String> keySet() {
		return valueStack.getContext() == null ? null : valueStack.getContext().keySet();
	}

	public Collection<Object> values() {
		return valueStack.getContext() == null ? null : valueStack.getContext().values();
	}

	public boolean containsKey(Object key) {
		return valueStack.getContext() == null ? false : valueStack.getContext().containsKey(key);
	}

	public boolean containsValue(Object value) {
		return valueStack.getContext() == null ? false : valueStack.getContext().containsKey(value);
	}

	public int size() {
		return valueStack.getContext() == null ? 0 : valueStack.getContext().size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

}