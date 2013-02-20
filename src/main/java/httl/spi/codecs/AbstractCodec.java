package httl.spi.codecs;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import httl.spi.Codec;
import httl.spi.formatters.AbstractFormatter;

public abstract class AbstractCodec extends AbstractFormatter<Object> implements Codec {

	public boolean isValueOf(char[] chars) { // slowly
		return isValueOf(String.valueOf(chars));
	}
	
	public boolean isValueOf(byte[] bytes) { // slowly
		return isValueOf(toString(bytes));
	}

	public <T> T valueOf(char[] chars, Class<T> type) throws ParseException { // slowly
		return valueOf(String.valueOf(chars), type);
	}

	public <T> T valueOf(byte[] bytes, Class<T> type) throws ParseException { // slowly
		return valueOf(toString(bytes), type);
	}

	protected String toString(byte[] bytes) {
		String str;
		if (outputEncoding == null) {
			str = new String(bytes);
		} else {
			try {
				str = new String(bytes, outputEncoding);
			} catch (UnsupportedEncodingException e) {
				str = new String(bytes);
			}
		}
		return str;
	}

}
