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

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import httl.spi.Codec;
import httl.spi.Compiler;
import httl.spi.codecs.json.JSON;
import httl.spi.converters.BeanMapConverter;
import httl.internal.util.StringUtils;

/**
 * Json Codec. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class JsonCodec implements Codec {

	private final BeanMapConverter converter = new BeanMapConverter();

	public void setCompiler(Compiler compiler) {
		this.converter.setCompiler(compiler);
	}
	
	private boolean jsonWithClass;

	protected boolean isJsonWithClass() {
		return jsonWithClass;
	}

	/**
	 * httl.properties: json.with.class=true
	 */
	public void setJsonWithClass(boolean jsonWithClass) {
		this.jsonWithClass = jsonWithClass;
	}

	public String getFormat() {
		return "json";
	}

	public boolean isDecodable(String str) {
		return StringUtils.isNotEmpty(str) 
				&& (str.startsWith("{") || str.startsWith("[")
				|| (str.startsWith("\"") && str.endsWith("\""))
				|| (str.startsWith("\'") && str.endsWith("\'"))
				|| StringUtils.isNumber(str) || "null".equals(str) 
				|| "true".equals(str) || "false".equals(str));
	}

	public String encode(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return JSON.json(value, jsonWithClass, converter);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T decode(String str, Class<T> type) throws ParseException {
		if (str == null) {
			return null;
		}
		if (type == null) {
			return (T) JSON.parse(str, Map.class, converter);
		}
		return JSON.parse(str, type, converter);
	}

}
