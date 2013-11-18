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
import httl.Template;
import httl.Visitor;
import httl.util.ClassUtils;

import java.io.IOException;
import java.text.ParseException;

/**
 * Abstract Syntax Tree (AST) Visitor. (API, Prototype, Callback, NonThreadSafe)
 * 
 * @see httl.Node#accept(Visitor)
 * @see httl.Template#accept(Visitor)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AstVisitor implements Visitor {

	public boolean visit(Node node) throws IOException, ParseException {
		try {
			if (node instanceof Expression) {
				visit((Expression) node);
			} else if (node instanceof Statement) {
				return visit((Statement) node);
			} else if (node instanceof Template) {
				return visit((Template) node);
			}
		} catch (IOException e) {
			throw e;
		} catch (ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new ParseException(ClassUtils.toString(e), node.getOffset());
		}
		return true;
	}

	public boolean visit(Statement node) throws IOException, ParseException {
		if (node instanceof Text) {
			visit((Text) node);
		} else if (node instanceof Comment) {
			visit((Comment) node);
		} else if (node instanceof LineDirective) {
			visit((LineDirective) node);
		} else if (node instanceof BlockDirective) {
			return visit((BlockDirective) node);
		} else if (node instanceof EndDirective) {
			visit((EndDirective) node);
		}
		return true;
	}

	public void visit(LineDirective node) throws IOException, ParseException {
		if (node instanceof ValueDirective) {
			visit((ValueDirective) node);
		} else if (node instanceof SetDirective) {
			visit((SetDirective) node);
		} else if (node instanceof BreakDirective) {
			visit((BreakDirective) node);
		}
	}

	public boolean visit(BlockDirective node) throws IOException, ParseException {
		if (node instanceof IfDirective) {
			return visit((IfDirective) node);
		} else if (node instanceof ElseDirective) {
			return visit((ElseDirective) node);
		} else if (node instanceof ForDirective) {
			return visit((ForDirective) node);
		} else if (node instanceof MacroDirective) {
			return visit((MacroDirective) node);
		}
		return true;
	}

	public void visit(EndDirective node) throws IOException, ParseException {
		Node start = ((EndDirective) node).getStart();
		if (start instanceof IfDirective) {
			end((IfDirective) start);
		} else if (start instanceof ElseDirective) {
			end((ElseDirective) start);
		} else if (start instanceof ForDirective) {
			end((ForDirective) start);
		} else if (start instanceof MacroDirective) {
			end((MacroDirective) start);
		}
	}

	public void visit(Expression node) throws IOException, ParseException {
		if (node instanceof Constant) {
			visit((Constant) node);
		} else if (node instanceof Variable) {
			visit((Variable) node);
		} else if (node instanceof UnaryOperator) {
			visit((UnaryOperator) node);
		} else if (node instanceof BinaryOperator) {
			visit((BinaryOperator) node);
		}
	}
	
	public void visit(UnaryOperator node) throws IOException, ParseException {
	}

	public void visit(BinaryOperator node) throws IOException, ParseException {
	}

	public void visit(Constant node) throws IOException, ParseException {
	}

	public void visit(Variable node) throws IOException, ParseException {
	}

	public void visit(Text node) throws IOException, ParseException {
	}

	public void visit(ValueDirective node) throws IOException, ParseException {
	}

	public void visit(Comment node) throws IOException, ParseException {
	}

	public void visit(SetDirective node) throws IOException, ParseException {
	}

	public void visit(BreakDirective node) throws IOException, ParseException {
	}

	public boolean visit(Template node) throws IOException, ParseException {
		return true;
	}

	public boolean visit(IfDirective node) throws IOException, ParseException {
		return true;
	}

	public boolean visit(ElseDirective node) throws IOException, ParseException {
		return true;
	}

	public boolean visit(ForDirective node) throws IOException, ParseException {
		return true;
	}

	public boolean visit(MacroDirective node) throws IOException, ParseException {
		return true;
	}

	public void end(IfDirective node) throws IOException, ParseException {
	}

	public void end(ElseDirective node) throws IOException, ParseException {
	}

	public void end(ForDirective node) throws IOException, ParseException {
	}

	public void end(MacroDirective node) throws IOException, ParseException {
	}

}