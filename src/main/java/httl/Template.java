/*
 * Copyright 2011-2012 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

/**
 * Template. (API, Prototype, Immutable, ThreadSafe)
 * 
 * <pre>
 * Engine engine = Engine.getEngine();
 * Template template = engine.getTemplate("/foo.httl");
 * </pre>
 * 
 * @see httl.Engine#getTemplate(String)
 * @see httl.Engine#getTemplate(String, String)
 * @see httl.Context#getTemplate()
 * @see httl.spi.Parser#parse(Resource)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Template extends Resource, Expression {

	/**
	 * Render the template to output stream.
	 * 
	 * <pre>
	 * OutputStream stream = ...;
	 * Context context = Context.getContext();
	 * context.put("foo", foo);
	 * template.render(stream);
	 * </pre>
	 * 
	 * @see httl.Context#getContext()
	 * @see httl.Context#getOut()
	 * @param stream - output stream
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void render(OutputStream stream) throws IOException, ParseException;

	/**
	 * Render the template to output stream.
	 * 
	 * <pre>
	 * OutputStream stream = ...;
	 * Map&lt;String, Object&gt; parameters = new HashMap&lt;String, Object&gt;();
	 * parameters.put("foo", foo);
	 * template.render(parameters, stream);
	 * </pre>
	 * 
	 * @see httl.Context#getContext()
	 * @see httl.Context#getOut()
	 * @param parameters - render parameters
	 * @param stream - output stream
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void render(Map<String, Object> parameters, OutputStream stream) throws IOException, ParseException;

	/**
	 * Render the template to writer.
	 * 
	 * <pre>
	 * Writer writer = ...;
	 * Context context = Context.getContext();
	 * context.put("foo", foo);
	 * template.render(writer);
	 * </pre>
	 * 
	 * @see httl.Context#getContext()
	 * @see httl.Context#getOut()
	 * @param writer - output writer
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void render(Writer writer) throws IOException, ParseException;

	/**
	 * Render the template to writer.
	 * 
	 * <pre>
	 * Writer writer = ...;
	 * Map&lt;String, Object&gt; parameters = new HashMap&lt;String, Object&gt;();
	 * parameters.put("foo", foo);
	 * template.render(parameters, writer);
	 * </pre>
	 * 
	 * @see httl.Context#getContext()
	 * @see httl.Context#getOut()
	 * @param parameters - render parameters
	 * @param writer - output writer
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void render(Map<String, Object> parameters, Writer writer) throws IOException, ParseException;

	/**
	 * Get the template set to context types.
	 * 
	 * @return context types
	 */
	Map<String, Class<?>> getContextTypes();

	/**
	 * Get the macro templates.
	 * 
	 * @return macro templates
	 */
	Map<String, Template> getMacros();

	/**
	 * Get the template macro flag.
	 * 
	 * @return true - if this template is a macro.
	 */
	boolean isMacro();

}