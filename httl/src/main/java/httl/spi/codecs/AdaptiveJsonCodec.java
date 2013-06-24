package httl.spi.codecs;

import java.text.ParseException;

import httl.spi.Codec;
import httl.spi.Compiler;

public class AdaptiveJsonCodec extends AbstractJsonCodec {

	private Codec codec;

	public AdaptiveJsonCodec() {
		try {
			codec = (Codec) Class.forName("httl.spi.codecs.FastjsonCodec").newInstance();
		} catch (Throwable e) {
			try {
				codec = (Codec) Class.forName("httl.spi.codecs.JacksonCodec").newInstance();
			} catch (Throwable e2) {
				codec = new JsonCodec();
			}
		}
	}

	public void setCompiler(Compiler compiler) {
		if (codec instanceof JsonCodec) {
			((JsonCodec) codec).setCompiler(compiler);
		}
	}

	private void checkCodec() {
		if (codec == null) {
			throw new IllegalStateException("No such any json codec lib in classpath, Please add fastjson.jar or jackson.jar in your classpath.");
		}
	}

	public String toString(String key, Object value) {
		checkCodec();
		return codec.toString(key, value);
	}

	public char[] toChars(String key, Object value) {
		checkCodec();
		return codec.toChars(key, value);
	}

	public byte[] toBytes(String key, Object value) {
		checkCodec();
		return codec.toBytes(key, value);
	}

	public <T> T valueOf(String string, Class<T> type) throws ParseException {
		checkCodec();
		return codec.valueOf(string, type);
	}

	public <T> T valueOf(char[] chars, Class<T> type) throws ParseException {
		checkCodec();
		return codec.valueOf(chars, type);
	}

	public <T> T valueOf(byte[] bytes, Class<T> type) throws ParseException {
		checkCodec();
		return codec.valueOf(bytes, type);
	}

}
