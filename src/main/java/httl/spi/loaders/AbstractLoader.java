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
import httl.spi.Logger;
import httl.util.UrlUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractLoader implements Loader {
	
	private Engine engine;

	private String encoding;
    
    private String directory;
    
    private String suffix;

    private Logger logger;

	private boolean reloadable;

	private volatile boolean first = true;

	/**
	 * httl.properties: engine=httl.spi.engines.DefaultEngine
	 */
    public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/**
	 * httl.properties: loggers=httl.spi.loggers.Log4jLogger
	 */
    public void setLogger(Logger logger) {
		this.logger = logger;
	}

    /**
	 * httl.properties: reloadable=true
	 */
	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}

    /**
	 * httl.properties: input.encoding=UTF-8
	 */
    public void setInputEncoding(String encoding) {
    	if (encoding != null && encoding.length() > 0) {
            Charset.forName(encoding);
            this.encoding = encoding;
        }
    }

    /**
	 * httl.properties: template.directory=/META-INF/templates
	 */
    public void setTemplateDirectory(String directory) {
    	if (directory != null && directory.length() > 0) {
            this.directory = UrlUtils.cleanDirectory(directory);
        }
    }

    /**
	 * httl.properties: template.suffix=.httl
	 */
    public void setTemplateSuffix(String suffix) {
    	this.suffix = suffix;
    }

	protected Engine getEngine() {
		return engine;
	}

	protected Logger getLogger() {
		return logger;
	}

    protected String getEncoding() {
        return encoding;
    }

    protected String getDirectory() {
        return directory;
    }

    protected String getSuffix() {
        return suffix;
    }

    protected String toPath(String name) {
    	if (name == null || name.length() == 0) {
    		throw new IllegalArgumentException("resource name == null");
    	}
    	name = UrlUtils.cleanName(name);
    	if (directory == null || (suffix != null && ! name.endsWith(suffix))) {
			return name;
		} else {
			return directory + name;
		}
    }

    public List<String> list() throws IOException {
        String directory = getDirectory();
        if (directory == null || directory.length() == 0) {
            return new ArrayList<String>(0);
        }
        List<String> list = doList(directory, new String[] { suffix == null ? "" : suffix });
        if (list == null || list.size() == 0) {
            return new ArrayList<String>(0);
        } else {
        	List<String> result = new ArrayList<String>(list.size());
        	for (String name : list) {
        		if (name != null && name.length() > 0) {
        			result.add(UrlUtils.cleanName(name));
        		}
        	}
        	return result;
        }
    }

    protected abstract List<String> doList(String directory, String[] suffixes) throws IOException;

	public boolean exists(String name) {
		try {
			return doExists(name, toPath(name));
		} catch (Exception e) {
			return false;
		}
	}

	public abstract boolean doExists(String name, String path) throws Exception;

    public Resource load(String name, String encoding) throws IOException {
        if (encoding == null || encoding.length() == 0) {
            encoding = this.encoding;
        }
        Resource resource = doLoad(name, encoding, toPath(name));
        if (first) {
        	first = false;
        	if (logger != null && logger.isInfoEnabled()
        			&& resource instanceof InputStreamResource) {
	        	File file = ((InputStreamResource) resource).getFile();
	    		if (file != null && file.exists()) {
	    			String uri = name.replace('\\', '/');
		    		String abs = file.getAbsolutePath().replace('\\', '/');
		    		if (abs.endsWith(uri)) {
		    			abs = abs.substring(0, abs.length() - uri.length());
		    		} else {
			    		int i = abs.lastIndexOf('/');
			        	if (i > 0) {
			        		abs = abs.substring(0, i);
			        	} else {
			        		abs = "/";
			        	}
		    		}
		        	logger.info("Load httl template from" + (reloadable ? " reloadable" : "") + " directory " + abs + " by " + getClass().getSimpleName() + ".");
	    		}
        	}
        }
        return resource;
    }

    protected abstract Resource doLoad(String name, String encoding, String path) throws IOException;

}
