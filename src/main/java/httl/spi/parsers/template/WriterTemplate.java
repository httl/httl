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
import httl.util.UnsafeStringWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Writer Template. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class WriterTemplate extends AbstractTemplate {
    
    private static final long serialVersionUID = 7127901461769617745L;

    public WriterTemplate(Engine engine, Filter filter, 
    		Formatter<?> formatter, Map<Class<?>, Object> functions,
    		Map<String, Template> importMacros){
        super(engine, filter, formatter, functions, importMacros);
    }

    public Class<?> getReturnType() {
    	return String.class;
    }

    public Object evaluate(Map<String, Object> parameters) {
        UnsafeStringWriter writer = new UnsafeStringWriter();
        try {
            render(parameters, writer);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return writer.toString();
    }

    public void render(Map<String, Object> parameters, OutputStream output) throws IOException {
    	Writer writer = new OutputStreamWriter(output);
    	render(parameters, writer);
    	writer.flush();
    }

    public void render(Map<String, Object> parameters, Writer writer) throws IOException {
    	if (writer == null) 
         	throw new IllegalArgumentException("writer == null");
    	if (Context.getContext().getTemplate() == this)
    		throw new IllegalStateException("The template " + getName() + " can not be recursive rendering the self template.");
        Context context = Context.pushContext(this, parameters, writer);
    	try {
    		doRender(context, writer);
        } catch (RuntimeException e) {
            throw (RuntimeException) e;
        } catch (IOException e) {
            throw (IOException) e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
        	Context.popContext();
        }
    }

    protected abstract void doRender(Context context, Writer output) throws Exception;

}
