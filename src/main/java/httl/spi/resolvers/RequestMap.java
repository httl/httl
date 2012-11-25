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
package httl.spi.resolvers;

import httl.util.ClassUtils;
import httl.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * RequestMap. (Integration, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class RequestMap implements Map<String, Object> {
    
    private static final String CONTEXT_PREFIX = "context.";

    private static final String PARAMETER_PREFIX = "parameter.";

    private static final String HEADER_PREFIX = "header.";
    
    private static final String REQUEST_PREFIX = "request.";
    
    private static final String SESSION_PREFIX = "session.";
    
    private static final String COOKIE_PREFIX = "cookie.";
    
    private static final String APPLICATION_PREFIX = "application.";

    private final HttpServletRequest request;
    
	private final Map<String, Object> context;
    
    public RequestMap(HttpServletRequest request){
        this(request, null);
    }
    
    public RequestMap(HttpServletRequest request, Map<String, Object> context){
        this.request = request;
        this.context = context == null ? new HashMap<String, Object>() : context;
    }

    public HttpServletRequest getRequest() {
		return request;
	}

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }
        Set<String> keys = context.keySet();
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                if (value.equals(context.get(key))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsKey(Object key) {
        String k = (String) key;
        if (k == null || k.length() == 0) {
            return false;
        }
        return context.containsKey(k) || request.getParameter(k) != null;
    }

    public Object get(Object key) {
        String k = (String) key;
        if (k == null || k.length() == 0) {
            return null;
        }
        if (k.startsWith(CONTEXT_PREFIX)) {
            return context.get(k.substring(CONTEXT_PREFIX.length()));
        } else if (k.startsWith(REQUEST_PREFIX)) {
        	String property = k.substring(REQUEST_PREFIX.length());
        	Object value = ClassUtils.getProperty(request, property);
        	if (value != null) {
        		return value;
        	}
            return request.getAttribute(property);
        } else if (k.startsWith(PARAMETER_PREFIX)) {
            return getParameterValue(k.substring(PARAMETER_PREFIX.length()));
        } else if (k.startsWith(HEADER_PREFIX)) {
        	String property = k.substring(HEADER_PREFIX.length());
        	String value = request.getHeader(property);
        	if (value != null) {
        		return value;
        	}
            return request.getHeader(StringUtils.splitCamelName(property, "-", true));
        } else if (k.startsWith(SESSION_PREFIX)) {
        	String property = k.substring(SESSION_PREFIX.length());
        	Object value = ClassUtils.getProperty(request.getSession(), property);
        	if (value != null) {
        		return value;
        	}
            return request.getSession().getAttribute(property);
        } else if (k.startsWith(COOKIE_PREFIX)) {
            return getCookieValue(k.substring(COOKIE_PREFIX.length()));
        } else if (k.startsWith(APPLICATION_PREFIX)) {
        	String property = k.substring(APPLICATION_PREFIX.length());
        	Object value = ClassUtils.getProperty(request.getSession().getServletContext(), property);
        	if (value != null) {
        		return value;
        	}
            return request.getSession().getServletContext().getAttribute(property);
        } else {
			Object value = context.get(k);
			if (value != null) {
        		return value;
        	}
			value = ClassUtils.getProperty(request, k);
			if (value != null) {
        		return value;
        	}
			value = request.getAttribute(k);
			if (value != null) {
        		return value;
        	}
			value = getParameterValue(k);
			if (value != null) {
        		return value;
        	}
			value = request.getHeader(k);
			if (value != null) {
        		return value;
        	}
			value = request.getHeader(StringUtils.splitCamelName(k, "-", true));
			if (value != null) {
        		return value;
        	}
			value = ClassUtils.getProperty(request.getSession(), k);
			if (value != null) {
        		return value;
        	}
			value = request.getSession().getAttribute(k);
			if (value != null) {
        		return value;
        	}
			value = getCookieValue(k);
			if (value != null) {
        		return value;
        	}
			value = ClassUtils.getProperty(request.getSession().getServletContext(), k);
			if (value != null) {
        		return value;
        	}
			value = request.getSession().getServletContext().getAttribute(k);
			if (value != null) {
        		return value;
        	}
            return value;
        }
    }
    
    private Object getParameterValue(String key) {
        String[] values = request.getParameterValues(key);
        if (values == null || values.length == 0) {
            return null;
        } else if (values.length == 1) {
            return values[0];
        } else  {
            return values;
        }
    }
    
    private Object getCookieValue(String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (key.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Object put(String key, Object value) {
        if (key == null || key.length() == 0) {
            return null;
        }
        if (key.startsWith(REQUEST_PREFIX)) {
            key = key.substring(APPLICATION_PREFIX.length());
            Object old = request.getAttribute(key);
            if (value == null) {
                request.removeAttribute(key);
            } else {
                request.setAttribute(key, value);
            }
            return old;
        } else if (key.startsWith(SESSION_PREFIX)) {
            key = key.substring(APPLICATION_PREFIX.length());
            Object old = request.getSession().getAttribute(key);
            if (value == null) {
                request.getSession().removeAttribute(key);
            } else {
                request.getSession().setAttribute(key, value);
            }
            return old;
        } else if (key.startsWith(COOKIE_PREFIX)) {
            key = key.substring(APPLICATION_PREFIX.length());
            Object old = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (key.equals(cookie.getName())) {
                        old = cookie.getValue();
                        if (value == null) {
                            cookie.setMaxAge(-1);
                        } else {
                            cookie.setValue((String) value);
                        }
                        break;
                    }
                }
            }
            return old;
        } else if (key.startsWith(APPLICATION_PREFIX)) {
            key = key.substring(APPLICATION_PREFIX.length());
            Object old = request.getSession().getServletContext().getAttribute(key);
            if (value == null) {
                request.getSession().getServletContext().removeAttribute(key);
            } else {
                request.getSession().getServletContext().setAttribute(key, value);
            }
            return old;
        } else {
            if (key.startsWith(APPLICATION_PREFIX)) {
                key = key.substring(APPLICATION_PREFIX.length());
            }
            Object old = context.get(key);
            if (value == null) {
                context.remove(key);
            } else {
                context.put(key, value);
            }
            return old;
        }
    }

    public Object remove(Object key) {
        return put((String) key, null);
    }

    public void putAll(Map<? extends String, ? extends Object> map) {
        if (map != null && map.size() > 0) {
            for (Map.Entry<? extends String, ? extends Object> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void clear() {
        Set<String> keys = context.keySet();
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                context.remove(key);
            }
        }
    }

    public Set<String> keySet() {
        return context.keySet();
    }

    public Collection<Object> values() {
        Set<String> keys = context.keySet();
        Set<Object> values = new HashSet<Object>();
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                values.add(context.get(key));
            }
        }
        return values;
    }

    public Set<Entry<String, Object>> entrySet() {
        Set<String> keys = context.keySet();
        Set<Entry<String, Object>> entries = new HashSet<Entry<String, Object>>();
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                entries.add(new ParameterEntry(key));
            }
        }
        return entries;
    }
    
    private class ParameterEntry implements Entry<String, Object> {

        private final String key;
        
        private volatile Object value;

        public ParameterEntry(String key){
            this.key = key;
            this.value = RequestMap.this.get(key);
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            this.value = value;
            return RequestMap.this.put(key, value);
        }

    }

}
