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
package httl.spi.translators.templates;

import httl.ast.AstVisitor;
import httl.ast.SetDirective;
import httl.ast.Variable;
import httl.util.OrderedMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VariableVisitor. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class VariableVisitor extends AstVisitor {

	private Class<?> defaultVariableType = Object.class;
	
	private boolean addDefault;

	private final List<String> variableNames = new ArrayList<String>();

	private final List<Class<?>> variableTypes = new ArrayList<Class<?>>();

	public VariableVisitor(Class<?> defaultVariableType, boolean addDefault) {
		this.defaultVariableType = defaultVariableType;
		this.addDefault = addDefault;
	}

	public Map<String, Class<?>> getVariables() {
		return new OrderedMap<String, Class<?>>(
				variableNames.toArray(new String[variableNames.size()]), 
				variableTypes.toArray(new Class<?>[variableTypes.size()]));
	}

	@Override
	public void visit(SetDirective node) throws ParseException {
		if (node.getExpression() == null) {
			Type type = node.getType();
			Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
			if (clazz == null) {
				if (addDefault) {
					clazz = defaultVariableType;
				} else {
					return;
				}
			}
			int i = variableNames.indexOf(node.getName());
			if (i >= 0) {
				Class<?> cls = variableTypes.get(i);
				if(! cls.equals(clazz) 
						&& ! cls.isAssignableFrom(clazz) 
						&& ! clazz.isAssignableFrom(cls)) {
					throw new ParseException("Defined different type variable " + node.getName() + ", conflict types: " + cls + ", " + clazz, node.getOffset());
				}
			} else {
				variableNames.add(node.getName());
				variableTypes.add(clazz);
			}
		}
	}

	@Override
	public void visit(Variable node) throws ParseException {
		if (addDefault && ! variableNames.contains(node.getName())) {
			variableNames.add(node.getName());
			variableTypes.add(defaultVariableType);
		}
	}

}