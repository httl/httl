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

import java.io.Writer;
import java.util.Map;

/**
 * Context. (API, ThreadLocal, ThreadSafe)
 * 
 * @see httl.spi.parsers.template.AbstractTemplate#render(Map, Writer)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Context {
    
    private static ThreadLocal<Context> LOCAL = new ThreadLocal<Context>() {
        @Override
        protected Context initialValue() {
            return new Context();
        }
    };

    /**
     * Get current thread local context.
     * 
     * @return current thread local context.
     */
    public static Context getContext() {
        return LOCAL.get();
    }
    
    /**
     * Remove current thread local context.
     */
    public static void removeContext() {
        LOCAL.remove();
    }
    
    private Template template;
    
    private Map<String, Object> parameters;
    
    private Context() {}
    
    /**
     * Get current template.
     * 
     * @see #getContext()
     * @return current template.
     */
    public Template getTemplate() {
        return template;
    }
    
    /**
     * Set current template.
     * 
     * @see #getContext()
     * @param template - current template.
     * @return current context.
     */
    public Context setTemplate(Template template) {
        this.template = template;
        return this;
    }
    
    /**
     * Get current parameters.
     * 
     * @see #getContext()
     * @return current parameters.
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    /**
     * Set current parameters.
     * 
     * @see #getContext()
     * @param parameters - current parameters.
     * @return current context.
     */
    public Context setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }
    
}
