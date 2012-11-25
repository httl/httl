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
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class RequestResolver implements Resolver, Filter {

    private static ThreadLocal<RequestMap> LOCAL = new ThreadLocal<RequestMap>();

	public static void setRequest(HttpServletRequest request) {
		if (request != null) {
			LOCAL.set(new RequestMap(request));
		} else {
			removeRequest();
		}
	}
	
	public static void removeRequest() {
		LOCAL.remove();
	}

    public static HttpServletRequest getRequest() {
    	RequestMap map = LOCAL.get();
    	return map == null ? null : map.getRequest();
    }

	public static Map<String, Object> getPrarameters() {
		return LOCAL.get();
	}

	public static Map<String, Object> getAndCheckPrarameters() {
		Map<String, Object> parameters = LOCAL.get();
		if (parameters == null) {
			throw new IllegalStateException("servletRequest == null. Please add config in your /WEB-INF/web.xml: \n<filter>\n\t<filter-name>" 
					+ RequestResolver.class.getSimpleName() + "</filter-name>\n\t<filter-class>" + RequestResolver.class.getName() 
					+ "</filter-class>\n</filter>\n<filter-mapping>\n\t<filter-name>" + RequestResolver.class.getSimpleName() 
					+ "</filter-name>\n\t<url-pattern>/*</url-pattern>\n</filter-mapping>\n");
		}
		return parameters;
	}

	public String getProperty(String key) {
		Map<String, Object> parameters = getPrarameters();
		if (parameters != null) {
			Object value = parameters.get(key);
			if (value != null) {
				return String.valueOf(value);
			}
		}
		return null;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		setRequest((HttpServletRequest) request);
		try {
			chain.doFilter(request, response);
		} finally {
			removeRequest();
		}
	}

}
