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

import httl.Context;
import httl.web.WebEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * HttlResult. (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlResult extends StrutsResultSupport {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doExecute(String location, ActionInvocation invocation)
			throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		ValueStack stack = ActionContext.getContext().getValueStack();
		ValueStackMap map = new ValueStackMap(stack);
		Context context = Context.getContext();
		context.put("request", request);
		context.put("response", response);
		WebEngine.setServletContext(ServletActionContext.getServletContext());
		WebEngine.getEngine().getTemplate(location, request.getLocale()).render(map, response);
	}

}