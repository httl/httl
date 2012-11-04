/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.methods;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Cycle.
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Cycle {

	private final Object values;
	
	private final int length;

	private int index;

	public Cycle(Object values) {
		if (values == null) {
		    throw new IllegalArgumentException("cycle array == null");
		}
		if (values instanceof Collection) {
		    values = ((Collection<?>)values).toArray();
		}
		if (! values.getClass().isArray()
                || Array.getLength(values) == 0) {
            throw new IllegalArgumentException("cycle array length == 0");
        }
		this.values = values;
		this.length = Array.getLength(values);
		this.index = -1;
	}

	public Object getNext() {
		index += 1;
		if (index >= length)
			index = 0;
		return getValue();
	}

	public int getIndex() {
		return index;
	}

	public Object getValue() {
		if (index == -1)
			return null;
		return Array.get(values, index);
	}

	public Object values() {
		return values;
	}

	public String toString() {
		return String.valueOf(getNext());
	}

}
