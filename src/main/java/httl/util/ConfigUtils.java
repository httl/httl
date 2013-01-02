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
package httl.util;

import java.io.ByteArrayInputStream;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

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

    private static final String COMMA = ",";

	public static boolean isInteger(String value) {
	    return value != null && value.length() > 0 
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
		if (path == null || path.length() == 0) {
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
    	if (path == null || path.length() == 0) {
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

    public static Document getDocument(String dataSource) throws Exception {
        return getDocument(new ByteArrayInputStream(dataSource.getBytes()));
    }

    public static Document getDocument(File dataFile) throws Exception {
        return getDocument(new FileInputStream(dataFile));
    }

    public static Document getDocument(InputStream dataInputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(dataInputStream);
        return document;
    }

    public static Properties mergeProperties(Object... configs) {
    	List<Properties> list = new ArrayList<Properties>();
    	int last = configs.length - 1;
    	for (int i = 0; i <= last; i ++) {
    		Object config = configs[i];
    		if (config != null) {
    			Properties properties;
    			if (config instanceof String) {
    				boolean required = (i == last || configs[i + 1] == null);
    				properties = loadProperties((String) config, required);
    			} else {
    				properties = (Properties) config;
    			}
    			list.add(properties);
    		}
    	}
    	Properties result = new Properties();
    	for (Properties properties : list) {
    		Map<String, String> plusConfigs = new HashMap<String, String>();
    		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (key.endsWith(PLUS)) {
                	if (value != null && value.length() > 0) {
                		plusConfigs.put(key, value);
                    }
                } else {
                	result.setProperty(key, value);
                }
            }
    		for (Map.Entry<String, String> entry : plusConfigs.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                String k = key.substring(0, key.length() - PLUS.length());
                String v = result.getProperty(k);
                if (v != null && v.length() > 0) {
                	result.setProperty(k, v + COMMA + value);
                } else {
                	result.setProperty(k, value);
                }
    		}
    	}
    	for (Map.Entry<Object, Object> entry : new HashMap<Object, Object>(result).entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            while (value != null && value.length() > 0 && value.startsWith(REF)) {
            	String ref = value.substring(1);
            	value = result.getProperty(ref);
            	if (value == null) {
            		value = System.getProperty(ref);
            	}
            	if (value == null) {
                	result.remove(key);
            	} else {
            		result.put(key, value);
            	}
            }
    	}
    	return result;
    }

    private ConfigUtils() {}
    
}
