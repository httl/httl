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
package httl.web;

import httl.Engine;
import httl.internal.util.StringUtils;
import httl.spi.Logger;
import httl.spi.loaders.ServletLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

/**
 * WebEngine (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class WebEngine {

	private static final String CONFIG_KEY = "httl.properties";

	private static final String WEBINF_CONFIG = "/WEB-INF/httl.properties";

	private static volatile Engine ENGINE;

	private static final Map<String, String> addProperties = new ConcurrentHashMap<String, String>();

	private static final Map<String, String> setProperties = new ConcurrentHashMap<String, String>();

	static {
		addProperty("modes", "web");
	}

	private WebEngine() {}

	public static void addProperty(String key, String value) {
		String old = addProperties.get(key);
		if (StringUtils.isNotEmpty(old)) {
			value = old + "," + value;
		}
		addProperties.put(key, value);
	}

	public static void setProperty(String key, String value) {
		setProperties.put(key, value);
	}

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
					Properties properties = new Properties();
					String config = servletContext.getInitParameter(CONFIG_KEY);
					if (StringUtils.isEmpty(config)) {
						config = WEBINF_CONFIG;
					}
					if (config.startsWith("/")) {
						InputStream in = servletContext.getResourceAsStream(config);
						if (in != null) {
							try {
								properties.load(in);
							} catch (IOException e) {
								throw new IllegalStateException("Failed to load httl config " + config + " in wepapp. cause: " + e.getMessage(), e);
							}
						} else if (servletContext.getInitParameter(CONFIG_KEY) != null) { // 用户主动配置错误提醒
							throw new IllegalStateException("Not found httl config " + config + " in wepapp.");
						} else {
							config = null;
						}
					} else if (config.startsWith("classpath:")) {
						config = config.substring("classpath:".length());
					} else if (config.startsWith("file:")) {
						config = config.substring("file:".length());
					}
					ENGINE = Engine.getEngine(config, addProperties(properties));
					logConfigPath(ENGINE, servletContext, config);
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

	private static Properties addProperties(Properties properties) {
		Properties def = new Properties();
		InputStream in = Engine.class.getClassLoader().getResourceAsStream("httl-default.properties");
		if (in != null) {
			try {
				def.load(in);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to load httl-default.properties. cause: " + e.getMessage());
			}
		}
		for (Map.Entry<String, String> entry : addProperties.entrySet()) {
			String key = entry.getKey();
			if (! properties.containsKey(key)) {
				String values = def.getProperty(key, "");
				if (values.length() > 0) {
					values = "," + values;
				}
				properties.setProperty(key, entry.getValue() + values);
			}
		}
		for (Map.Entry<String, String> entry : setProperties.entrySet()) {
			String key = entry.getKey();
			if (! properties.containsKey(key)) {
				properties.setProperty(key, entry.getValue());
			}
		}
		return properties;
	}

	public static Engine getEngine() {
		if (ENGINE == null) {
			setServletContext(ServletLoader.getAndCheckServletContext());
		}
		return ENGINE;
	}

}