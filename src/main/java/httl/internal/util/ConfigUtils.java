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
package httl.internal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * ConfigUtils. (Tool, Static, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class ConfigUtils {
	
	// Windows file path prefix pattern
	private static final Pattern WINDOWS_FILE_PATTERN = Pattern.compile("^[A-Za-z]+:");
	
	private static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");

	private static final String REF = "$";

	private static final String PLUS = "+";

	private static final String MINUS = "-";

	private static final String COMMA = ",";

	public static boolean isInteger(String value) {
		return StringUtils.isNotEmpty(value) 
				&& INTEGER_PATTERN.matcher(value).matches();
	}

	public static Properties loadProperties(String path) {
		return loadProperties(path, false);
	}

	/**
	 * Load properties file
	 * 
	 * @param path - File path
	 * @return Properties map
	 */
	public static Properties loadProperties(String path, boolean required) {
		Properties properties = new Properties();
		return loadProperties(properties, path, required);
	}
	
	public static Properties loadProperties(Properties properties, String path, boolean required) {
		if (StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException("path == null");
		}
		try {
			InputStream in = null;
			try {
				if (path.startsWith("/") || path.startsWith("./") || path.startsWith("../") 
						|| WINDOWS_FILE_PATTERN.matcher(path).matches()) {
					File file = new File(path);
					if (file.exists()) {
						in = new FileInputStream(file);
					}
				} else {
					in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
				}
				if (in != null) {
					properties.load(in);
				} else if (required) {
					throw new FileNotFoundException("Not found httl config file " + path);
				}
				return properties;
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			if (required) {
				throw new IllegalStateException(e.getMessage(), e);
			} else {
				return properties;
			}
		}
	}
	
	public static boolean isFilePath(String path) {
		return path != null && (path.startsWith("/") || path.startsWith("./") || path.startsWith("../") 
				|| WINDOWS_FILE_PATTERN.matcher(path).matches());
	}
	
	public static String getRealPath(String path) {
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		if (isFilePath(path)) {
			File file = new File(path);
			if (file.exists()) {
				return file.getAbsolutePath();
			}
		} else {
			URL url = Thread.currentThread().getContextClassLoader().getResource(path);
			if (url != null) {
				return url.getFile();
			}
		}
		return null;
	}

	public static Map<String, String> filterWithPrefix(String prefix, Map<String, String> input, boolean escape) {
		Map<String, String> ret = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : input.entrySet()) {
			String key = entry.getKey();
			if (escape) {
				key = key.replace('_', '.');
			}
			if(key.startsWith(prefix)) {
				key = key.substring(prefix.length());
				String value = entry.getValue();
				if (escape) {
					if (value.startsWith(PLUS)) {
						key += PLUS;
						value = value.substring(PLUS.length());
					} else if (value.startsWith(MINUS)) {
						key += MINUS;
						value = value.substring(MINUS.length());
					}
				}
				ret.put(key, value);
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static Properties mergeProperties(Object... configs) {
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		int last = configs.length - 1;
		for (int i = 0; i <= last; i ++) {
			Object config = configs[i];
			if (config != null) {
				Map<Object, Object> properties;
				if (config instanceof String) {
					boolean required = (i == last || configs[i + 1] == null);
					properties = loadProperties((String) config, required);
				} else {
					properties = (Map<Object, Object>) config;
				}
				list.add(properties);
			}
		}
		Properties result = new Properties();
		for (Map<Object, Object> properties : list) {
			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				if (key.endsWith(MINUS)) {
					if (StringUtils.isNotEmpty(value)) {
						String k = key.substring(0, key.length() - MINUS.length());
						String v = result.getProperty(k);
						if (StringUtils.isNotEmpty(v) && v.contains(value)) {
							result.setProperty(k, StringUtils.removeCommaValue(v, value));
						}
					}
				} else if (key.endsWith(PLUS)) {
					if (StringUtils.isNotEmpty(value)) {
						String k = key.substring(0, key.length() - PLUS.length());
						String v = result.getProperty(k);
						if (StringUtils.isNotEmpty(v)) {
							result.setProperty(k, value + COMMA + v);
						} else {
							result.setProperty(k, value);
						}
					}
				} else {
					result.setProperty(key, value);
				}
			}
		}
		for (Map.Entry<Object, Object> entry : new HashMap<Object, Object>(result).entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (StringUtils.isNotEmpty(value) && value.contains(REF)) {
				if (value.contains(COMMA)) {
					String[] values = StringUtils.splitByComma(value);
					StringBuilder buf = new StringBuilder(value.length());
					for (String v : values) {
						if (buf.length() > 0) {
							buf.append(COMMA);
						}
						buf.append(getRefValue(result, v));
					}
					result.put(key, buf.toString());
				} else {
					result.put(key, getRefValue(result, value));
				}
			}
		}
		return result;
	}
	
	private static String getRefValue(Properties result, String v) {
		if (v != null) {
			while (v.startsWith(REF)) {
				String ref = v.substring(1);
				v = result.getProperty(ref);
				if (v == null) {
					v = System.getProperty(ref);
				}
			}
		}
		return v == null ? "" : v;
	}

	private ConfigUtils() {}
	
}