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
package httl.web.struts;

import httl.web.WebEngine;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.components.template.BaseTemplateEngine;
import org.apache.struts2.components.template.Template;
import org.apache.struts2.components.template.TemplateRenderingContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * HttlTemplateEngine. (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlTemplateEngine extends BaseTemplateEngine {

	public void renderTemplate(TemplateRenderingContext templateContext) throws Exception {
		// get the various items required from the stack
		Map<String, Object> actionContext = templateContext.getStack().getContext();
		ServletContext servletContext = (ServletContext) actionContext.get(ServletActionContext.SERVLET_CONTEXT);

		// prepare httl
		WebEngine.setServletContext(servletContext);

		// get the list of templates we can use
		List<Template> templates = templateContext.getTemplate().getPossibleTemplates(this);

		// find the right template
		httl.Template template = null;
		String templateName = null;
		Exception exception = null;
		for (Template t : templates) {
			templateName = getFinalTemplateName(t);
			try {
				// try to load, and if it works, stop at the first one
				if (WebEngine.getEngine().hasResource(templateName)) {
					template = WebEngine.getEngine().getTemplate(templateName);
					break;
				}
			} catch (IOException e) {
				if (exception == null) {
					exception = e;
				}
			}
		}

		if (template == null) {
			if (exception != null) {
				throw exception;
			} else {
				return;
			}
		}

		ValueStack stack = ActionContext.getContext().getValueStack();
		ValueStackMap map = new ValueStackMap(stack);

		Writer outputWriter = templateContext.getWriter();

		template.render(map, outputWriter);
	}

	protected String getSuffix() {
		return "httl";
	}
}