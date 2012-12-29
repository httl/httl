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
import httl.util.IOUtils;

import java.io.IOException;
import java.io.Serializable;

/**
 * AbstractResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.AbstractLoader#load(String, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractResource implements Resource, Serializable {

    private static final long serialVersionUID = 6834431114838915042L;

    private final transient Engine engine;
    
    private final String name;
    
    private final String encoding;

	private final long lastModified;

    public AbstractResource(Engine engine, String name, String encoding) {
    	this(engine, name, encoding, -1);
    }

    public AbstractResource(Engine engine, String name, String encoding, long lastModified) {
        this.engine = engine;
        this.name = name;
        this.encoding = encoding;
        this.lastModified = lastModified;
    }

    public Engine getEngine() {
        return engine;
    }

    public String getName() {
        return name;
    }

    public String getEncoding() {
        return encoding;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getLength() {
        return -1;
    }

    public String getSource() {
    	try {
			return IOUtils.readToString(getReader());
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
    }
    
    @Override
    public String toString() {
    	try {
			return IOUtils.readToString(getReader());
		} catch (IOException e) {
			return super.toString();
		}
    }

}
