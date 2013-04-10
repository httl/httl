/*
 * Copyright 2011-2013 HTTL Team.
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
import java.text.ParseException;
import java.util.List;
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
 * @see httl.spi.Translator#translate(Resource, java.util.Map)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Template extends Node, Resource {

	/**
	 * Render the template to output stream.
	 * 
	 * <pre>
	 * Writer/OutputStream out = ...;
	 * Map&lt;String, Object&gt; context = new HashMap&lt;String, Object&gt;();
	 * context.put("foo", foo);
	 * template.render(context, out);
	 * </pre>
	 * 
	 * @see httl.Context#getContext()
	 * @see httl.Context#getOut()
	 * @param context - render context
	 * @param out - output
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void render(Object context, Object out) throws IOException, ParseException;

	/**
	 * Render the template to output stream.
	 * 
	 * <pre>
	 * Writer/OutputStream out = ...;
	 * Context context = Context.getContext();
	 * context.put("foo", foo);
	 * template.render(out);
	 * </pre>
	 * 
	 * @see httl.Context#getContext()
	 * @see httl.Context#getOut()
	 * @param out - output
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void render(Object out) throws IOException, ParseException;

	/**
	 * Evaluate the template.
	 * 
	 * <pre>
	 * Map&lt;String, Object&gt; context = new HashMap&lt;String, Object&gt;();
	 * context.put("foo", foo);
	 * Object result = template.evaluate(context);
	 * </pre>
	 * 
	 * @param context - evaluate context
	 * @return evaluate result
	 * @throws ParseException - If the expression cannot be parsed on runtime
	 */
	Object evaluate(Object context) throws ParseException;

	/**
	 * Evaluate the template.
	 * 
	 * <pre>
	 * Context context = Context.getContext();
	 * context.put("foo", foo);
	 * Object result = template.evaluate();
	 * </pre>
	 * 
	 * @return evaluate result
	 * @throws ParseException - If the expression cannot be parsed on runtime
	 */
	Object evaluate() throws ParseException;

	/**
	 * Get the macro parent template.
	 * 
	 * @return parent template
	 */
	Template getParent();

	/**
	 * Get the template children nodes.
	 * 
	 * @return children nodes
	 */
	List<Node> getChildren();

	/**
	 * Get the template variables. (Ordered)
	 * 
	 * @return variable types
	 */
	Map<String, Class<?>> getVariables();

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