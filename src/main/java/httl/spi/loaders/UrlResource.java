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

import httl.spi.Loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * UrlResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.UrlLoader#load(String, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class UrlResource extends InputStreamResource {
    
    private static final long serialVersionUID = 1L;
    
    private static final String FILE_PROTOCOL = "file";
    
    private final URL url;
    
    private final File file;
    
    public UrlResource(Loader loader, String name, String encoding, String path) throws IOException {
        super(loader, name, encoding);
        this.url = new URL(path);
        this.file = toFile(url);
    }
    
    private File toFile(URL url) throws IOException {
        if (FILE_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
            try {
                return new File(url.toURI().getSchemeSpecificPart());
            } catch (URISyntaxException e) {
                throw new MalformedURLException(e.getMessage());
            }
        }
        return null;
    }
    
    public long getLastModified() {
        if (file != null) {
            return file.lastModified();
        }
        return super.getLastModified();
    }

    public long getLength() {
        if (file != null) {
            return file.length();
        }
        return super.getLength();
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return url.openStream();
    }
    
}
