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
import httl.spi.resolvers.RequestResolver;
import httl.util.WrappedMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebEngine {

    private static final String CONFIG_KEY = "httl.properties";

    private static final String WEBINF_CONFIG = "/WEB-INF/httl.properties";

    private static final String TEMPLATE_SUFFIX = "template.suffix";

    private static final String OUTPUT_STREAM_KEY = "output.stream";

	private static Engine ENGINE;

    private static boolean OUTPUT_STREAM;

	private WebEngine() {}

	public static void init(ServletContext servletContext) {
		if (servletContext == null) {
			return;
		}
		if (ServletLoader.getServletContext() == null) {
			ServletLoader.setServletContext(servletContext);
		}
		if (ENGINE == null) {
			synchronized (WebEngine.class) {
				if (ENGINE == null) { // double check
					String config = servletContext.getInitParameter(CONFIG_KEY);
			        if (config != null && config.length() > 0) {
			            if (config.startsWith("/")) {
			                Properties properties = new Properties();
			                InputStream in = servletContext.getResourceAsStream(config);
			                if (in == null) {
			                	throw new IllegalStateException("Not found httl config " + config + " in wepapp.");
			                }
			                try {
								properties.load(in);
							} catch (IOException e) {
								throw new IllegalStateException("Failed to load httl config " + config + " in wepapp.");
							}
			                addProperties(properties);
			                ENGINE = Engine.getEngine(config, properties);
			            } else {
			            	Properties properties = new Properties();
			        		addProperties(properties);
			            	ENGINE = Engine.getEngine(config, properties);
			            }
			        } else {
			        	InputStream in = servletContext.getResourceAsStream(WEBINF_CONFIG);
			        	if (in != null) {
			        		Properties properties = new Properties();
			        		try {
			        			properties.load(in);
			        		} catch (IOException e) {
								throw new IllegalStateException("Failed to load httl config " + config + " in wepapp.");
							}
			        		addProperties(properties);
			        		ENGINE = Engine.getEngine(WEBINF_CONFIG, properties);
			        	} else {
			        		Properties properties = new Properties();
			        		addProperties(properties);
			        		ENGINE = Engine.getEngine(properties);
			        	}
			        }
			        OUTPUT_STREAM = ENGINE.getProperty(OUTPUT_STREAM_KEY, false);
				}
			}
		}
	}

	private static void addProperties(Properties properties) {
		if (! properties.containsKey("loader") 
        		&& ! properties.containsKey("loaders")
        		&& ! properties.containsKey("loaders+")) {
        	properties.setProperty("loaders+", ServletLoader.class.getName());
        }
        if (! properties.containsKey("resolver") 
        		&& ! properties.containsKey("resolvers")
        		&& ! properties.containsKey("resolvers+")) {
        	properties.setProperty("resolvers+", RequestResolver.class.getName());
        }
	}

	public static Engine getEngine() {
		if (ENGINE == null) {
			init(ServletLoader.getAndCheckServletContext());
		}
		return ENGINE;
	}

	public static void render(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
		doRender(request, response, null, null, null);
	}

	public static void render(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) throws IOException, ParseException {
		doRender(request, response, null, model, null);
	}

	public static void render(HttpServletRequest request, HttpServletResponse response, String path) throws IOException, ParseException {
		doRender(request, response, path, null, null);
	}
	
	public static void render(HttpServletRequest request, HttpServletResponse response, String path, Map<String, Object> model) throws IOException, ParseException {
		doRender(request, response, path, model, null);
	}
	
	public static void render(HttpServletRequest request, HttpServletResponse response, String path, Map<String, Object> model, OutputStream output) throws IOException, ParseException {
		doRender(request, response, path, model, output);
	}
	
	public static void render(HttpServletRequest request, HttpServletResponse response, String path, Map<String, Object> model, Writer writer) throws IOException, ParseException {
		doRender(request, response, path, model, writer);
	}

	private static void doRender(HttpServletRequest request, HttpServletResponse response, String path, Map<String, Object> model, Object output) throws IOException, ParseException {
		if (ENGINE == null) {
			init(request.getSession().getServletContext());
		}
		boolean unresolved = RequestResolver.getRequest() == null;
		if (unresolved) {
			RequestResolver.setRequest(request);
		}
		Map<String, Object> parameters = RequestResolver.getAndCheckPrarameters();
		if (model != null) {
			parameters = new WrappedMap<String, Object>(parameters, model);
		}
		if (path == null || path.length() == 0) {
			path = request.getPathInfo();
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
	        	path = "/index";
	        }
	        String suffix = ENGINE.getProperty(TEMPLATE_SUFFIX);
	        if (suffix != null && suffix.length() > 0 && ! path.endsWith(suffix)) {
	        	path += suffix;
	        }
		}
		try {
			Template template = ENGINE.getTemplate(path);
			if (output == null) {
				if (OUTPUT_STREAM) {
					template.render(parameters, response.getOutputStream());
				} else {
					template.render(parameters, response.getWriter());
				}
			} else {
				if (output instanceof OutputStream) {
					template.render(parameters, (OutputStream) output);
				} else {
					template.render(parameters, (Writer) output);
				}
			}
			response.flushBuffer();
		} catch (FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} finally {
			if (unresolved) {
				RequestResolver.removeRequest();
			}
		}
	}

}
