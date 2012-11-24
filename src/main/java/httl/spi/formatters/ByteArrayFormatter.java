package httl.spi.formatters;

import java.nio.charset.Charset;

import httl.spi.Formatter;

public class ByteArrayFormatter implements Formatter<byte[]> {
	
	private Charset outputCharset;
	
	public void setOutputEncoding(String outputEncoding) {
		this.outputCharset = Charset.forName(outputEncoding);
	}

	public String format(byte[] value) {
		return outputCharset == null ? new String(value) : new String(value, outputCharset);
	}

}
