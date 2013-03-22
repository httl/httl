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
package httl.ast;

import java.util.HashMap;
import java.util.Map;

/**
 * Variable. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Variable extends Expression {

	private Class<?> type;

	private final String name;

	public Variable(Class<?> type, String name, int offset) {
		super(offset);
		this.type = type;
		this.name = name;
	}

	public Map<String, Class<?>> getVariableTypes() {
		Map<String, Class<?>> map = new HashMap<String, Class<?>>();
		map.put(name, type);
		return map;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
