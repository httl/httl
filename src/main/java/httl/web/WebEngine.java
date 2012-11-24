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
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.spi.loaders.ServletLoader;
import httl.spi.resolvers.RequestResolver;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class WebEngine extends Engine {

    private static final String CONFIG_KEY = "httl.properties";

    private static final String WEBINF_CONFIG = "/WEB-INF/httl.properties";

    private static final String TEMPLATE_SUFFIX = "template.suffix";
    
    private static final WebEngine WEB_ENGINE = new WebEngine();
    
    private ServletContext servletContext;

	private Engine engine;

	private WebEngine() {}

	private void init() {
		setServletContext(ServletLoader.getServletContext());
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext) {
		if (servletContext == null) {
			return;
		}
		if (engine == null) {
			synchronized (WebEngine.class) {
				if (engine == null) {
					this.servletContext = servletContext;
					if (ServletLoader.getServletContext() == null) {
						ServletLoader.setServletContext(servletContext);
					}
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
			                engine = Engine.getEngine(config, properties);
			            } else {
			            	Properties properties = new Properties();
			        		addProperties(properties);
			            	engine = Engine.getEngine(config);
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
			        		engine = Engine.getEngine(WEBINF_CONFIG, properties);
			        	} else {
			        		Properties properties = new Properties();
			        		addProperties(properties);
			        		engine = Engine.getEngine(properties);
			        	}
			        }
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

	public static WebEngine getWebEngine() {
		WEB_ENGINE.init();
		return WEB_ENGINE;
	}

	public WebTemplate getWebTemplate(HttpServletRequest request) throws IOException, ParseException {
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
        	path = "/index";
        }
        if (engine == null) {
        	setServletContext(request.getSession().getServletContext());
        }
        String suffix = engine.getProperty(TEMPLATE_SUFFIX);
        if (suffix != null && suffix.length() > 0 && ! path.endsWith(suffix)) {
        	path += suffix;
        }
        return getWebTemplate(path);
	}

	public WebTemplate getWebTemplate(String path) throws IOException, ParseException {
		return getWebTemplate(path, null);
	}

	public WebTemplate getWebTemplate(String path, String encoding) throws IOException, ParseException {
		return new WebTemplate(engine.getTemplate(path, encoding));
	}

	@Override
	public Template getTemplate(String name, String encoding) throws IOException, ParseException {
		return getWebTemplate(name, encoding);
	}

	@Override
	public Expression getExpression(String source, Map<String, Class<?>> parameterTypes) throws ParseException {
		return engine.getExpression(source, parameterTypes);
	}

	@Override
	public Resource getResource(String name, String encoding) throws IOException {
		return engine.getResource(name, encoding);
	}

	@Override
	public void addResource(String name, String source) {
		engine.addResource(name, source);
	}

	@Override
	public void removeResource(String name) {
		engine.removeResource(name);
	}

	@Override
	public boolean hasResource(String name) {
		return engine.hasResource(name);
	}

}
