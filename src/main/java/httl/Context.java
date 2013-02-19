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
package httl;

import httl.internal.util.WriterOutputStream;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Context. (API, Prototype, ThreadLocal, ThreadSafe)
 * 
 * <pre>
 * Context context = Context.getContext();
 * Object value = context.get(key);
 * </pre>
 * 
 * Lookup variable:
 * 
 * <pre>
 * if (value == null) value = puts.get(key); // context.put(key, value);
 * if (value == null) value = current.get(key); // render(current);
 * if (value == null) value = parent.get(key); // recursive above
 * 
 * if (value == null) value = servletResovler.get(key); // httl.properties: resovlers+=ServletResovler
 * if (value == null) value = globalResovler.get(key); // GlobalResovler.put(key, value)
 * 
 * if (value == null) value = engineConfig.get(key); // httl.properties
 * </pre>
 * 
 * @see httl.Template#evaluate(Map)
 * @see httl.Template#render(Map, java.io.Writer)
 * @see httl.Template#render(Map, java.io.OutputStream)
 * @see httl.spi.parsers.templates.WriterTemplate#render(Map, java.io.Writer)
 * @see httl.spi.parsers.templates.OutputStreamTemplate#render(Map, java.io.OutputStream)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Context implements Map<String, Object> {

	// The context thread local holder.
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
	 * @param map - current current
	 * @param out - current out
	 * @param template - current template
	 */
	public static Context pushContext(Map<String, Object> map, Writer out, Template template) {
		return doPushContext(map, out, template);
	}

	/**
	 * Push the current context to thread local.
	 * @param map - current current
	 * @param out - current out
	 * @param template - current template
	 */
	public static Context pushContext(Map<String, Object> map, OutputStream out, Template template) {
		return doPushContext(map, out, template);
	}

	// do push
	private static Context doPushContext(Map<String, Object> map, Object out, Template template) {
		Context context = new Context(getContext(), map, out, template);
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

	// The current thread id
	private final long thread;

	// The context level.
	private final int level;

	// The parent context.
	private final Context parent;

	// The current context.
	private Map<String, Object> current;

	// The current out.
	private Object out;

	// The current template.
	private Template template;

	// The current engine.
	private Engine engine;

	private Context(Context parent, Map<String, Object> current, Object out, Template template) {
		this.thread = Thread.currentThread().getId();
		this.level = parent == null ? 0 : parent.getLevel() + 1;
		this.parent = parent;
		this.current = current;
		this.out = out;
		this.template = template;
		initEngine(template == null ? null : template.getEngine());
	}

	// Check the cross-thread use.
	private void checkThread() {
		if (Thread.currentThread().getId() != thread) {
			throw new IllegalStateException("Don't cross-thread using " + Context.class.getName() + " object.");
		}
	}

	/**
	 * Get the context level.
	 * 
	 * @see #getContext()
	 * @return context level
	 */
	public int getLevel() {
		checkThread();
		return level;
	}

	/**
	 * Get the parent context.
	 * 
	 * @see #getContext()
	 * @return parent context
	 */
	public Context getParent() {
		checkThread();
		return parent;
	}

	/**
	 * Get the parent template.
	 * 
	 * @see #getContext()
	 * @return parent template
	 */
	public Template getSuper() {
		checkThread();
		return parent == null ? null : parent.getTemplate();
	}

	/**
	 * Get the current template.
	 * 
	 * @see #getContext()
	 * @return current template
	 */
	public Template getTemplate() {
		checkThread();
		return template;
	}

	/**
	 * Set the current template.
	 * 
	 * @param template - current template
	 */
	public Context setTemplate(Template template) {
		checkThread();
		this.template = template;
		if (template != null) {
			setEngine(template.getEngine());
		}
		return this;
	}

	/**
	 * Get the current engine.
	 * 
	 * @see #getContext()
	 * @return current engine
	 */
	public Engine getEngine() {
		checkThread();
		return engine;
	}

	/**
	 * Set the current engine.
	 * 
	 * @param engine - current engine
	 */
	public Context setEngine(Engine engine) {
		checkThread();
		if (template != null && template.getEngine() != engine) {
			throw new IllegalStateException("template.engine != context.engine");
		}
		initEngine(engine);
		return this;
	}
	
	private void initEngine(Engine engine) {
		if (engine != null) {
			if (parent != null && parent.getEngine() == null) {
				parent.setEngine(engine);
			}
			if (this.engine == null) {
				current = engine.createContext(parent, current);
			}
		}
		this.engine = engine;
	}

	/**
	 * Get the current out.
	 * 
	 * @see #getContext()
	 * @return current out
	 */
	public Object getOut() {
		checkThread();
		return out;
	}

	/**
	 * Get the current output stream.
	 * 
	 * @see #getContext()
	 * @return current output stream
	 */
	@SuppressWarnings("resource")
	public OutputStream getOutputStream() {
		checkThread();
		return out == null ? null : (out instanceof OutputStream ? (OutputStream) out 
				: new WriterOutputStream((Writer) out));
	}

	/**
	 * Set the current output stream.
	 * 
	 * @param out - current output stream
	 */
	public Context setOutputStream(OutputStream out) {
		checkThread();
		this.out = out;
		return this;
	}

	/**
	 * Get the current writer.
	 * 
	 * @see #getContext()
	 * @return current writer
	 */
	public Writer getWriter() {
		checkThread();
		return out == null ? null : (out instanceof Writer ? (Writer) out 
				: new OutputStreamWriter((OutputStream) out));
	}

	/**
	 * Set the current writer.
	 * 
	 * @param out - current writer
	 */
	public Context setWriter(Writer out) {
		checkThread();
		this.out = out;
		return this;
	}

	/**
	 * Get the variable value.
	 * 
	 * @see #getContext()
	 * @param key - variable key
	 * @param defaultValue - default value
	 * @return variable value
	 */
	public Object get(String key, Object defaultValue) {
		Object value = get(key);
		return value == null ? defaultValue : value;
	}
	
	// ==== Delegate the current context ==== //

	public Object get(Object key) {
		checkThread();
		return current == null ? null : current.get(key);
	}

	public int size() {
		checkThread();
		return current == null ? 0 : current.size();
	}

	public boolean isEmpty() {
		checkThread();
		return current == null ? true : current.isEmpty();
	}

	public boolean containsKey(Object key) {
		checkThread();
		return current == null ? false : current.containsKey(key);
	}

	public boolean containsValue(Object value) {
		checkThread();
		return current == null ? false : current.containsValue(value);
	}

	@SuppressWarnings("unchecked")
	public Set<String> keySet() {
		checkThread();
		return current == null ? Collections.EMPTY_SET : current.keySet();
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> values() {
		checkThread();
		return current == null ? Collections.EMPTY_SET : current.values();
	}

	@SuppressWarnings("unchecked")
	public Set<Map.Entry<String, Object>> entrySet() {
		checkThread();
		return current == null ? Collections.EMPTY_SET : current.entrySet();
	}

	public Object put(String key, Object value) {
		checkThread();
		if (current == null) {
			current = new HashMap<String, Object>();
		}
		return current.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		checkThread();
		if (current == null) {
			current = new HashMap<String, Object>();
		}
		current.putAll(m);
	}

	public Object remove(Object key) {
		checkThread();
		return current == null ? null : current.remove(key);
	}

	public void clear() {
		checkThread();
		if (current != null) {
			current.clear();
		}
	}

}