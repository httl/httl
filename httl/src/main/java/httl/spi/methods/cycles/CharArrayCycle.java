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
package httl.spi.methods.cycles;

/**
 * CharArrayCycle.
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CharArrayCycle {

	private final char[] values;
	
	private final int size;

	private int index;

	public CharArrayCycle(char[] values) {
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException("cycle values == null");
		}
		this.values = values;
		this.size = values.length;
		this.index = -1;
	}

	public char getNext() {
		index += 1;
		if (index >= size)
			index = 0;
		return values[index];
	}

	public char getValue() {
		if (index == -1)
			return values[0];
		return values[index];
	}

	public char[] getValues() {
		return values;
	}

	public int getSize() {
		return size;
	}

	public int getIndex() {
		return index;
	}

	public String toString() {
		return String.valueOf(getNext());
	}

}