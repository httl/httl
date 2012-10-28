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
import java.io.Reader;
import java.io.Serializable;

/**
 * Resource. (API, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getResource(String)
 * @see httl.Engine#getResource(String, String)
 * @see httl.spi.Loader#load(String, String)
 * @see httl.spi.Parser#parse(Resource)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Resource extends Serializable {

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
	 * Get the the template last modified time.
	 * 
	 * @return last modified
	 */
	long getLastModified();

	/**
	 * Get the the template length.
	 * 
	 * @return length
	 */
	long getLength();

	/**
	 * Get the template source reader.
	 * 
	 * NOTE: Don't forget close the reader.
	 * 
	 * <code>
	 * Reader reader = resource.getSource();
	 * try {
	 *     // do something ...
	 * } finally {
	 *     readerc.close();
	 * }
	 * </code>
	 * 
	 * @return source reader
	 */
	Reader getSource() throws IOException;

	/**
	 * Get the template engine.
	 * 
	 * @return template engine.
	 */
	Engine getEngine();

}
