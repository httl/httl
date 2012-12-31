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
 * IntegerSequence. (SPI, Prototype, ThreadSafe)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class IntegerSequence implements List<Integer>, Serializable {

	private static final long serialVersionUID = 1L;

	private int begin;

	private int end;

	private int min;

	private int max;

	private boolean asc;

	private int size;
	
	public IntegerSequence(Short begin, Short end) {
        this(begin == null ? 0 : begin.intValue(), end == null ? 0 : end.intValue());
    }
    
    public IntegerSequence(Long begin, Long end) {
        this(begin == null ? 0 : begin.intValue(), end == null ? 0 : end.intValue());
    }

    public IntegerSequence(Integer begin, Integer end) {
        this(begin == null ? 0 : begin.intValue(), end == null ? 0 : end.intValue());
    }
	
	public IntegerSequence(short begin, short end) {
        this((int) begin, (int) end);
    }
	
	public IntegerSequence(long begin, long end) {
	    this((int) begin, (int) end);
	}

	public IntegerSequence(int begin, int end) {
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

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public int getMax() {
		return max;
	}

	public int getMin() {
		return min;
	}

	public boolean isAsc() {
		return asc;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return false;
	}

	public boolean contains(Object o) {
		int i = ((Integer)o).intValue();
		return i >= min && i <= max;
	}

	public Iterator<Integer> iterator() {
		return new IntegerSequenceIterator(begin, end);
	}

	public Object[] toArray() {
		Integer[] arr = new Integer[size];
		for (int i = 0, n = arr.length; i < n; i ++) {
			if(asc) {
				arr[i] = Integer.valueOf(begin + i);
			} else {
				arr[i] = Integer.valueOf(begin - i);
			}
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

	public Integer get(int index) {
		int value = begin + (asc ? index : - index);

		if ((asc && value > end) || (! asc && value < end))
			throw new IndexOutOfBoundsException("index = " + index);

		return Integer.valueOf(value);
	}

	public int indexOf(Object o) {
		int i = ((Integer)o).intValue();
		if (i < min || i > max) {
			return -1;
		}
		return (asc? i - begin:begin - i);
	}

	public int lastIndexOf(Object o) {
		return indexOf(o);
	}

	public ListIterator<Integer> listIterator() {
		return new IntegerSequenceIterator(begin, end);
	}

	public ListIterator<Integer> listIterator(int index) {
		int beginIndex = begin + (asc ? index : - index);

		if ((asc && beginIndex > end) || (! asc && beginIndex < end))
			throw new IndexOutOfBoundsException("index = " + index);

		return new IntegerSequenceIterator(beginIndex, end);
	}

	public List<Integer> subList(int fromIndex, int toIndex) {
		if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
        if(asc) {
        	return new IntegerSequence(begin + fromIndex, begin + fromIndex + toIndex);
        } else {
        	return new IntegerSequence(begin - fromIndex, begin - fromIndex - toIndex);
        }
	}

	public boolean add(Integer o) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends Integer> c) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int index, Collection<? extends Integer> c) {
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

	public Integer set(int index, Integer element) {
		throw new UnsupportedOperationException();
	}

	public void add(int index, Integer element) {
		throw new UnsupportedOperationException();
	}

	public Integer remove(int index) {
		throw new UnsupportedOperationException();
	}

	private String buffer;

	public String toString() {
		if (buffer == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (int i = min; i <= max; i ++) {
				if (i != min)
					sb.append(",");
				sb.append(asc ? i : max + min - i);
			}
			sb.append("]");
			buffer = sb.toString();
		}
		return buffer;
	}

	private static final class IntegerSequenceIterator implements ListIterator<Integer> {

		private int begin;

		private int end;

		private int cur;

		private int inc;

		public IntegerSequenceIterator(int begin, int end) {
			this.begin = begin;
			this.end = end;
			cur = begin;
			inc = begin < end ? 1 : -1;
		}

		public boolean hasNext() {
			return (inc>0?cur <= end:cur>=end);
		}

		public Integer next() {

			if (isOverFlowEnd())
				throw new java.util.NoSuchElementException("IndexOutOfBounds");

			Integer next;
			if(isOverFlowOfBegin()) {
				cur = cur + inc;
				next = Integer.valueOf(cur);
			} else {
				next = Integer.valueOf(cur);
			}
			cur = cur + inc;

			return next;
		}

		public int nextIndex() {

			if (isOverFlowEnd())
				throw new java.util.NoSuchElementException("IndexOutOfBounds");

			int index;
			if(isOverFlowOfBegin()) {
				cur = cur + inc;
				index = (inc > 0?cur - begin: begin - cur);
			} else {
				index = (inc > 0?cur - begin: begin - cur);
			}
			cur = cur + inc;

			return index;
		}

		public boolean hasPrevious() {
			return (inc>0?cur >= begin:cur<=begin);
		}

		public Integer previous() {

			if (isOverFlowOfBegin())
				throw new java.util.NoSuchElementException("IndexOutOfBounds");

			Integer prev;
			if(isOverFlowEnd()) {
				cur = cur - inc;
				prev = Integer.valueOf(cur);
			} else {
				prev = Integer.valueOf(cur);
			}
			cur = cur - inc;

			return prev;
		}

		public int previousIndex() {

			if (isOverFlowOfBegin())
				throw new java.util.NoSuchElementException("IndexOutOfBounds");

			int index;
			if(isOverFlowEnd()) {
				cur = cur - inc;
				index = (inc > 0?cur - begin: begin - cur);
			} else {
				index = (inc > 0?cur - begin: begin - cur);
			}
			cur = cur - inc;

			return index;
		}

		public void add(Integer o) {
			throw new UnsupportedOperationException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(Integer o) {
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
