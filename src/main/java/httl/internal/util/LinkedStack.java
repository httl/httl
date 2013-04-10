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
package httl.internal.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;

/**
 * LinkedStack. (Tool, Prototype, ThreadUnsafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class LinkedStack<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final LinkedList<T> stack = new LinkedList<T>();
	
	private volatile List<T> unmodifiable;

	public int size() {
		return stack.size();
	}

	public boolean isEmpty() {
		return stack.size() == 0;
	}

	public void push(T value) {
		stack.addLast(value);
	}

	public T pop() {
		T value = peek();
		stack.removeLast();
		return value;
	}

	public T peek() {
		if (isEmpty()) {
			return null;
		}
 		return stack.getLast();
	}

	public void poke(T value) {
		if (! isEmpty()) {
			stack.removeLast();
		}
		push(value);
	}

	public void clear() {
		stack.clear();
	}

	public Iterator<T> iterator() {
		return toList().iterator();
	}

	public List<T> toList() {
		if (unmodifiable == null) {
			unmodifiable = Collections.unmodifiableList(stack);
		}
		return unmodifiable;
	}

	public String toString() {
		return stack.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stack == null) ? 0 : stack.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LinkedStack<?> other = (LinkedStack<?>) obj;
		if (stack == null) {
			if (other.stack != null) return false;
		} else if (!stack.equals(other.stack)) return false;
		return true;
	}

}