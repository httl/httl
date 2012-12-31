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
package httl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Context. (API, ThreadLocal, ThreadSafe)
 * 
 * @see httl.Template#evaluate(Map)
 * @see httl.Template#render(Map, java.io.Writer)
 * @see httl.Template#render(Map, java.io.OutputStream)
 * @see httl.spi.parsers.template.WriterTemplate#render(Map, java.io.Writer)
 * @see httl.spi.parsers.template.OutputStreamTemplate#render(Map, java.io.OutputStream)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Context implements Map<String, Object> {

    // The thread local holder.
    private static final ThreadLocal<Context> LOCAL = new ThreadLocal<Context>();

    /**
     * Get the current context from thread local.
     * 
     * @return current context
     */
    public static Context getContext() {
        Context context = LOCAL.get();
        if (context == null) {
            context = new Context(null, null, null, null);
            LOCAL.set(context);
        }
        return context;
    }

    /**
     * Push the current context to thread local.
     * 
     * @param template - current template
     * @param parameters - current parameters
     */
    public static Context pushContext(Template template, Map<String, Object> parameters, Object output) {
        Context context = new Context(getContext(), template, parameters, output);
        LOCAL.set(context);
        return context;
    }

    /**
     * Pop the current context from thread local, and restore parent context to thread local.
     */
    public static void popContext() {
        Context context = LOCAL.get();
        if (context != null) {
            Context parent = context.getParent();
            if (parent != null) {
                LOCAL.set(parent);
            } else {
                LOCAL.remove();
            }
        }
    }

    /**
     * Remove the current context from thread local.
     */
    public static void removeContext() {
        LOCAL.remove();
    }

    // The parent context.
    private final Context parent;

    // The current template.
    private final Template template;

    // The current parameters.
    private final Map<String, Object> parameters;

    // The current output.
    private final Object output;

    // The current store.
    private Map<String, Object> storage;

    private Context(Context parent, Template template, Map<String, Object> parameters, Object output) {
        this.parent = parent;
        this.template = template;
        this.parameters = parameters;
        this.output = output;
    }

    /**
     * Get the parent context.
     * 
     * @see #getContext()
     * @return parent context
     */
    public Context getParent() {
        return parent;
    }

    /**
     * Get the current template.
     * 
     * @see #getContext()
     * @return current template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Get the current parameters.
     * 
     * @see #getContext()
     * @return current parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Get the current output.
     * 
     * @see #getContext()
     * @return current output
     */
    public Object getOutput() {
        return output;
    }

    // java.util.Map
	public Object get(Object key) {
		if (storage != null) {
			Object value = storage.get(key);
			if (value != null) {
				return value;
			}
		}
		if (parameters != null) {
			Object value = parameters.get(key);
			if (value != null) {
				return value;
			}
		}
		if (parent != null) {
			Object value = parent.get(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	// java.util.Map
	public Set<Map.Entry<String, Object>> entrySet() {
		Set<Map.Entry<String, Object>> enrtySet = new HashSet<Map.Entry<String, Object>>();
		if (storage != null) {
			enrtySet.addAll(storage.entrySet());
		}
		if (parameters != null) {
			enrtySet.addAll(parameters.entrySet());
		}
		if (parent != null) {
			enrtySet.addAll(parent.entrySet());
		}
		return enrtySet;
	}

	// java.util.Map
	public Set<String> keySet() {
		Set<String> keySet = new HashSet<String>();
		if (storage != null) {
			keySet.addAll(storage.keySet());
		}
		if (parameters != null) {
			keySet.addAll(parameters.keySet());
		}
		if (parent != null) {
			keySet.addAll(parent.keySet());
		}
		return keySet;
	}

	// java.util.Map
	public Collection<Object> values() {
		Collection<Object> values = new HashSet<Object>();
		if (storage != null) {
			values.addAll(storage.values());
		}
		if (parameters != null) {
			values.addAll(parameters.values());
		}
		if (parent != null) {
			values.addAll(parent.values());
		}
		return values;
	}

	// java.util.Map
	public boolean containsKey(Object key) {
		if (storage != null && storage.containsKey(key)) {
			return true;
		}
		if (parameters != null && parameters.containsKey(key)) {
			return true;
		}
		if (parent != null && parent.containsKey(key)) {
			return true;
		}
		return false;
	}

	// java.util.Map
	public boolean containsValue(Object value) {
		if (storage != null && storage.containsValue(value)) {
			return true;
		}
		if (parameters != null && parameters.containsValue(value)) {
			return true;
		}
		if (parent != null && parent.containsValue(value)) {
			return true;
		}
		return false;
	}

	// java.util.Map
	public boolean isEmpty() {
		if (storage != null && ! storage.isEmpty()) {
			return false;
		}
		if (parameters != null && ! parameters.isEmpty()) {
			return false;
		}
		if (parent != null && ! parent.isEmpty()) {
			return false;
		}
		return true;
	}

	// java.util.Map
	public int size() {
		int size = 0;
		if (storage != null) {
			size += storage.size();
		}
		if (parameters != null) {
			size += parameters.size();
		}
		if (parent != null) {
			size += parent.size();
		}
		return size;
	}

	// java.util.Map
	public Object put(String key, Object value) {
		if (storage == null) { // safely in thread local
            storage = new HashMap<String, Object>();
        }
		return storage.put(key, value);
	}

	// java.util.Map
	public void putAll(Map<? extends String, ? extends Object> map) {
		if (storage == null) { // safely in thread local
            storage = new HashMap<String, Object>();
        }
		storage.putAll(map);
	}

	// java.util.Map
	public Object remove(Object key) {
		if (storage != null) {
			return storage.remove(key);
		}
		return null;
	}

	// java.util.Map
	public void clear() {
		if (storage != null) {
			storage.clear();
		}
	}

}
