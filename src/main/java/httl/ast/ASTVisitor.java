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

import java.text.ParseException;

/**
 * ASTVisitor
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class ASTVisitor implements Visitor {

	public boolean visit(Node node) throws ParseException {
		if (node instanceof Statement) {
			return visit((Statement) node);
		} else if (node instanceof Expression) {
			visit((Expression) node);
		}
		return true;
	}

	public boolean visit(Statement node) throws ParseException {
		if (node instanceof Text) {
			visit((Text) node);
		} else if (node instanceof Value) {
			visit((Value) node);
		} else if (node instanceof Comment) {
			visit((Comment) node);
		} else if (node instanceof Var) {
			visit((Var) node);
		} else if (node instanceof Break) {
			visit((Break) node);
		} else if (node instanceof Block) {
			return visit((Block) node);
		} else if (node instanceof End) {
			visit((End) node);
		}
		return true;
	}

	public boolean visit(Block node) throws ParseException {
		if (node instanceof If) {
			return visit((If) node);
		} else if (node instanceof Else) {
			return visit((Else) node);
		} else if (node instanceof For) {
			return visit((For) node);
		} else if (node instanceof Macro) {
			return visit((Macro) node);
		}
		return true;
	}

	public void visit(End node) throws ParseException {
		Node start = node.getStart();
		if (start instanceof If) {
			end((If) start);
		} else if (start instanceof Else) {
			end((Else) start);
		} else if (start instanceof For) {
			end((For) start);
		} else if (start instanceof Macro) {
			end((Macro) start);
		}
	}

	public void visit(Expression node) throws ParseException {
		if (node instanceof Constant) {
			visit((Constant) node);
		} else if (node instanceof Variable) {
			visit((Variable) node);
		} else if (node instanceof Operator) {
			visit((Operator) node);
		}
	}

	public void visit(Operator node) throws ParseException {
		if (node instanceof UnaryOperator) {
			visit((UnaryOperator) node);
		} else if (node instanceof BinaryOperator) {
			visit((BinaryOperator) node);
		}
	}

	public void visit(Text node) throws ParseException {}

	public void visit(Value node) throws ParseException {}

	public void visit(Comment node) throws ParseException {}

	public void visit(Var node) throws ParseException {}

	public void visit(Break node) throws ParseException {}

	public boolean visit(If node) throws ParseException {
		return false;
	}

	public boolean visit(Else node) throws ParseException {
		return false;
	}

	public boolean visit(For node) throws ParseException {
		return false;
	}

	public boolean visit(Macro node) throws ParseException {
		return false;
	}

	public void end(If node) throws ParseException {}

	public void end(Else node) throws ParseException {}

	public void end(For node) throws ParseException {}

	public void end(Macro node) throws ParseException {}

	public void visit(Constant node) throws ParseException {}

	public void visit(Variable node) throws ParseException {}

	public void visit(UnaryOperator node) throws ParseException {}

	public void visit(BinaryOperator node) throws ParseException {}

}