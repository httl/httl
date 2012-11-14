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
package httl.spi.parsers.template;

import httl.Context;
import httl.Engine;
import httl.Template;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.util.ClassUtils;
import httl.util.UnsafeByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/**
 * OutputStream template. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class OutputStreamTemplate extends AbstractTemplate {

    private static final long serialVersionUID = 7127901461769617745L;

    public OutputStreamTemplate(Engine engine, Filter filter, 
    		Formatter<?> formatter, Map<Class<?>, Object> functions,
    		Map<String, Template> importMacros){
        super(engine, filter, formatter, functions, importMacros);
    }

    public String render(Map<String, Object> parameters) {
        UnsafeByteArrayOutputStream output = new UnsafeByteArrayOutputStream();
        try {
            render(parameters, output);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return toString(output);
    }

    public void render(Map<String, Object> parameters, OutputStream output) throws IOException {
        if (output == null) 
        	throw new IllegalArgumentException("output == null");
        Context context = Context.pushContext(this, parameters, output);
        try {
            doRender(context, parameters, output);
        } catch (RuntimeException e) {
            throw (RuntimeException) e;
        } catch (IOException e) {
            throw (IOException) e;
        } catch (Exception e) {
            throw new IllegalStateException(ClassUtils.toString(e), e);
        } finally {
        	Context.popContext();
        }
    }

    public void render(Map<String, Object> parameters, Writer writer) throws IOException {
        writer.write(render(parameters));
    }

    protected abstract void doRender(Context context, Map<String, Object> parameters, OutputStream output) throws Exception;

}
