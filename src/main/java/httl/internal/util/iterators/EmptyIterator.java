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
package httl.internal.util.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyIterator<T> implements Iterator<T> {
	
	private static final EmptyIterator<Object> EMPTY_ITERATOR = new EmptyIterator<Object>();
	
	@SuppressWarnings("unchecked")
	public static <T> EmptyIterator<T> getEmptyIterator() {
		return (EmptyIterator<T>) EMPTY_ITERATOR;
	}
	
	private EmptyIterator() {}

	public boolean hasNext() {
		return false;
	}

	public T next() {
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException("remove() method is not supported");
	}

}