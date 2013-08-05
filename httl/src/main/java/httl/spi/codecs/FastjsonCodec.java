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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * FastJson Codec. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class FastjsonCodec extends AbstractJsonCodec {

	private static final SerializerFeature[] FEATURES = new SerializerFeature[] {SerializerFeature.SortField};

	private static final SerializerFeature[] FEATURES_WITH_CLASS = new SerializerFeature[] {SerializerFeature.SortField, SerializerFeature.WriteClassName};

	public String toString(String key, Object value) {
		if (value == null) {
			return null;
		}
		if (isJsonWithClass()) {
			return JSON.toJSONString(value, FEATURES_WITH_CLASS);
		}
		return JSON.toJSONString(value, FEATURES);
	}

	public byte[] toBytes(String key, Object value) {
		if (value == null) {
			return null;
		}
		if (isJsonWithClass()) {
			return JSON.toJSONBytes(value, FEATURES_WITH_CLASS);
		}
		return JSON.toJSONBytes(value, FEATURES);
	}

	@SuppressWarnings("unchecked")
	public <T> T valueOf(String str, Class<T> type) throws ParseException {
		if (str == null) {
			return null;
		}
		if (type == null) {
			return (T) JSON.parseObject(str);
		}
		return JSON.parseObject(str, type);
	}

	@SuppressWarnings("unchecked")
	public <T> T valueOf(byte[] str, Class<T> type) throws ParseException {
		if (str == null) {
			return null;
		}
		if (type == null) {
			return (T) JSON.parse(str);
		}
		return (T) JSON.parseObject(str, type);
	}

}