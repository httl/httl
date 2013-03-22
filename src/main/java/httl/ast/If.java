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

import httl.internal.util.ClassUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * If
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class If extends BlockDirective {
	
	private Expression expression;
	
	private String ifVariable;

	public If() {
		super();
	}

	public If(Expression expression, String ifVariable, int offset) {
		super(offset);
		this.expression = expression;
		this.ifVariable = ifVariable;
	}

	public void render(Map<String, Object> context, Object out) throws IOException,
			ParseException {
		if (ClassUtils.isTrue(expression.evaluate(context))) {
			super.render(context, out);
			context.put(ifVariable, Boolean.TRUE);
		} else {
			context.put(ifVariable, Boolean.FALSE);
		}
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "#if(" + expression + ")";
	}

}
