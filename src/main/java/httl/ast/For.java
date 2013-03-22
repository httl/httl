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

import httl.internal.util.CollectionUtils;
import httl.internal.util.Status;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

/**
 * For
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class For extends BlockDirective {

	private Class<?> type;

	private String name;

	private Expression expression;

	private String forVariable;

	public For() {
	}

	public For(Class<?> type, String name, Expression expression, String forVariable, int offset) {
		super(offset);
		this.type = type;
		this.name = name;
		this.expression = expression;
		this.forVariable = forVariable;
	}

	public void render(Map<String, Object> context, Object out) throws IOException,
			ParseException {
		Object list = expression.evaluate(context);
		Status old = (Status) context.get(forVariable);
		Status status = new Status(old, list);
		context.put(forVariable, status);
		for(Iterator<?> iterator = CollectionUtils.toIterator(list); iterator.hasNext();) {
			context.put(name, iterator.next());
			try {
				super.render(context, out);
			} catch (BreakException e) {
				break;
			}
		}
		if (old != null) {
			context.put(forVariable, old);
		} else {
			context.remove(forVariable);
		}
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
		return "#for(" + type.getCanonicalName() + " " + name + " : " + expression + ")";
	}

}
