package httl.util.iterators;

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
