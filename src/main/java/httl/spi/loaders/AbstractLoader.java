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
import httl.Resource;
import httl.spi.Loader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractLoader implements Loader {
	
	private Engine engine;

	private String encoding;
    
    private String directory;
    
    private String[] suffixes;

	public Engine getEngine() {
		return engine;
	}

    public void setEngine(Engine engine) {
		this.engine = engine;
	}

    protected String getEncoding() {
        return encoding;
    }
    
    protected String getDirectory() {
        return directory;
    }
    
    public String[] getSuffixes() {
        return suffixes;
    }
    
    public void setInputEncoding(String encoding) {
    	if (encoding != null && encoding.length() > 0) {
            Charset.forName(encoding);
            this.encoding = encoding;
        }
    }

    public void setTemplateDirectory(String directory) {
    	if (directory != null && directory.length() > 0) {
            this.directory = directory;
        }
    }

    public void setTemplateSuffix(String[] suffix) {
    	this.suffixes = suffix;
    }

    public List<String> list() throws IOException {
        String directory = getDirectory();
        if (directory == null || directory.length() == 0) {
            return new ArrayList<String>(0);
        }
        String[] suffixes = getSuffixes();
        if (suffixes == null || suffixes.length == 0) {
            return new ArrayList<String>(0);
        }
        List<String> list = doList(directory, suffixes);
        if (list == null) {
            list = new ArrayList<String>(0);
        }
        return list;
    }
    
    protected abstract List<String> doList(String directory, String[] suffixes) throws IOException;
    
    public Resource load(String name, String encoding) throws IOException {
        if (encoding == null || encoding.length() == 0) {
            encoding = this.encoding;
        }
        return doLoad(name, encoding, directory == null ? name : directory + name);
    }
    
    protected abstract Resource doLoad(String name, String encoding, String path) throws IOException;
    
}
