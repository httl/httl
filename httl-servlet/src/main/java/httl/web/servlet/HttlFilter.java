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
package httl.web.servlet;

import httl.web.WebEngine;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttlFilter. (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlFilter implements Filter {

	private String suffix;

	public void init(FilterConfig config) throws ServletException {
		suffix = WebEngine.getTemplateSuffix(config.getServletContext());
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
	}

	public void doFilter(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
		try {
			WebEngine.setRequestAndResponse(request, response);
			WebEngine.getEngine().getTemplate(getTemplatePath(request), request.getLocale()).render(response);
		} catch (ParseException e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	public void destroy() {
	}
	
	protected String getTemplatePath(HttpServletRequest request) {
		String path = request.getPathInfo();
		if (path == null || path.length() == 0) {
			path = request.getServletPath();
		}
		if (path == null || path.length() == 0) {
			path = request.getRequestURI();
			String contextPath = request.getContextPath();
			if (contextPath != null && ! "/".equals(contextPath)
					&& path != null && path.startsWith(contextPath)) {
				path = path.substring(contextPath.length());
			}
		}
		if (path == null || path.length() == 0) {
			path = getRootPath();
		}
		if (suffix != null && suffix.length() > 0 && ! path.endsWith(suffix)) {
			int i = path.lastIndexOf('.');
			if (i > 0 && i > path.lastIndexOf('/')) {
				path = path.substring(0, i);
			}
			path += suffix;
		}
		return path;
	}
	
	protected String getRootPath() {
		return "/index";
	}

}