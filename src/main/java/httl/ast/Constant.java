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
 * Constant. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Constant extends Expression {

	public static final Constant NULL = new Constant(null);

	public static final Constant EMPTY = new Constant(new Object());

	public static final Constant TRUE = new Constant(true);

	public static final Constant FALSE = new Constant(false);

	private final Object value;

	public Constant(Object value){
		super(0);
		this.value = value;
	}

	public String getToken() {
		return toString();
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		if (value instanceof String) {
			return "\"" + value + "\"";
		} else if (value instanceof Character) {
			return "`" + value + "`";
		} else if (value instanceof Long) {
			return value + "L";
		} else if (value instanceof Float) {
			return value + "F";
		} else if (value instanceof Double) {
			return value + "D";
		}
		return String.valueOf(value);
	}

}
