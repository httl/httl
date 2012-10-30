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
package httl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

/**
 * Template. (API, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * @see httl.Engine#getTemplate(String, String)
 * @see httl.Context#getTemplate()
 * @see httl.spi.Parser#parse(Resource)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Template extends Resource {

    /**
     * Render the template to a string.
     * 
     * @see httl.Context#getParameters()
     * @param parameters - parameters
     * @return Template render result
     */
    String render(Map<String, Object> parameters);

    /**
     * Render the template to output stream.
     * 
     * @see httl.Context#getParameters()
     * @param parameters - parameters
     * @param output - output stream
     * @throws IOException Failed to output
     */
    void render(Map<String, Object> parameters, OutputStream output) throws IOException;

    /**
     * Render the template to writer.
     * 
     * @see httl.Context#getParameters()
     * @param parameters - parameters
     * @param writer - writer
     * @throws IOException Failed to writer
     */
    void render(Map<String, Object> parameters, Writer writer) throws IOException;

    /**
     * Get the template parameter types. (Ordered)
     * 
     * @return parameter types.
     */
    Map<String, Class<?>> getParameterTypes();

    /**
     * Get the macro templates.
     * 
     * @return macro templates.
     */
    Map<String, Template> getMacros();

    /**
     * Get the template code.
     * 
     * @return code.
     */
 
    String getCode() throws ParseException;

}
