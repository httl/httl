/*
 * Copyright 2011-2012 HTTL Team.
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
package httl.spi.resolvers;

import httl.spi.Resolver;
import httl.spi.loaders.ServletLoader;
import httl.util.ClassUtils;
import httl.util.StringUtils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ServletResolver. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ServletResolver implements Resolver, Filter {

	private static final String REQUEST_KEY = "request";

	private static final String RESPONSE_KEY = "response";

	private static final String SESSION_KEY = "session";

	private static final String APPLICATION_KEY = "application";

	private static final ThreadLocal<HttpServletRequest> REQUEST_LOCAL = new ThreadLocal<HttpServletRequest>();

	private static final ThreadLocal<HttpServletResponse> RESPONSE_LOCAL = new ThreadLocal<HttpServletResponse>();

	public static void set(HttpServletRequest request, HttpServletResponse response) {
		setRequest(request);
		setResponse(response);
	}

	public static void remove() {
		REQUEST_LOCAL.remove();
		RESPONSE_LOCAL.remove();
	}

	public static void setRequest(HttpServletRequest request) {
		if (request != null) {
			if (ServletLoader.getServletContext() == null) {
				ServletLoader.setServletContext(request.getSession().getServletContext());
			}
			REQUEST_LOCAL.set(request);
		} else {
			REQUEST_LOCAL.remove();
		}
	}

	public static void setResponse(HttpServletResponse response) {
		if (response != null) {
			RESPONSE_LOCAL.set(response);
		} else {
			RESPONSE_LOCAL.remove();
		}
	}

	public static HttpServletRequest getRequest() {
		return REQUEST_LOCAL.get();
	}
	
	public static HttpServletResponse getResponse() {
		return RESPONSE_LOCAL.get();
	}

	public static HttpServletRequest getAndCheckRequest() {
		return assertNotNull(getRequest());
	}

	public static HttpServletResponse getAndCheckResponse() {
		return assertNotNull(getResponse());
	}

	private static <T> T assertNotNull(T object) {
		if (object == null) {
			throw new IllegalStateException("servletRequest == null. Please add config in your /WEB-INF/web.xml: \n<filter>\n\t<filter-name>" 
					+ ServletResolver.class.getSimpleName() + "</filter-name>\n\t<filter-class>" + ServletResolver.class.getName() 
					+ "</filter-class>\n</filter>\n<filter-mapping>\n\t<filter-name>" + ServletResolver.class.getSimpleName() 
					+ "</filter-name>\n\t<url-pattern>/*</url-pattern>\n</filter-mapping>\n");
		}
		return object;
	}

	private Object getParameterValue(HttpServletRequest request, String key) {
		String[] values = request.getParameterValues(key);
		if (values == null || values.length == 0) {
			return null;
		} else if (values.length == 1) {
			return values[0];
		} else  {
			return values;
		}
	}
	
	private Object getCookieValue(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (key.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public Object get(String key) {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		Object value = ClassUtils.getProperty(request, key);
		if (value != null) {
			if ("contextPath".equals(key) && "/".equals(value)) {
				return ""; // e.g. ${contextPath}/index.html
			}
			return value;
		}
		value = request.getAttribute(key);
		if (value != null) {
			return value;
		}
		value = getParameterValue(request, key);
		if (value != null) {
			return value;
		}
		value = request.getHeader(key);
		if (value != null) {
			return value;
		}
		value = request.getHeader(StringUtils.splitCamelName(key, "-", true));
		if (value != null) {
			return value;
		}
		value = ClassUtils.getProperty(request.getSession(), key);
		if (value != null) {
			return value;
		}
		value = request.getSession().getAttribute(key);
		if (value != null) {
			return value;
		}
		value = getCookieValue(request, key);
		if (value != null) {
			return value;
		}
		value = ClassUtils.getProperty(request.getSession().getServletContext(), key);
		if (value != null) {
			return value;
		}
		value = request.getSession().getServletContext().getAttribute(key);
		if (value != null) {
			return value;
		}
		if (REQUEST_KEY.equals(key)) {
			return request;
		}
		if (RESPONSE_KEY.equals(key)) {
			return getResponse();
		}
		if (SESSION_KEY.equals(key)) {
			return request.getSession();
		}
		if (APPLICATION_KEY.equals(key)) {
			return request.getSession().getServletContext();
		}
		return value;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		set((HttpServletRequest) request, (HttpServletResponse) response);
		try {
			chain.doFilter(request, response);
		} finally {
			remove();
		}
	}

}