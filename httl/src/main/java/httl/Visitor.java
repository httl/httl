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
 * Visitor. (API, Prototype, Callback, NonThreadSafe)
 * 
 * @see httl.Node#accept(Visitor)
 * @see httl.Template#accept(Visitor)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Visitor {

	/**
	 * Visit a node.
	 * 
	 * @param node - visited node
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the expression cannot be parsed on runtime
	 * @return true - need visit the children node.
	 */
	boolean visit(Node node) throws IOException, ParseException;

}