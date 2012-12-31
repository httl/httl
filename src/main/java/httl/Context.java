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

import httl.util.DelegateMap;

import java.util.Map;

/**
 * Context. (API, Prototype, ThreadLocal, ThreadSafe)
 * 
 * <pre>
 * Context context = Context.getContext();
 * </pre>
 * 
 * @see httl.Template#evaluate(Map)
 * @see httl.Template#render(Map, java.io.Writer)
 * @see httl.Template#render(Map, java.io.OutputStream)
 * @see httl.spi.parsers.template.WriterTemplate#render(Map, java.io.Writer)
 * @see httl.spi.parsers.template.OutputStreamTemplate#render(Map, java.io.OutputStream)
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

    // The context key
    private static final String CONTEXT_KEY = "context";

    // The parent key
    private static final String PARENT_KEY = "parent";

    // The template key
    private static final String TEMPLATE_KEY = "template";

    // The output key
    private static final String OUTPUT_KEY = "output";

    // The parent context.
    private final Context parent;

    // The current template.
    private final Template template;

    // The current parameters.
    private final Map<String, Object> parameters;

    // The current output.
    private final Object output;

    private Context(Context parent, Template template, Map<String, Object> parameters, Object output) {
    	super(parent, parameters);
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
    
    /**
     * Get parameter value.
     * 
     * @see #getContext()
     * @param key parameter key
     * @param defaultValue parameter default value
     * @return parameter value
     */
    public Object get(String key, Object defaultValue) {
    	Object value = get(key);
    	if (value == null) {
    		return defaultValue;
    	}
    	return value;
    }

    // Get specific variable.
    @Override
	protected Object doGet(Object key) {
		if (CONTEXT_KEY.equals(key)) {
			return this;
		}
		if (PARENT_KEY.equals(key)) {
			return parent;
		}
		if (TEMPLATE_KEY.equals(key)) {
			return template;
		}
		if (OUTPUT_KEY.equals(key)) {
			return output;
		}
		return null;
	}

}
