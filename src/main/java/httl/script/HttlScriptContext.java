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
package httl.script;

import httl.Context;
import httl.Template;
import httl.spi.resolvers.GlobalResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;

/**
 * HttlScriptContext (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlScriptContext implements ScriptContext {
	
	private static final List<Integer> SCOPES = Arrays.asList(GLOBAL_SCOPE, ENGINE_SCOPE);

	private Reader reader;
	
	private Writer writer;

	private Writer errorWriter;

	private Map<String, Object> getContext(int scope) {
		if (scope == GLOBAL_SCOPE) {
			return GlobalResolver.getGlobal();
		}
		return Context.getContext();
	}

	public Object getAttribute(String name) {
		return getContext(ENGINE_SCOPE).get(name);
	}

	public Object getAttribute(String name, int scope) {
		return getContext(scope).get(name);
	}

	public Object removeAttribute(String name, int scope) {
		return getContext(scope).remove(name);
	}

	public void setAttribute(String name, Object value, int scope) {
		getContext(scope).put(name, value);
	}

	public Bindings getBindings(int scope) {
		return new SimpleBindings(getContext(scope));
	}

	@SuppressWarnings("unchecked")
	public void setBindings(Bindings bindings, int scope) {
		getContext(scope).putAll(bindings);
	}

	public int getAttributesScope(String name) {
		if (getContext(GLOBAL_SCOPE).containsKey(name))
			return GLOBAL_SCOPE;
		return ENGINE_SCOPE;
	}

	public List<Integer> getScopes() {
		return SCOPES;
	}

	public Reader getReader() {
		if (reader == null) {
			Template template = Context.getContext().getTemplate();
			if (template != null) {
				try {
					return template.getReader();
				} catch (IOException e) {
					return null;
				}
			}
		}
		return reader;
	}

	public void setReader(Reader reader) {
		this.reader = reader;
	}

	public Writer getWriter() {
		if (writer == null)
			return (Writer) Context.getContext().getOut();
		return writer;
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	public Writer getErrorWriter() {
		if (errorWriter == null)
			errorWriter = new PrintWriter(System.err);
		return errorWriter;
	}

	public void setErrorWriter(Writer writer) {
		this.errorWriter = writer;
	}

}