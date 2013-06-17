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

import httl.Template;

import java.text.ParseException;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * HttlScript (Integration, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlScript extends CompiledScript {

	private final ScriptEngine engine;

	private final Template template;

	public HttlScript(ScriptEngine engine, Template template) {
		this.engine = engine;
		this.template = template;
	}

	@Override
	public Object eval(ScriptContext context) throws ScriptException {
		try {
			return template.evaluate();
		} catch (ParseException e) {
			throw new ScriptException(e.getMessage(), template.getName(), e.getErrorOffset());
		}
	}

	@Override
	public ScriptEngine getEngine() {
		return engine;
	}

}