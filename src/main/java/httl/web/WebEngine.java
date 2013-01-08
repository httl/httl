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
package httl.web;

import httl.Engine;
import httl.Template;
import httl.spi.Logger;
import httl.spi.loaders.ServletLoader;
import httl.spi.resolvers.ServletResolver;
import httl.util.StringUtils;

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
import javax.servlet.http.HttpSession;

/**
 * WebEngine (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class WebEngine {

	private static final String DEFAULT_CONTENT_TYPE = "text/html";

	private static final String CHARSET_SEPARATOR = "; ";

	private static final String CHARSET_KEY = "charset=";

	private static final String CONFIG_KEY = "httl.properties";

	private static final String WEBINF_CONFIG = "/WEB-INF/httl.properties";

	private static final String OUTPUT_ENCODING_KEY = "output.encoding";

	private static final String OUTPUT_STREAM_KEY = "output.stream";
	
	private static final String LOCALIZED_KEY = "localized";

	private static volatile Engine ENGINE;

	private static String OUTPUT_ENCODING;

	private static boolean OUTPUT_STREAM;

	private static boolean LOCALIZED;

	private WebEngine() {}

	public static ServletContext getServletContext() {
		return ServletLoader.getServletContext();
	}

	public static void setServletContext(ServletContext servletContext) {
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
								throw new IllegalStateException("Failed to load httl config " + config + " in wepapp. cause: " + e.getMessage(), e);
							}
							addProperties(properties);
							ENGINE = Engine.getEngine(config, properties);
						} else {
							Properties properties = new Properties();
							addProperties(properties);
							ENGINE = Engine.getEngine(config, properties);
						}
						logConfigPath(ENGINE, servletContext, config);
					} else {
						InputStream in = servletContext.getResourceAsStream(WEBINF_CONFIG);
						if (in != null) {
							Properties properties = new Properties();
							try {
								properties.load(in);
							} catch (IOException e) {
								throw new IllegalStateException("Failed to load httl config " + config + " in wepapp. cause: " + e.getMessage(), e);
							}
							addProperties(properties);
							ENGINE = Engine.getEngine(WEBINF_CONFIG, properties);
							logConfigPath(ENGINE, servletContext, WEBINF_CONFIG);
						} else {
							Properties properties = new Properties();
							addProperties(properties);
							ENGINE = Engine.getEngine(properties);
						}
					}
					OUTPUT_ENCODING = ENGINE.getProperty(OUTPUT_ENCODING_KEY, String.class);
					OUTPUT_STREAM = ENGINE.getProperty(OUTPUT_STREAM_KEY, false);
					LOCALIZED = ENGINE.getProperty(LOCALIZED_KEY, false);
				}
			}
		}
	}
	
	private static void logConfigPath(Engine engine, ServletContext servletContext, String path) {
		if (engine != null && servletContext != null && path != null) {
			Logger logger = engine.getProperty("logger", Logger.class);
			if (logger != null && logger.isInfoEnabled()) {
				String name = engine.getName();
				try {
					if (name != null && name.startsWith("/")
							&& servletContext.getResource(name) != null) {
						logger.info("Load httl config form " + servletContext.getRealPath(name) + " in webapp.");
					}
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private static void addProperties(Properties properties) {
		Properties def = new Properties();
		InputStream in = Engine.class.getClassLoader().getResourceAsStream("httl-default.properties");
		if (in != null) {
			try {
				def.load(in);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to load httl-default.properties. cause: " + e.getMessage());
			}
		}
		if (! properties.containsKey("loader") 
				&& ! properties.containsKey("loaders")) {
			String loaders = def.getProperty("loaders", "");
			if (loaders.length() > 0) {
				loaders = loaders + ",";
			}
			properties.setProperty("loaders", loaders + ServletLoader.class.getName());
		}
		if (! properties.containsKey("resolver") 
				&& ! properties.containsKey("resolvers")) {
			String resolvers = def.getProperty("resolvers", "");
			if (resolvers.length() > 0) {
				resolvers = resolvers + ",";
			}
			properties.setProperty("resolvers", resolvers + ServletResolver.class.getName());
		}
		if (! properties.containsKey("import.variables")) {
			String variables = def.getProperty("import.variables", "");
			if (variables.length() > 0) {
				variables = variables + ",";
			}
			properties.setProperty("import.variables",  variables 
					+ HttpServletRequest.class.getName() + " request,"
					+ HttpServletResponse.class.getName() + " response,"
					+ HttpSession.class.getName() + " session,"
					+ ServletContext.class.getName() + " application");
		}
	}

	public static Engine getEngine() {
		if (ENGINE == null) {
			setServletContext(ServletLoader.getAndCheckServletContext());
		}
		return ENGINE;
	}

	public static void render(HttpServletRequest request, HttpServletResponse response, String path) throws IOException, ParseException {
		render(request, response, path, null);
	}

	public static void render(HttpServletRequest request, HttpServletResponse response, String path, Map<String, Object> parameters) throws IOException, ParseException {
		render(request, response, path, parameters, OUTPUT_STREAM ? response.getOutputStream() : response.getWriter());
	}

	public static void render(HttpServletRequest request, HttpServletResponse response, String path, Map<String, Object> parameters, Object out) throws IOException, ParseException {
		ServletResolver.set(request, response);
		try {
			setResponseEncoding(response);
			Template template = LOCALIZED ? getEngine().getTemplate(path, request.getLocale()) : getEngine().getTemplate(path);
			if (out instanceof OutputStream) {
				template.render(parameters, (OutputStream) out);
			} else {
				template.render(parameters, (Writer) out);
			}
		} finally {
			ServletResolver.remove();
		}
	}
	
	public static void setResponseEncoding(HttpServletResponse response) {
		getEngine();
		if (StringUtils.isNotEmpty(OUTPUT_ENCODING)) {
			response.setCharacterEncoding(OUTPUT_ENCODING);
			String contentType = response.getContentType();
			if (StringUtils.isEmpty(contentType)) {
				response.setContentType(DEFAULT_CONTENT_TYPE + CHARSET_SEPARATOR + CHARSET_KEY + OUTPUT_ENCODING);
			} else {
				int i = contentType.indexOf(CHARSET_KEY);
				if (i > 0) {
					response.setContentType(contentType.substring(0, i + CHARSET_KEY.length()) + OUTPUT_ENCODING);
				} else {
					response.setContentType(contentType + CHARSET_SEPARATOR + CHARSET_KEY + OUTPUT_ENCODING);
				}
			}
		}
	}

}