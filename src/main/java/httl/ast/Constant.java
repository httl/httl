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
 * Constant. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Constant extends Parameter {

	public static final Constant NULL = new Constant(null, null, "null");

	public static final Constant EMPTY = new Constant(null, null, "");

	public static final Constant TRUE = new Constant(true, boolean.class, "true");

	public static final Constant FALSE = new Constant(false, boolean.class, "false");
	
	private final Object value;

	private final Class<?> type;
	
	private final String literal;

	public Constant(Object value, Class<?> type, String literal){
		super(null, 0);
		this.value = value;
		this.type = type;
		this.literal = literal;
	}

	public Object getValue() {
		return value;
	}

	public Class<?> getReturnType() throws ParseException {
		return type;
	}

	public String getCode() throws ParseException {
		return literal;
	}

	@Override
	public String toString() {
		return literal;
	}

}
