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

import httl.Engine;
import httl.internal.util.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * HttlScriptEngine (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlScriptEngine extends AbstractScriptEngine implements Compilable {

	private final ScriptEngineFactory factory;

	private final Engine engine;

	public HttlScriptEngine(ScriptEngineFactory factory, Engine engine) {
		this.factory = factory;
		this.engine = engine;
		super.setContext(new HttlScriptContext());
	}

	public ScriptEngineFactory getFactory() {
		return factory;
	}

	public Bindings createBindings() {
		return getContext().getBindings(ScriptContext.ENGINE_SCOPE);
	}

	public Object eval(String script, ScriptContext context) throws ScriptException {
		return compile(script).eval(context);
	}

	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		return compile(reader).eval(context);
	}

	public CompiledScript compile(String script) throws ScriptException {
		try {
			return new HttlScript(this, engine.parseTemplate(script));
		} catch (ParseException e) {
			throw new ScriptException(e.getMessage());
		}
	}

	public CompiledScript compile(Reader script) throws ScriptException {
		try {
			return compile(IOUtils.readToString(script));
		} catch (IOException e) {
			throw new ScriptException(e);
		}
	}

}