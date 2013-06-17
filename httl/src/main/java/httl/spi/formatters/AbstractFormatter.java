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
package httl.spi.formatters;

import httl.spi.Formatter;

import java.io.UnsupportedEncodingException;

/**
 * Abstract Formatter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setFormatter(Formatter)
 * @see httl.spi.translators.InterpretedTranslator#setFormatter(Formatter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractFormatter<T> implements Formatter<T> {

	protected String outputEncoding;

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
		return toChars(toString(key, value));
	}

	public byte[] toBytes(String key, T value) { // slowly
		if (value == null) {
			return new byte[0];
		}
		return toBytes(toString(key, value));
	}
	
	protected char[] toChars(String str) {
		if (str == null) {
			return new char[0];
		}
		return str.toCharArray();
	}

	protected byte[] toBytes(String str) {
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