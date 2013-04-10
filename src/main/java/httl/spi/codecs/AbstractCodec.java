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