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
import httl.util.UrlUtils;

import java.io.IOException;
import java.util.List;

/**
 * ClasspathLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ClasspathLoader extends AbstractLoader {

	public List<String> doList(String directory, String[] suffixes) throws IOException {
        return UrlUtils.listUrl(Thread.currentThread().getContextClassLoader().getResource(cleanPath(directory)), suffixes);
    }
    
    protected Resource doLoad(String name, String encoding, String path) throws IOException {
		return new ClasspathResource(getEngine(), name, encoding, cleanPath(path));
	}

	public boolean doExists(String name, String path) throws Exception {
		return Thread.currentThread().getContextClassLoader().getResource(cleanPath(path)) != null;
	}
	
	private String cleanPath(String path) {
		return path.startsWith("/") ? path.substring(1) : path;
	}

}
