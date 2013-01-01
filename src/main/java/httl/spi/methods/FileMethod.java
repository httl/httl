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
package httl.spi.methods;

import httl.Context;
import httl.Engine;
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.util.Digest;
import httl.util.IOUtils;
import httl.util.UrlUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

/**
 * FileMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class FileMethod {

    private Engine engine;

    /**
     * httl.properties: engine=httl.spi.engines.DefaultEngine
     */
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
    
    public Template extend(String name) throws IOException, ParseException {
    	return extend(name, (Locale) null, (String) null);
    }

    public Template extend(String name, String encoding) throws IOException, ParseException {
    	return extend(name, (Locale) null, encoding);
    }

    public Template extend(String name, Locale locale) throws IOException, ParseException {
    	return extend(name, locale, (String) null);
    }

    public Template extend(String name, Locale locale, String encoding) throws IOException, ParseException {
    	Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("extend context template == null, extend: " + name);
        }
        Template extend = engine.getTemplate(name);
        if (extend == template) {
        	throw new IllegalStateException("The template " + template.getName() + " can not be recursive extending the self template.");
        }
        Context.getContext().putAll(template.getMacros());
        return extend;
    }

    public Template extend(String name, Map<String, Object> parameters) throws IOException, ParseException {
    	return extend(name, null, null, parameters);
    }

    public Template extend(String name, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
    	return extend(name, null, encoding, parameters);
    }

    public Template extend(String name, Locale locale, Map<String, Object> parameters) throws IOException, ParseException {
    	return extend(name, locale, null, parameters);
    }

    public Template extend(String name, Locale locale, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
        if (parameters != null) {
        	Context.getContext().putAll(parameters);
        }
        return extend(name, locale, encoding);
    }

    public Expression evaluate(byte[] source) throws IOException, ParseException {
    	Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("display context template == null");
        }
        String encoding = template.getEncoding();
    	return evaluate(encoding == null ? new String(source) : new String(source, encoding));
    }

    public Expression evaluate(String expr) throws ParseException {
    	Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("display context template == null");
        }
    	return engine.getExpression(expr, template.getParameterTypes());
    }

    public Template render(Resource resource) throws IOException, ParseException {
    	return render(IOUtils.readToString(resource.getReader()));
    }

    public Template render(byte[] source) throws IOException, ParseException {
    	Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("display context template == null");
        }
        String encoding = template.getEncoding();
    	return render(encoding == null ? new String(source) : new String(source, encoding));
    }

    public Template render(String source) throws IOException, ParseException {
        Template template = Context.getContext().getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("display context template == null");
        }
        String name = template.getName() + "$" + Digest.getMD5(source);
        if (! engine.hasResource(name)) {
        	engine.addResource(name, source);
        }
        return engine.getTemplate(name);
    }

    public Template include(String name) throws IOException, ParseException {
        return include(name, (Locale) null, (String) null);
    }
    
    public Template include(String name, String encoding) throws IOException, ParseException {
    	return include(name, (Locale) null, encoding);
    }

    public Template include(String name, Locale locale) throws IOException, ParseException {
    	return include(name, locale, (String) null);
    }

    public Template include(String name, Locale locale, String encoding) throws IOException, ParseException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("include template name == null");
        }
        String macro = null;
		int i = name.indexOf('#');
        if (i > 0) {
        	macro = name.substring(i + 1);
        	name = name.substring(0, i);
        }
        Template template = Context.getContext().getTemplate();
        if (template != null) {
            if (encoding == null || encoding.length() == 0) {
                encoding = template.getEncoding();
            }
            name = UrlUtils.relativeUrl(name, template.getName());
            if (locale == null) {
            	locale = template.getLocale();
            }
        }
        Template include = engine.getTemplate(name, locale, encoding);
        if (macro != null && macro.length() > 0) {
        	include = include.getMacros().get(macro);
		}
        if (include == template) {
        	throw new IllegalStateException("The template " + template.getName() + " can not be recursive including the self template.");
        }
        return include;
    }

    public Template include(String name, Map<String, Object> parameters) throws IOException, ParseException {
    	return include(name, null, null, parameters);
    }
    
    public Template include(String name, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
    	return include(name, null, encoding, parameters);
    }
    
    public Template include(String name, Locale locale, Map<String, Object> parameters) throws IOException, ParseException {
    	return include(name, locale, null, parameters);
    }
    
    public Template include(String name, Locale locale, String encoding, Map<String, Object> parameters) throws IOException, ParseException {
    	if (parameters != null) {
    		Context.getContext().putAll(parameters);
    	}
    	return include(name, locale, encoding);
    }

    public Resource read(String name) throws IOException, ParseException {
        return read(name, null, null);
    }

    public Resource read(String name, String encoding) throws IOException {
    	return read(name, null, encoding);
    }

    public Resource read(String name, Locale locale) throws IOException {
    	return read(name, locale, null);
    }

    public Resource read(String name, Locale locale, String encoding) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("display template name == null");
        }
        Template template = Context.getContext().getTemplate();
        if (template != null) {
            if (encoding == null || encoding.length() == 0) {
                encoding = template.getEncoding();
            }
            name = UrlUtils.relativeUrl(name, template.getName());
            if (locale == null) {
            	locale = template.getLocale();
            }
        }
        return engine.getResource(name, locale, encoding);
    }

}
