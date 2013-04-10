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
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;

/**
 * Resource. (API, Prototype, Immutable, ThreadSafe)
 * 
 * <pre>
 * Engine engine = Engine.getEngine();
 * Resource resource = engine.getResource("/foo.txt");
 * </pre>
 * 
 * @see httl.Engine#getResource(String)
 * @see httl.Engine#getResource(String, String)
 * @see httl.spi.Loader#load(String, Locale, String)
 * @see httl.spi.Translator#translate(Resource, java.util.Map)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Resource {

	/**
	 * Get the template name.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * Get the the template encoding.
	 * 
	 * @return encoding
	 */
	String getEncoding();

	/**
	 * Get the the template locale.
	 * 
	 * @return locale
	 */
	Locale getLocale();

	/**
	 * Get the the template last modified time.
	 * 
	 * @return last modified time
	 */
	long getLastModified();

	/**
	 * Get the the template length.
	 * 
	 * @return source length
	 */
	long getLength();

	/**
	 * Get the template source.
	 * 
	 * @return source
	 * @throws IOException - If an I/O error occurs
	 */
	String getSource() throws IOException;

	/**
	 * Get the template source reader.
	 * 
	 * NOTE: Don't forget close the reader.
	 * 
	 * <pre>
	 * Reader reader = resource.getReader();
	 * try {
	 *	 // do something ...
	 * } finally {
	 *	 reader.close();
	 * }
	 * </pre>
	 * 
	 * @return source reader
	 * @throws IOException - If an I/O error occurs
	 */
	Reader getReader() throws IOException;

	/**
	 * Get the template source input stream.
	 * 
	 * NOTE: Don't forget close the input stream.
	 * 
	 * <pre>
	 * InputStream input = resource.getInputStream();
	 * try {
	 *	 // do something ...
	 * } finally {
	 *	 input.close();
	 * }
	 * </pre>
	 * 
	 * @return source input stream
	 * @throws IOException - If an I/O error occurs
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Get the template engine.
	 * 
	 * @return engine
	 */
	Engine getEngine();

}