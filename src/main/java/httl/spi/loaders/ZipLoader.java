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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * ZipLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ZipLoader extends AbstractLoader {
	
	private File file;
	
	public void setTemplateDirectory(String directory) {
	    super.setTemplateDirectory(directory);
	    file = new File(getDirectory());
	}
	
	protected List<String> doList(String directory, String[] suffixes) throws IOException {
	    ZipFile zipFile = new ZipFile(file);
	    try {
	        return UrlUtils.listZip(zipFile, suffixes);
	    } finally {
	        zipFile.close();
	    }
    }
	
	public Resource doLoad(String name, String encoding, String path) throws IOException {
		return new ZipResource(getEngine(), name, encoding, file);
	}

	public boolean doExists(String name, String path) throws Exception {
		return file.exists() && new ZipFile(file).getEntry(name) != null;
	}

}
