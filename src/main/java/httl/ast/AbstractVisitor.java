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
package httl.ast;

import httl.Node;
import httl.Visitor;

/**
 * AbstractVisitor
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class AbstractVisitor implements Visitor {

	public void visit(Node node) {
		if (node instanceof Text) {
			visit((Text) node);
		} else if (node instanceof Value) {
			visit((Value) node);
		} else if (node instanceof Set) {
			visit((Set) node);
		} else if (node instanceof If) {
			visit((If) node);
		} else if (node instanceof ElseIf) {
			visit((ElseIf) node);
		} else if (node instanceof Else) {
			visit((Else) node);
		} else if (node instanceof Foreach) {
			visit((Foreach) node);
		} else if (node instanceof BreakIf) {
			visit((BreakIf) node);
		} else if (node instanceof Macro) {
			visit((Macro) node);
		} else if (node instanceof End) {
			visit((End) node);
		}
	}

	public void visit(Text node) {}

	public void visit(Value node) {}

	public void visit(Set node) {}

	public void visit(If node) {}

	public void visit(ElseIf node) {}

	public void visit(Else node) {}

	public void visit(Foreach node) {}

	public void visit(BreakIf node) {}

	public void visit(Macro node) {}

	public void visit(End node) {}

}
