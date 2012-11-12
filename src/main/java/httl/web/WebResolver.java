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
package httl.web;

import httl.spi.Resolver;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebResolver implements Resolver {

	public String getProperty(String key) {
		HttpServletRequest request = WebContext.getWebContext().getRequest();
		if ("locale".equals(key)) {
			Locale locale = request.getLocale();
			if (locale != null) {
				return locale.toString();
			}
		}
		HttpServletResponse response = WebContext.getWebContext().getResponse();
		if ("output.encoding".equals(key)) {
			String encoding = response.getCharacterEncoding();
			if (encoding != null && encoding.length() > 0) {
				return encoding;
			}
			encoding = request.getCharacterEncoding();
			if (encoding != null && encoding.length() > 0) {
				return encoding;
			}
		}
		String header = request.getHeader(key);
		if (header != null && header.length() > 0) {
			return header;
		}
		Object attribute = request.getAttribute(key);
		if (attribute != null) {
			String value = String.valueOf(attribute);
			if (value != null && value.length() > 0) {
				return value;
			}
		}
		return null;
	}

}
