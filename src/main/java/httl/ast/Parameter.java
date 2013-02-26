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

import httl.Node;
import httl.Visitor;

import java.text.ParseException;
import java.util.Map;

/**
 * Node. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class Parameter implements Node {

	private final int offset;
	
	private final Map<String, Class<?>> parameterTypes;

	public Parameter(Map<String, Class<?>> parameterTypes, int offset) {
		this.offset = offset;
		this.parameterTypes = parameterTypes;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public int getOffset() {
		return offset;
	}

	public Map<String, Class<?>> getVariableTypes() {
		return parameterTypes;
	}

	public Class<?>[] getReturnTypes() throws ParseException {
		Class<?> type = getReturnType();
		return type == null ? new Class<?>[0] : new Class<?>[] { type };
	}

	public String getGenericVariableName() {
		return getGenericVariableName(this);
	}

	protected static String getGenericVariableName(Parameter node) {
		if (node instanceof Variable) {
			return ((Variable)node).getName();
		}
		while (node instanceof BinaryOperator) {
			String name = ((BinaryOperator)node).getName();
			if ("+".equals(name) || "||".equals(name)
					 || "&&".equals(name)
					 || "entrySet".equals(name)) {
				node = ((BinaryOperator)node).getLeftParameter();
				if (node instanceof Variable) {
					return ((Variable)node).getName();
				}
			} else {
				return null;
			}
		}
		return null;
	}

	public abstract Class<?> getReturnType() throws ParseException;

	public abstract String getCode() throws ParseException;

}
