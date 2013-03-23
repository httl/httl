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

/**
 * Value
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Value extends Statement {

	private final Expression expression;

	private final boolean noFilter;

	public Value(Expression expression, boolean noFilter, int offset) {
		super(offset);
		this.expression = expression;
		this.noFilter = noFilter;
	}

	public Expression getExpression() {
		return expression;
	}

	public boolean isNoFilter() {
		return noFilter;
	}

	@Override
	public String toString() {
		return "$" + (noFilter ? "!" : "") + "{" + expression + "}";
	}

}
