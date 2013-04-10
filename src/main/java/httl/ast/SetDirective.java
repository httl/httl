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
 * SetDirective. (SPI, Prototype, ThreadSafe)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class SetDirective extends LineDirective {

	private final Type type;

	private final String name;

	private final Expression expression;

	private final boolean export;

	private final boolean hide;

	public SetDirective(Type type, String name, Expression expression, boolean export, boolean hide, int offset) throws ParseException {
		super(offset);
		if (! StringUtils.isNamed(name)) {
			throw new ParseException("Illegal variable name " + name + ", Can not contains any symbol.", offset);
		}
		this.type = type;
		this.name = name;
		this.expression = expression;
		this.export = export;
		this.hide = hide;
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

	public boolean isExport() {
		return export;
	}

	public boolean isHide() {
		return hide;
	}

	@Override
	public String toString() {
		String typeName = type == null ? "" : (type instanceof Class ? ((Class<?>) type).getCanonicalName() : type.toString());
		return "#var(" + typeName + " " + name + 
				(expression == null ? "" : (export ? " := " : (hide ? " .= " : " = ")) + expression) + ")";
	}

}