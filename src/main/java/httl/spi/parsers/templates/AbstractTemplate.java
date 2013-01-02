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
package httl.spi.parsers.templates;

import httl.Context;
import httl.Engine;
import httl.Template;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.util.UnsafeByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract template. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractTemplate implements Template, Serializable {
    
    private static final long serialVersionUID = 8780375327644594903L;

    private transient final Engine engine;
    
    private transient final Filter filter;
    
    private final TemplateFormatter formatter;
    
	private final Map<String, Template> importMacros;

	private final Map<String, Template> macros;

    public AbstractTemplate(Engine engine, Filter filter, 
    		Formatter<?> formatter, Map<Class<?>, Object> functions,
    		Map<String, Template> importMacros) {
		this.engine = engine;
		this.filter = filter;
		this.formatter = new TemplateFormatter(engine, formatter);
		this.importMacros = importMacros;
		this.macros = initMacros(engine, filter, formatter, functions, importMacros);
	}

    protected String filter(String value) {
        if (filter != null)
            return filter.filter(value);
        return value;
    }
    
	protected Template getMacro(Context context, String key, Template defaultValue) {
    	Object value = context.get(key);
    	if (! (value instanceof Template)) {
    		return defaultValue;
    	}
    	return (Template) value;
    }

	protected TemplateFormatter getFormatter() {
		return formatter;
	}

	public Reader getReader() throws IOException {
		return new StringReader(getSource());
	}

	public InputStream getInputStream() throws IOException {
		return new UnsafeByteArrayInputStream(getSource().getBytes(getEncoding()));
	}

	public Object evaluate() {
		return evaluate(null);
	}

	public void render(OutputStream output) throws IOException {
		render(null, output);
	}

	public void render(Writer writer) throws IOException {
		render(null, writer);
	}

	protected Map<String, Template> getImportMacros() {
		return importMacros;
	}

	private Map<String, Template> initMacros(Engine engine, Filter filter, 
			Formatter<?> formatter, Map<Class<?>, Object> functions,
			Map<String, Template> importMacros) {
		Map<String, Template> macros = new HashMap<String, Template>();
		Map<String, Class<?>> macroTypes = getMacroTypes();
		if (macroTypes == null || macroTypes.size() == 0) {
			return Collections.unmodifiableMap(macros);
		}
		for (Map.Entry<String, Class<?>> entry : macroTypes.entrySet()) {
			try {
				Template macro = (Template) entry.getValue()
						.getConstructor(Engine.class, Filter.class, Formatter.class, Map.class, Map.class)
						.newInstance(engine, filter, formatter, functions, importMacros);
				macros.put(entry.getKey(), macro);
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		return Collections.unmodifiableMap(macros);
	}
	
	public Engine getEngine() {
		return engine;
	}

	public Map<String, Template> getMacros() {
		return macros;
	}
	
	protected abstract Map<String, Class<?>> getMacroTypes();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        String name = getName();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractTemplate other = (AbstractTemplate) obj;
        String name = getName();
        String otherName = other.getName();
        if (name == null) {
            if (otherName != null) return false;
        } else if (!name.equals(otherName)) return false;
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }

}
