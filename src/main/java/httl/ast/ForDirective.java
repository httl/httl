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

import httl.internal.util.StringUtils;

import java.lang.reflect.Type;
import java.text.ParseException;

/**
 * ForDirective. (SPI, Prototype, ThreadSafe)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ForDirective extends BlockDirective {

	private final Type type;

	private final String name;

	private final Expression expression;

	public ForDirective(Type type, String name, Expression expression, int offset) throws ParseException {
		super(offset);
		if (! StringUtils.isNamed(name)) {
			throw new ParseException("Illegal foreach name " + name + ", Can not contains any symbol.", offset);
		}
		if (expression == null) {
			throw new ParseException("The foreach expression is required.", offset);
		}
		this.type = type;
		this.name = name;
		this.expression = expression;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		String typeName = type == null ? "" : (type instanceof Class ? ((Class<?>) type).getCanonicalName() : type.toString());
		return "#for(" + typeName + " " + name + " : " + expression + ")";
	}

}