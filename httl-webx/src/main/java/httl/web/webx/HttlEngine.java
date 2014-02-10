/*
 * Copyright 2011-2013 HTTL Team. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package httl.web.webx;

import httl.web.WebEngine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.citrus.service.template.TemplateContext;
import com.alibaba.citrus.service.template.TemplateEngine;
import com.alibaba.citrus.service.template.TemplateException;
import com.alibaba.citrus.turbine.TurbineRunData;
import com.alibaba.citrus.util.StringUtil;

/**
 * HttlEngine. (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlEngine implements TemplateEngine {

    private String path;

    private String templateSuffix = ".httl";

    private String templateEncoding;

    public void setPath(String path) {
        this.path = path;
    }

    public void setTemplateEncoding(String templateEncoding) {
        this.templateEncoding = templateEncoding;
    }

    public void setAdvancedProperties(Map<String, String> configuration) {
        if (configuration.containsKey("template.suffix")) {
            this.templateSuffix = configuration.get("template.suffix");
        }
        for (Map.Entry<String, String> entry : configuration.entrySet()) {
            WebEngine.setProperty(entry.getKey(), entry.getValue());
        }
    }

    public String[] getDefaultExtensions() {
        return new String[] { StringUtil.substringAfter(getTemplateSuffix(), ".") };
    }

    private String getTemplatePath(String name) {
        if (path == null) {
            return name;
        } else {
            return path + name;
        }
    }

    public boolean exists(String templateName) {
        if (WebEngine.getServletContext() == null) {
            return templateName.endsWith(getTemplateSuffix());
        }
        return WebEngine.getEngine().hasResource(getTemplatePath(templateName));
    }

    private String getTemplateSuffix() {
        return this.templateSuffix;
    }

    public String getText(String templateName, TemplateContext context) throws TemplateException, IOException {
        StringWriter writer = new StringWriter();
        writeTo(templateName, context, writer);
        return writer.toString();
    }

    public void writeTo(String templateName, TemplateContext templateContext, OutputStream ostream) throws TemplateException, IOException {
        doWriteTo(templateName, templateContext, ostream);
    }

    public void writeTo(String templateName, TemplateContext templateContext, Writer writer) throws TemplateException, IOException {
        doWriteTo(templateName, templateContext, writer);
    }

    private void doWriteTo(String templateName, TemplateContext templateContext, Object out) throws TemplateException, IOException {
        try {
            String path = getTemplatePath(templateName);
            ContextMap map = new ContextMap(templateContext);
            TurbineRunData rundata = (TurbineRunData) templateContext.get("rundata");
            if (rundata != null) {
                HttpServletRequest request = rundata.getRequest();
                HttpServletResponse response = rundata.getResponse();
                if (rundata.getRequest() != null && rundata.getResponse() != null) {
                    WebEngine.setRequestAndResponse(request, response);
                    WebEngine.getEngine().getTemplate(path, request.getLocale(), templateEncoding, map).render(map, out);
                    return;
                }
            }
            WebEngine.getEngine().getTemplate(path, templateEncoding).render(map, out);
        } catch (ParseException e) {
            throw new TemplateException(e.getMessage(), e);
        }
    }

}
