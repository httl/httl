/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.methods;

import httl.util.ClassUtils;

import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * JsonMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class JsonMethod {

	private JsonMethod() {}

	public static String toJson(Object object) {
		if (object == null) {
			return null;
		}
		return JSON.toJSONString(object);
	}

	public static Map<String, Object> parseJson(String json) {
		if (json == null) {
			return null;
		}
		return JSON.parseObject(json);
	}

	public static Object parseJson(String json, String cls) {
		return parseJson(json, ClassUtils.forName(cls));
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseJson(String json, Class<T> cls) {
		if (json == null) {
			return null;
		}
		if (cls == null) {
			return (T) JSON.parseObject(json);
		}
		return JSON.parseObject(json, cls);
	}

}
