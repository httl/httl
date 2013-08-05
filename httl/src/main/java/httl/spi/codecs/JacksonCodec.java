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

import java.text.ParseException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Jackson Codec. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class JacksonCodec extends AbstractJsonCodec {

	private static ObjectMapper mapper = new ObjectMapper();

	public static ObjectMapper getMapper() {
		return mapper;
	}

	public static void setMapper(ObjectMapper mapper) {
		JacksonCodec.mapper = mapper;
	}

	public String toString(String key, Object value) {
		if (value == null) {
			return null;
		}
		try {
			return mapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize " + value + " using jackson, cause: " + e.getMessage(), e);
		}
	}

	public byte[] toBytes(String key, Object value) {
		if (value == null) {
			return null;
		}
		try {
			return mapper.writeValueAsBytes(value);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize " + value + " using jackson, cause: " + e.getMessage(), e);
		}
	}

	public <T> T valueOf(String str, Class<T> type) throws ParseException {
		if (str == null) {
			return null;
		}
		try {
			return mapper.readValue(str, type);
		} catch (Exception e) {
			int offset = 0;
			if (e instanceof JsonProcessingException) {
				offset = (int) ((JsonProcessingException) e).getLocation().getCharOffset();
			}
			throw new ParseException("Failed to parse " + str + " using jackson, cause: " + e.getMessage(), offset);
		}
	}

	public <T> T valueOf(byte[] str, Class<T> type) throws ParseException {
		if (str == null) {
			return null;
		}
		try {
			return mapper.readValue(str, type);
		} catch (Exception e) {
			int offset = 0;
			if (e instanceof JsonProcessingException) {
				offset = (int) ((JsonProcessingException) e).getLocation().getCharOffset();
			}
			throw new ParseException("Failed to parse " + toString(str) + " using jackson, cause: " + e.getMessage(), offset);
		}
	}

}