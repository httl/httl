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

import httl.Engine;
import httl.Template;
import httl.spi.loaders.ServletLoader;
import httl.util.WrappedMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebEngine {

    private static final String CONFIG_KEY = "httl.properties";

    private static final String WEBINF_CONFIG = "/WEB-INF/httl.properties";

    private static final String OUTPUT_STREAM = "output.stream";

    private static final String TEMPLATE_SUFFIX = "template.suffix";

	private static volatile Engine ENGINE;
	
	private static volatile boolean IS_OUTPUT_STREAM;
	
	public static void init() throws IOException {
		init(null);
	}

	public static void init(ServletContext servletContext) throws IOException {
		if (ENGINE == null) {
			synchronized (WebEngine.class) {
				if (ENGINE == null) {
					if (servletContext != null) {
						ServletLoader.setServletContext(servletContext);
					} else {
						servletContext = ServletLoader.getServletContext();
					}
					String config = servletContext == null ? null : servletContext.getInitParameter(CONFIG_KEY);
			        if (config != null && config.length() > 0) {
			            if (config.startsWith("/")) {
			                Properties properties = new Properties();
			                InputStream in = servletContext == null ? null : servletContext.getResourceAsStream(config);
			                if (in == null) {
			                	throw new FileNotFoundException("Not found httl config " + config + " in wepapp.");
			                }
			                properties.load(in);
			                ENGINE = Engine.getEngine(config, properties);
			            } else {
			            	ENGINE = Engine.getEngine(config);
			            }
			        } else {
			        	InputStream in = servletContext == null ? null : servletContext.getResourceAsStream(WEBINF_CONFIG);
			        	if (in != null) {
			        		Properties properties = new Properties();
			        		properties.load(in);
			        		ENGINE = Engine.getEngine(WEBINF_CONFIG, properties);
			        	} else {
			        		ENGINE = Engine.getEngine();
			        	}
			        }
			        IS_OUTPUT_STREAM = ENGINE.getProperty(OUTPUT_STREAM, false);
				}
			}
		}
	}

	public static Template getTemplate(String path) throws IOException, ParseException {
		init();
		return ENGINE == null ? null : ENGINE.getTemplate(path);
	}

	public static Template getTemplate(String path, String encoding) throws IOException, ParseException {
		init();
		return ENGINE == null ? null : ENGINE.getTemplate(path, encoding);
	}

	public static void render(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
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
        	response.sendError(HttpServletResponse.SC_NOT_FOUND);
        	return;
        }
        Engine engine = ENGINE;
        if (engine == null) {
        	init(request.getSession().getServletContext());
        }
        String suffix = engine.getProperty(TEMPLATE_SUFFIX);
        if (suffix != null && suffix.length() > 0 && ! path.endsWith(suffix)) {
        	path += suffix;
        }
		render(request, response, path, null);
	}

	public static void render(HttpServletRequest request, HttpServletResponse response, String path) throws IOException, ParseException {
		render(request, response, path, null);
	}

	public static void render(HttpServletRequest request, HttpServletResponse response, String path, Map<String, Object> model) throws IOException, ParseException {
		init(request.getSession().getServletContext());
		Map<String, Object> parameters = new ParameterMap(request);
		if (model != null) {
			parameters = new WrappedMap<String, Object>(parameters, model);
		}
		WebContext.setWebContext(request, response);
		try {
			Template template = ENGINE.getTemplate(path);
			if (IS_OUTPUT_STREAM) {
				template.render(parameters, response.getOutputStream());
			} else {
				template.render(parameters, response.getWriter());
			}
			response.flushBuffer();
		} catch (FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} finally {
			WebContext.removeWebContext();
		}
	}

}
