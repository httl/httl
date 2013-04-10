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

/**
 * Node. (API, Prototype, Immutable, ThreadSafe)
 * 
 * @see httl.Template
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Node {

	/**
	 * Accept a visitor.
	 * 
	 * @param visitor
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the expression cannot be parsed on runtime
	 */
	void accept(Visitor visitor) throws IOException, ParseException;

	/**
	 * Get the node offset.
	 * 
	 * @return offset
	 */
	int getOffset();

	/**
	 * Get the parent node.
	 * 
	 * @return parent
	 */
	Node getParent();

}