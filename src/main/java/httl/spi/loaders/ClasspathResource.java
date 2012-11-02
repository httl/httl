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

import httl.Engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * ClasspathResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.ClasspathLoader#load(String, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ClasspathResource extends InputStreamResource {

    private static final long serialVersionUID = 2499229996487593996L;
    
    private final String path;

    public ClasspathResource(Engine engine, String name, String encoding, String path) {
        super(engine, name, encoding);
        this.path = (path.startsWith("/") ? path.substring(1) : path);
    }

    public long getLastModified() {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url != null) {
                if ("file".equals(url.getProtocol())) {
                    String path = url.getFile();
                    if (path.startsWith("file:")) {
                        path = path.substring("file:".length());
                    }
                    File file = new File(path);
                    if (file.exists()) {
                        return file.lastModified();
                    }
                } else if ("jar".equals(url.getProtocol())) {
                    String path = url.getFile();
                    if (path.startsWith("jar:")) {
                        path = path.substring("jar:".length());
                    }
                    if (path.startsWith("file:")) {
                        path = path.substring("file:".length());
                    }
                    int i = path.indexOf("!/");
                    if (i > 0) {
                        path = path.substring(0, i);
                    }
                    File file = new File(path);
                    if (file.exists()) {
                        return file.lastModified();
                    }
                }
            }
        } catch (Throwable t) {
        }
        return -1;
    }

    public InputStream getInputStream() throws IOException {
    	return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

}