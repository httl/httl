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

	private final Object value;
	
	private final boolean boxed;

	public Constant(Object value, boolean boxed, int offset) {
		super(offset);
		this.value = value;
		this.boxed = boxed;
	}

	public String getToken() {
		return toString();
	}

	public Object getValue() {
		return value;
	}

	public boolean isBoxed() {
		return boxed;
	}

	@Override
	public String toString() {
		return value == null && boxed ? "" : String.valueOf(value);
	}

}