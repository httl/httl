package httl.spi.formatters;

import httl.spi.Formatter;

public abstract class AbstractFormatter<T> implements Formatter<T> {

	public char[] toChars(T value) {
		return toString(value).toCharArray(); // slowly
	}

	public byte[] toBytes(T value) {
		return toString(value).getBytes(); // slowly
	}

}
