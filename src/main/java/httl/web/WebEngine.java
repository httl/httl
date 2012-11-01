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

import org.apache.struts2.ServletActionContext;

public class WebEngine {

    private static final String CONFIGURATION = "httl.properties";

    private static final String OUTPUT_STREAM = "output.stream";

	private static volatile Engine engine;
	
	private static volatile boolean outputStream;

	public static void init(ServletContext servletContext) throws IOException {
		if (engine == null) {
			synchronized (WebEngine.class) {
				if (engine == null) {
					ServletLoader.setServletContext(servletContext);
					String config = servletContext.getInitParameter(CONFIGURATION);
			        if (config != null && config.length() > 0) {
			            if (config.startsWith("/")) {
			                Properties properties = new Properties();
			                InputStream in = servletContext.getResourceAsStream(config);
			                if (in == null) {
			                	throw new FileNotFoundException("Failed to load httl config " + config + " in wepapp.");
			                }
			                properties.load(in);
			                engine = Engine.getEngine(config, properties);
			            } else {
			            	engine = Engine.getEngine(config);
			            }
			        } else {
			        	engine = Engine.getEngine();
			        }
			        outputStream = engine.getProperty(OUTPUT_STREAM, false);
				}
			}
		}
	}

	public static Template getTemplate(String path) throws IOException, ParseException {
		return engine == null ? null : engine.getTemplate(path);
	}

	public static Template getTemplate(String path, String encoding) throws IOException, ParseException {
		return engine == null ? null : engine.getTemplate(path, encoding);
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
		Template template = engine.getTemplate(path);
		if (outputStream) {
			template.render(parameters, ServletActionContext.getResponse().getOutputStream());
		} else {
			template.render(parameters, ServletActionContext.getResponse().getWriter());
		}
		response.flushBuffer();
	}

}
