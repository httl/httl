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

import httl.Context;
import httl.internal.util.StringUtils;
import httl.web.WebEngine;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttlServlet. (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		WebEngine.setServletContext(getServletContext());
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			Context context = Context.getContext();
			context.put("request", request);
			context.put("response", response);
			WebEngine.getEngine().getTemplate(getTemplatePath(request), request.getLocale()).render(response);
		} catch (ParseException e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	protected String getTemplatePath(HttpServletRequest request) {
		String path = request.getPathInfo();
		if (StringUtils.isEmpty(path)) {
			path = request.getServletPath();
		}
		if (StringUtils.isEmpty(path)) {
			path = request.getRequestURI();
			String contextPath = request.getContextPath();
			if (contextPath != null && ! "/".equals(contextPath)
					&& path != null && path.startsWith(contextPath)) {
				path = path.substring(contextPath.length());
			}
		}
		if (StringUtils.isEmpty(path)) {
			path = getRootPath();
		}
		return path;
	}

	protected String getRootPath() {
		return "/index.httl";
	}

}