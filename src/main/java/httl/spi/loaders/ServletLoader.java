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
package httl.spi.loaders;

import httl.Resource;
import httl.spi.Loader;
import httl.spi.loaders.AbstractLoader;
import httl.util.UrlUtils;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * ServletLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ServletLoader extends AbstractLoader implements ServletContextListener {
    
    private static ServletContext SERVLET_CONTEXT;
    
    private ServletContext servletContext;
    
    public ServletLoader() {
    }
    
    public ServletLoader(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();
    }
    
    public void contextDestroyed(ServletContextEvent sce) {
        this.servletContext = null;
    }
    
    public static void setServletContext(ServletContext servletContext) {
        SERVLET_CONTEXT = servletContext;
    }
    
    public List<String> doList(String directory, String[] suffixes) throws IOException {
        return UrlUtils.listUrl(servletContext.getResource(directory), suffixes);
    }

    protected Resource doLoad(String name, String encoding, String path) throws IOException {
		return new ServletResource(getEngine(), name, encoding, path, servletContext != null ? servletContext : SERVLET_CONTEXT);
	}

}
