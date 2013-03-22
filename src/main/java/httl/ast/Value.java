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

import httl.spi.Filter;
import httl.spi.Formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

/**
 * Value
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Value extends Directive {

	private Expression expression;

	private Formatter<Object> formatter;

	private Filter filter;

	private boolean noFilter;

	public Value() {
	}

	public Value(Expression expression, Formatter<Object> formatter, Filter filter, boolean noFilter, int offset) {
		super(offset);
		this.expression = expression;
		this.formatter = formatter;
		this.filter = filter;
		this.noFilter = noFilter;
	}

	public void render(Map<String, Object> context, Object out) throws IOException,
			ParseException {
		String key = expression.toString();
		Object value = expression.evaluate(context);
		if (out instanceof OutputStream) {
			((OutputStream) out).write(filter.filter(key, formatter.toBytes(key, value)));
		} else {
			((Writer) out).write(filter.filter(key, formatter.toString(key, value)));
		}
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public boolean isNoFilter() {
		return noFilter;
	}

	public void setNoFilter(boolean noFilter) {
		this.noFilter = noFilter;
	}

	@Override
	public String toString() {
		return "$" + (noFilter ? "!" : "") + "{" + expression + "}";
	}

}
