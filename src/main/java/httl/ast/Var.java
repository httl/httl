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


import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * Var
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Var extends Directive {

	private Class<?> type;

	private String name;

	private Expression expression;

	private boolean parent;

	private boolean hide;

	public Var() {
	}

	public Var(Class<?> type, String name, Expression expression, boolean parent, boolean hide, int offset) {
		super(offset);
		this.type = type;
		this.name = name;
		this.expression = expression;
		this.parent = parent;
		this.hide = hide;
	}

	public void render(Map<String, Object> context, Object out) throws IOException,
			ParseException {
		if (expression != null) {
			context.put(name, expression.evaluate(context));
		}
	}

	public boolean isParent() {
		return parent;
	}

	public void setParent(boolean parent) {
		this.parent = parent;
	}

	public boolean isHide() {
		return hide;
	}

	public void setHide(boolean hide) {
		this.hide = hide;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		return "#var(" + type.getCanonicalName() + " " + name + 
				(expression == null ? "" : (parent ? " := " : " = ") + expression) + ")";
	}

}
