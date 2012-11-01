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
package httl.web.webx;

import httl.web.WebEngine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;

import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateEngine;
import com.alibaba.citrus.service.template.TemplateException;

/**
 * HttlTemplateEngine. (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlTemplateEngine implements TemplateEngine {

	public String[] getDefaultExtensions() {
		return new String[] { "httl" };
	}

	public boolean exists(String templateName) {
		try {
			return WebEngine.getTemplate(templateName) != null;
		} catch (Exception e) {
			return true;
		}
	}

	public String getText(String templateName, TemplateContext context)
			throws TemplateException, IOException {
		try {
			return WebEngine.getTemplate(templateName).render(new TemplateContextMap(context));
		} catch (ParseException e) {
			throw new TemplateException(e.getMessage(), e);
		}
	}

	public void writeTo(String templateName, TemplateContext context,
			OutputStream ostream) throws TemplateException, IOException {
		try {
			WebEngine.getTemplate(templateName).render(new TemplateContextMap(context), ostream);
		} catch (ParseException e) {
			throw new TemplateException(e.getMessage(), e);
		}
	}

	public void writeTo(String templateName, TemplateContext context,
			Writer writer) throws TemplateException, IOException {
		try {
			WebEngine.getTemplate(templateName).render(new TemplateContextMap(context), writer);
		} catch (ParseException e) {
			throw new TemplateException(e.getMessage(), e);
		}
	}

}
