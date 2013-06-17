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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ListCycle.
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ListCycle<T> {

	private final List<T> values;
	
	private final int size;

	private int index;
	
	public ListCycle(Collection<T> values) {
		this(values instanceof List ? (List<T>) values : new ArrayList<T>(values));
	}

	public ListCycle(List<T> values) {
		if (values == null || values.size() == 0) {
			throw new IllegalArgumentException("cycle values == null");
		}
		this.values = values;
		this.size = values.size();
		this.index = -1;
	}

	public Object getNext() {
		index += 1;
		if (index >= size)
			index = 0;
		return values.get(index);
	}

	public T getValue() {
		if (index == -1)
			return values.get(0);
		return values.get(index);
	}

	public List<T> getValues() {
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