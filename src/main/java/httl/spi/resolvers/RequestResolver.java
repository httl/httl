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
package httl.spi.resolvers;

import httl.spi.Resolver;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class RequestResolver implements Resolver, Filter {

    private static ThreadLocal<ServletRequest> REQUEST_LOCAL = new ThreadLocal<ServletRequest>();

    private static ThreadLocal<ServletResponse> RESPONSE_LOCAL = new ThreadLocal<ServletResponse>();

    public static ServletRequest getServletRequest() {
    	return REQUEST_LOCAL.get();
    }

	public static void setServletRequest(ServletRequest request) {
		if (request == null) {
			REQUEST_LOCAL.remove();
		} else {
			REQUEST_LOCAL.set(request);
		}
	}
	
	public static void removeServletRequest() {
		REQUEST_LOCAL.remove();
	}

    public static ServletResponse getServletResponse() {
    	return RESPONSE_LOCAL.get();
    }

	public static void setServletResponse(ServletResponse response) {
		if (response == null) {
			RESPONSE_LOCAL.remove();
		} else {
			RESPONSE_LOCAL.set(response);
		}
	}

	public static void removeServletResponse() {
		RESPONSE_LOCAL.remove();
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		setServletRequest(request);
		setServletResponse(response);
		try {
			chain.doFilter(request, response);
		} finally {
			removeServletRequest();
			removeServletResponse();
		}
	}

	public String getProperty(String key) {
		ServletRequest request = REQUEST_LOCAL.get();
		if (request != null) {
			if ("locale".equals(key)) {
				Locale locale = request.getLocale();
				if (locale != null) {
					return locale.toString();
				}
			}
			ServletResponse response = RESPONSE_LOCAL.get();
			if ("output.encoding".equals(key)) {
				String encoding = response == null ? null : response.getCharacterEncoding();
				if (encoding != null && encoding.length() > 0) {
					return encoding;
				}
				encoding = request.getCharacterEncoding();
				if (encoding != null && encoding.length() > 0) {
					return encoding;
				}
			}
			if (request instanceof HttpServletRequest) {
				String header = ((HttpServletRequest) request).getHeader(key);
				if (header != null && header.length() > 0) {
					return header;
				}
			}
			Object attribute = request.getAttribute(key);
			if (attribute != null) {
				String value = String.valueOf(attribute);
				if (value != null && value.length() > 0) {
					return value;
				}
			}
		}
		return null;
	}

}
