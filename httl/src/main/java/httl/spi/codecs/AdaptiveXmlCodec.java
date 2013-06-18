package httl.spi.codecs;

import httl.spi.Codec;

import java.text.ParseException;

public class AdaptiveXmlCodec extends AbstractXmlCodec {

	private Codec codec;

	public AdaptiveXmlCodec() {
		try {
			codec = (Codec) Class.forName("httl.spi.codecs.XstreamCodec").newInstance();
		} catch (Throwable e) {
			codec = new XbeanCodec();
		}
	}

	public String toString(String key, Object value) {
		return codec.toString(key, value);
	}

	public char[] toChars(String key, Object value) {
		return codec.toChars(key, value);
	}

	public byte[] toBytes(String key, Object value) {
		return codec.toBytes(key, value);
	}

	public <T> T valueOf(String string, Class<T> type) throws ParseException {
		return codec.valueOf(string, type);
	}

	public <T> T valueOf(char[] chars, Class<T> type) throws ParseException {
		return codec.valueOf(chars, type);
	}

	public <T> T valueOf(byte[] bytes, Class<T> type) throws ParseException {
		return codec.valueOf(bytes, type);
	}

}
