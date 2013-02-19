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

import httl.internal.util.DelegateMap;
import httl.internal.util.WriterOutputStream;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

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
public final class Context extends DelegateMap<String, Object> {

	private static final long serialVersionUID = 1L;

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
	 * @param current - current current
	 * @param out - current out
	 * @param template - current template
	 */
	public static Context pushContext(Map<String, Object> current, Writer out, Template template) {
		return doPushContext(current, out, template);
	}

	/**
	 * Push the current context to thread local.
	 * @param current - current current
	 * @param out - current out
	 * @param template - current template
	 */
	public static Context pushContext(Map<String, Object> current, OutputStream out, Template template) {
		return doPushContext(current, out, template);
	}

	// do push
	private static Context doPushContext(Map<String, Object> current, Object out, Template template) {
		Context parent = getContext();
		if (template != null && parent.parent == null) {
			parent.engine = template.getEngine(); // set root context engine
		}
		Context context = new Context(parent, current, out, template);
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

	// The context level.
	private final int level;

	// The current out.
	private Object out;

	// The current template.
	private Template template;

	// The current engine.
	private Engine engine;

	private Context(Context parent, Map<String, Object> current, Object out, Template template) {
		super(parent, current);
		this.parent = parent;
		this.level = parent == null ? 0 : parent.getLevel() + 1;
		this.out = out;
		this.template = template;
		this.engine = template == null ? null : template.getEngine();
	}

	/**
	 * Get the context level.
	 * 
	 * @see #getContext()
	 * @return context level
	 */
	public int getLevel() {
		return level;
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
	 * Get the parent template.
	 * 
	 * @see #getContext()
	 * @return parent template
	 */
	public Template getSuper() {
		return parent == null ? null : parent.getTemplate();
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
	 * Set the current template.
	 * 
	 * @param template - current template
	 */
	public Context setTemplate(Template template) {
		this.template = template;
		if (template != null) {
			this.engine = template.getEngine();
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
		return engine;
	}

	/**
	 * Set the current engine.
	 * 
	 * @param engine - current engine
	 */
	public Context setEngine(Engine engine) {
		if (template != null && template.getEngine() != engine) {
			throw new IllegalStateException("template.engine != context.engine");
		}
		this.engine = engine;
		return this;
	}

	/**
	 * Get the current out.
	 * 
	 * @see #getContext()
	 * @return current out
	 */
	public Object getOut() {
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
		return out instanceof OutputStream ? (OutputStream) out : new WriterOutputStream((Writer) out);
	}

	/**
	 * Set the current output stream.
	 * 
	 * @param out - current output stream
	 */
	public Context setOutputStream(OutputStream out) {
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
		return out instanceof Writer ? (Writer) out : new OutputStreamWriter((OutputStream) out);
	}

	/**
	 * Set the current writer.
	 * 
	 * @param out - current writer
	 */
	public Context setWriter(Writer out) {
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

	// Get the special variables after the user variables.
	// Allows the user to override these special variables.
	@Override
	protected Object doGet(Object key) {
		if ("super".equals(key)) {
			return getSuper();
		} else if ("template".equals(key) || "this".equals(key)) {
			return getTemplate();
		} else if ("engine".equals(key)) {
			return getEngine();
		} else if ("out".equals(key)) {
			return getOut();
		} else if ("level".equals(key)) {
			return getLevel();
		} else if ("parent".equals(key)) {
			return getParent();
		} else if ("context".equals(key) || "current".equals(key)) {
			return this;
		} else if (getParent() == null && getEngine() != null) {
			return getEngine().getVariable((String) key);
		} else {
			return null;
		}
	}

	// Create the context writable storage map.
	@Override
	protected Map<String, Object> newMap() {
		Engine engine = getEngine();
		return engine == null ? super.newMap() : engine.createContext();
	}

}