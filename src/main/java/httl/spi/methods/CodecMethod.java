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
package httl.spi.methods;

import httl.spi.Codec;
import httl.internal.util.ClassUtils;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CodecMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CodecMethod {

	private final Map<String, Codec> codecs = new ConcurrentHashMap<String, Codec>();

	private String[] importPackages;

	public void setCodecs(Codec[] codecs) {
		for (Codec codec : codecs) {
			this.codecs.put(codec.getFormat(), codec);
		}
	}

	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	private Codec getAndCheckCodec(String format) {
		Codec codec = codecs.get(format);
		if (codec == null) {
			throw new IllegalStateException("Unsupported encode format " + format + ", please add config codecs+=com.your." + format + "Codec" + " in httl.properties.");
		}
		return codec;
	}

	public String encode(Object value, String format) {
		return getAndCheckCodec(format).toString(null, value);
	}

	public Object decode(String value, String format) throws ParseException {
		return decode(value, (Class<?>) null, format);
	}

	public Object decode(String value, String type, String format) throws ParseException {
		return decode(value, ClassUtils.forName(importPackages, type), format);
	}

	public <T> T decode(String value, Class<T> type, String format) throws ParseException {
		return getAndCheckCodec(format).valueOf(value, type);
	}

	public String encodeJson(Object value) {
		return encode(value, "json");
	}

	public Object decodeJson(String value) throws ParseException {
		return decode(value, (Class<?>) null, "json");
	}

	public <T> T decodeJson(String value, Class<T> type) throws ParseException {
		return decode(value, type, "json");
	}

	public Object decodeJson(String value, String type) throws ParseException {
		return decode(value, type, "json");
	}

	public String encodeXml(Object value) {
		return encode(value, "xml");
	}

	public Object decodeXml(String value) throws ParseException {
		return decode(value, "xml");
	}

	public <T> T decodeXml(String value, Class<T> type) throws ParseException {
		return decode(value, type, "xml");
	}

	public Object decodeXml(String value, String type) throws ParseException {
		return decode(value, type, "xml");
	}

}