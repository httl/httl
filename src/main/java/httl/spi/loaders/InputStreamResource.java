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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


/**
 * InputStreamResource. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class InputStreamResource extends AbstractResource {

    private static final long serialVersionUID = -5150738383353330217L;

    public InputStreamResource(Engine engine, String name, String encoding){
        super(engine, name, encoding);
    }

    public Reader getSource() throws IOException {
        InputStream in = getInputStream();
        if (in == null) {
            throw new FileNotFoundException("Not found template " + getName());
        }
        String encoding = getEncoding();
        return encoding == null || encoding.length() == 0 
            ? new InputStreamReader(in) : new InputStreamReader(in, encoding);
    }
    
    protected abstract InputStream getInputStream() throws IOException;

}
