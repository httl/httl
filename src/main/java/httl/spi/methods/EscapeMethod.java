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

import httl.internal.util.Base64;
import httl.internal.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * EscapeMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class EscapeMethod {
	
	private EscapeMethod() {}

	public static String escapeString(String value) {
		return StringUtils.escapeString(value);
	}

	public static String unescapeString(String value) {
		return StringUtils.unescapeString(value);
	}

	public static String escapeXml(String value) {
		return StringUtils.escapeXml(value);
	}

	public static String unescapeXml(String value) {
		return StringUtils.unescapeXml(value);
	}

	public static String escapeUrl(String value) {
		return escapeUrl(value, "UTF-8");
	}

	public static String escapeUrl(String value, String encoding) {
		try {
			return value == null ? null : URLEncoder.encode(value, encoding);
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}

	public static String unescapeUrl(String value) {
		return unescapeUrl(value, "UTF-8");
	}

	public static String unescapeUrl(String value, String encoding) {
		try {
			return value == null ? null : URLDecoder.decode(value, encoding);
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}

	public static String escapeBase64(String value) {
		return value == null ? null : Base64.encodeBytes(value.getBytes());
	}

	public static String unescapeBase64(String value) {
		try {
			return value == null ? null : new String(Base64.decode(value));
		} catch (IOException e) {
			return value;
		}
	}

}