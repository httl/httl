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

import java.text.ParseException;

/**
 * IfDirective. (SPI, Prototype, ThreadSafe)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class IfDirective extends BlockDirective {
	
	private final Expression expression;

	public IfDirective(Expression expression, int offset) throws ParseException {
		super(offset);
		if (expression == null) {
			throw new ParseException("The if expression is required.", offset);
		}
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		return "#if(" + expression + ")";
	}

}