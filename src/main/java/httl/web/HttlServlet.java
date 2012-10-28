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
import httl.Template;
import httl.spi.loaders.ServletLoader;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttlServlet. (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    private static final String CONFIGURATION = "configuration";

    private static final String OUTPUT_STREAM = "output.stream";

    private transient Engine engine;
    
    private boolean isOutputStream;
    
    @Override
    public void init() throws ServletException {
        ServletLoader.setServletContext(getServletContext());
        String config = getServletConfig().getInitParameter(CONFIGURATION);
        if (config != null && config.length() > 0) {
            if (config.startsWith("/")) {
                Properties properties = new Properties();
                try {
                    properties.load(getServletContext().getResourceAsStream(config));
                } catch (IOException e) {
                    throw new ServletException("Failed to load httl config: " + config + ", cause: " + e.getMessage(), e);
                }
                this.engine = Engine.getEngine(config, properties);
            } else {
                this.engine = Engine.getEngine(config);
            }
        } else {
            this.engine = Engine.getEngine();
        }
        isOutputStream = "true".equalsIgnoreCase(engine.getConfig(OUTPUT_STREAM));
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Template template = engine.getTemplate(getTemplatePath(request));
            ParameterMap context = new ParameterMap(request);
            if (isOutputStream) {
                template.render(context, response.getOutputStream());
            } else {
                template.render(context, response.getWriter());
            }
            response.flushBuffer();
        } catch (ParseException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    protected String getTemplatePath(HttpServletRequest request)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path != null && path.length() > 0)
            return path;
        return request.getServletPath();
    }

}
