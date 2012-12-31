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
package httl.spi.sequences;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * CharacterSequence. (SPI, Prototype, ThreadSafe)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CharacterSequence implements List<Character>, Serializable {

	private static final long serialVersionUID = 1L;

	private char begin;

	private char end;

	private char min;

	private char max;

	private boolean asc;

	private int size;
	
	public CharacterSequence(Character begin, Character end) {
	    this(begin == null ? '\0' : begin.charValue(), end == null ? '\0' : end.charValue());
	}
	
	public CharacterSequence(char begin, char end) {
		this.begin = begin;
		this.end = end;
		if (begin < end) {
			min = begin;
			max = end;
			asc = true;
		} else {
			min = end;
			max = begin;
			asc = false;
		}
		size = max - min + 1;
	}

	public char getBegin() {
		return begin;
	}

	public char getEnd() {
		return end;
	}

	public int size() {
		return size;
	}

	public boolean isAsc() {
		return asc;
	}

	public boolean isEmpty() {
		return false;
	}

	public boolean contains(Object o) {
		char i = ((Character)o).charValue();
		return i >= min && i <= max;
	}

	public Iterator<Character> iterator() {
		return new CharacterSequenceIterator(begin, end);
	}

	public Object[] toArray() {
		Character[] arr = new Character[size];
		for (int i = 0, n = arr.length; i < n; i ++) {
			arr[i] = Character.valueOf((char)(begin + (asc?i:-i)));
		}
		return arr;
	}

	@SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] arr) {
		return (T[]) toArray();
	}

	public boolean containsAll(Collection<?> c) {
		Iterator<?> it = c.iterator();
		Object obj;
		while(it.hasNext()) {

			obj = it.next();
			if(!contains(obj)) {
				return false;
			}
		}

		return true;
	}

	public Character get(int index) {
		char value = (char)(begin + (asc ? index : - index));

		if ((asc && value > end) || (! asc && value < end))
			throw new IndexOutOfBoundsException("index = " + index);

		return Character.valueOf(value);
	}

	public int indexOf(Object o) {
		char i = ((Character)o).charValue();
		if (i < min || i > max) {
			return -1;
		}
		return asc?i - begin:begin - i;
	}

	public int lastIndexOf(Object o) {
		return indexOf(o);
	}

	public ListIterator<Character> listIterator() {
		return new CharacterSequenceIterator(begin, end);
	}

	public ListIterator<Character> listIterator(int index) {
		char beginIndex = (char)(begin + (asc ? index : - index));

		if ((asc && beginIndex > end) || (! asc && beginIndex < end))
			throw new IndexOutOfBoundsException("index = " + index);

		return new CharacterSequenceIterator(beginIndex, end);
	}

	public List<Character> subList(int fromIndex, int toIndex) {
		if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
        if(asc) {
        	return new CharacterSequence((char)(begin + fromIndex), (char)(begin + fromIndex + toIndex));
        }else {
        	return new CharacterSequence((char)(begin - fromIndex), (char)(begin - fromIndex - toIndex));
        }
	}

	public boolean add(Character o) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends Character> c) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int index, Collection<? extends Character> c) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Character set(int index, Character element) {
		throw new UnsupportedOperationException();
	}

	public void add(int index, Character element) {
		throw new UnsupportedOperationException();
	}

	public Character remove(int index) {
		throw new UnsupportedOperationException();
	}

	private String buffer;

	public String toString() {
		if (buffer == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (char i = min; i <= max; i ++) {
				if (i != min)
					sb.append(",");
				sb.append((char)(asc ? i : max + min - i));
			}
			sb.append("]");
			buffer = sb.toString();
		}
		return buffer;
	}

	private static final class CharacterSequenceIterator implements ListIterator<Character> {

		private char begin;

		private char end;

		private char cur;

		private int inc;

		public CharacterSequenceIterator(char begin, char end) {
			this.begin = begin;
			this.end = end;
			cur = begin;
			inc = (begin < end ? 1 : -1);
		}

		public boolean hasNext() {
			return (inc>0?cur <= end:cur>=end);
		}

		public Character next() {
			if (isOverFlowEnd())
				throw new java.util.NoSuchElementException("IndexOutOfBounds");

			Character next;
			if(isOverFlowOfBegin()) {
				cur = (char)(cur + inc);
				next = Character.valueOf(cur);
			} else {
				next = Character.valueOf(cur);
			}
			cur = (char)(cur + inc);

			return next;
		}

		public int nextIndex() {
			if (isOverFlowEnd())
				throw new java.util.NoSuchElementException("IndexOutOfBounds");

			int index;
			if(isOverFlowOfBegin()) {
				cur = (char)(cur + inc);
				index = (inc > 0?cur - begin: begin - cur);
			} else {
				index = (inc > 0?cur - begin: begin - cur);
			}
			cur = (char)(cur + inc);

			return index;
		}

		public boolean hasPrevious() {
			return (inc>0?cur >= begin:cur<=begin);
		}

		public Character previous() {
			if (isOverFlowOfBegin())
				throw new java.util.NoSuchElementException("IndexOutOfBounds");

			Character prev;
			if(isOverFlowEnd()) {
				cur = (char)(cur - inc);
				prev = Character.valueOf(cur);
			} else {
				prev = Character.valueOf(cur);
			}
			cur = (char)(cur - inc);

			return prev;
		}

		public int previousIndex() {
			if (isOverFlowOfBegin())
				throw new java.util.NoSuchElementException("IndexOutOfBounds");

			int index;
			if(isOverFlowEnd()) {
				cur = (char)(cur - inc);
				index = (inc > 0?cur - begin: begin - cur);
			} else {
				index = (inc > 0?cur - begin: begin - cur);
			}
			cur = (char)(cur - inc);

			return index;
		}

		public void add(Character o) {
			throw new UnsupportedOperationException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(Character o) {
			throw new UnsupportedOperationException();
		}

		private boolean isOverFlowOfBegin() {
			return (inc > 0 && cur < begin) ||
					(inc < 0 && cur > begin);
		}

		private boolean isOverFlowEnd() {
			return (inc > 0 && cur > end) ||
					(inc < 0 && cur < end);
		}

	}

}
