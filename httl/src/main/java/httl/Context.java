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
package httl;

import java.io.OutputStream;
import java.io.Writer;
import java.util.*;

/**
 * Context. (API, Prototype, ThreadLocal, ThreadSafe)
 * <p/>
 * <pre>
 * Context context = Context.getContext();
 * Object value = context.get(key);
 * </pre>
 * <p/>
 * Lookup variable:
 * <p/>
 * <pre>
 * if (value == null) value = puts.get(key); // context.put(key, value);
 * if (value == null) value = current.get(key); // render(current);
 * if (value == null) value = parent.get(key); // recursive above
 *
 * default resovler:
 * if (value == null) value = contextResovler.get(key); // parent, super, this, engine, out, level
 * if (value == null) value = servletResovler.get(key); // request, response, parameter, header, session, cookie, application
 * if (value == null) value = globalResovler.get(key); // GlobalResovler.put(key, value)
 *
 * non default resovler:
 * if (value == null) value = engineResovler.get(key); // httl.properties: key=value
 * if (value == null) value = systemResovler.get(key); // java -Dkey=value
 * if (value == null) value = environmentResolver.get(key); // export key=value
 * </pre>
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 * @see httl.Template#render(Object, Object)
 * @see httl.spi.translators.templates.AbstractTemplate#render(Object, Object)
 */
public final class Context implements Map<String, Object> {

    // The context thread local holder.
    private static final ThreadLocal<Context> LOCAL = new ThreadLocal<Context>();
    // The current thread.
    private final Thread thread;
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

    private Context(Context parent, Map<String, Object> current) {
        this.thread = parent == null ? Thread.currentThread() : parent.thread;
        this.level = parent == null ? 0 : parent.getLevel() + 1;
        this.parent = parent;
        setCurrent(current);
    }

    /**
     * Get the current context from thread local.
     *
     * @return current context
     */
    public static Context getContext() {
        Context context = LOCAL.get();
        if (context == null) {
            context = new Context(null, null);
            LOCAL.set(context);
        }
        return context;
    }

    /**
     * Push the current context to thread local.
     */
    public static Context pushContext() {
        return pushContext(null);
    }

    /**
     * Push the current context to thread local.
     *
     * @param current - current variables
     */
    public static Context pushContext(Map<String, Object> current) {
        Context context = new Context(getContext(), current);
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

    // Check the cross-thread use.
    private void checkThread() {
        if (Thread.currentThread() != thread) {
            throw new IllegalStateException("Don't cross-thread using the "
                    + Context.class.getName() + " object, it's thread-local only. context thread: "
                    + thread.getName() + ", current thread: " + Thread.currentThread().getName());
        }
    }

    // Set the current context
    private void setCurrent(Map<String, Object> current) {
        if (current instanceof Context) {
            throw new IllegalArgumentException("Don't using the " + Context.class.getName()
                    + " object as a parameters, it's implicitly delivery by thread-local. parameter context: "
                    + ((Context) current).thread.getName() + ", current context: " + thread.getName());
        }
        this.current = current;
    }

    /**
     * Get the context level.
     *
     * @return context level
     * @see #getContext()
     */
    public int getLevel() {
        checkThread();
        return level;
    }

    /**
     * Get the parent context.
     *
     * @return parent context
     * @see #getContext()
     */
    public Context getParent() {
        checkThread();
        return parent;
    }

    /**
     * Get the current template.
     *
     * @return current template
     * @see #getContext()
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
        if (template != null) {
            setEngine(template.getEngine());
        }
        this.template = template;
        return this;
    }

    /**
     * Get the current engine.
     *
     * @return current engine
     * @see #getContext()
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
        if (engine != null) {
            if (template != null && template.getEngine() != engine) {
                throw new IllegalStateException("Failed to set the context engine, because is not the same to template engine. template engine: "
                        + template.getEngine().getName() + ", context engine: " + engine.getName()
                        + ", template: " + template.getName() + ", context: " + thread.getName());
            }
            if (parent != null && parent.getEngine() != engine) {
                parent.setEngine(engine);
            }
            if (this.engine == null) {
                setCurrent(engine.createContext(parent, current));
            }
        }
        this.engine = engine;
        return this;
    }

    /**
     * Get the current out.
     *
     * @return current out
     * @see #getContext()
     */
    public Object getOut() {
        checkThread();
        return out;
    }

    /**
     * Set the current writer.
     *
     * @param out - current writer
     */
    public Context setOut(Writer out) {
        checkThread();
        this.out = out;
        return this;
    }

    /**
     * Set the current output stream.
     *
     * @param out - current output stream
     */
    public Context setOut(OutputStream out) {
        checkThread();
        this.out = out;
        return this;
    }

    /**
     * Get the variable value.
     *
     * @param key          - variable key
     * @param defaultValue - default value
     * @return variable value
     * @see #getContext()
     */
    public Object get(String key, Object defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : value;
    }

    // ==== Delegate the current context map ==== //

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