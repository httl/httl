package httl.spi.formatters;

import httl.spi.Formatter;

import java.io.UnsupportedEncodingException;

public abstract class AbstractFormatter<T> implements Formatter<T> {

	private String outputEncoding;

	/**
	 * httl.properties: output.encoding=UTF-8
	 */
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public char[] toChars(String key, T value) { // slowly
		if (value == null) {
			return new char[0];
		}
		String str = toString(key, value);
		if (str == null) {
			return new char[0];
		}
		return str.toCharArray();
	}

	public byte[] toBytes(String key, T value) { // slowly
		if (value == null) {
			return new byte[0];
		}
		String str = toString(key, value);
		if (str == null) {
			return new byte[0];
		}
		if (outputEncoding == null) {
			return str.getBytes();
		}
		try {
			return str.getBytes(outputEncoding);
		} catch (UnsupportedEncodingException e) {
			return str.getBytes();
		}
	}

}
