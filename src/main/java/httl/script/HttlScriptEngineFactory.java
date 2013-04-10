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
import httl.spi.Parser;
import httl.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * HttlScriptEngineFactory (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlScriptEngineFactory implements ScriptEngineFactory {

	private final List<String> names = Arrays.asList(new String[] { "httl" });

	private final List<String> extensions;

	private final List<String> mimeTypes;

	private final Engine engine;

	public HttlScriptEngineFactory() {
		String config = System.getProperty("httl.properties");
		this.engine = StringUtils.isEmpty(config) ? Engine.getEngine() : Engine.getEngine(config);
		
		List<String> extensions = new ArrayList<String>();
		extensions.add("httl");
		
		List<String> mimeTypes = new ArrayList<String>();
		mimeTypes.add("text/httl");
		mimeTypes.add("text/html");
		
		for (String suffix : engine.getProperty("template.suffix", new String[] { ".httl" })) {
			if (suffix.startsWith(".")) {
				suffix = suffix.substring(1);
			}
			if (! "httl".equals(suffix)) {
				extensions.add(suffix);
			}
			if (! "httl".equals(suffix) && ! "html".equals(suffix)) {
				mimeTypes.add("text/" + suffix);
			}
		}
		
		this.extensions = Collections.unmodifiableList(extensions);
		this.mimeTypes = Collections.unmodifiableList(mimeTypes);
	}

	public ScriptEngine getScriptEngine() {
		return new HttlScriptEngine(this, engine);
	}

	public List<String> getNames() {
		return names;
	}

	public List<String> getExtensions() {
		return extensions;
	}

	public List<String> getMimeTypes() {
		return mimeTypes;
	}

	public Object getParameter(String key) {
		return engine.getProperty(key);
	}

	public String getEngineName() {
		return "httl";
	}

	public String getEngineVersion() {
		return engine.getVersion();
	}

	public String getLanguageName() {
		Parser parser = engine.getProperty("parser", Parser.class);
		if (parser != null) {
			String name = parser.getClass().getSimpleName();
			String suffix = Parser.class.getSimpleName();
			if (name.endsWith(suffix)) {
				name = name.substring(0, name.length() - suffix.length());
			}
			return "httl-" + name.toLowerCase();
		}
		return "httl";
	}

	public String getLanguageVersion() {
		return "1.0.0";
	}

	public String getMethodCallSyntax(String obj, String m, String[] args) {
		StringBuilder buf = new StringBuilder();
		if (args != null && args.length > 0) {
			boolean first = true;
			for (String arg : args) {
				if (first)
					first = false;
				else
					buf.append(", ");
				buf.append(arg);
			}
		}
		return obj + "." + m + "(" + buf.toString() + ")";
	}

	public String getOutputStatement(String toDisplay) {
		return "${" + toDisplay + "}";
	}

	public String getProgram(String[] statements) {
		StringBuilder buf = new StringBuilder();
		for (String stat : statements) {
			buf.append(stat);
		}
		return buf.toString();
	}

}