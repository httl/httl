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

import java.io.IOException;
import java.text.ParseException;

/**
 * AstVisitor. (API, Prototype, Callback, NonThreadSafe)
 * 
 * @see httl.Node#accept(Visitor)
 * @see httl.Template#accept(Visitor)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AstVisitor implements Visitor {

	public boolean visit(Node node) throws IOException, ParseException {
		if (node instanceof Expression) {
			visit((Expression) node);
		} else if (node instanceof Statement) {
			return visit((Statement) node);
		} else if (node instanceof Template) {
			return visit((Template) node);
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
		if (node instanceof PositiveOperator) {
			visit((PositiveOperator) node);
		} else if (node instanceof NegativeOperator) {
			visit((NegativeOperator) node);
		} else if (node instanceof NotOperator) {
			visit((NotOperator) node);
		} else if (node instanceof BitNotOperator) {
			visit((BitNotOperator) node);
		} else if (node instanceof ListOperator) {
			visit((ListOperator) node);
		} else if (node instanceof NewOperator) {
			visit((NewOperator) node);
		} else if (node instanceof StaticMethodOperator) {
			visit((StaticMethodOperator) node);
		} else if (node instanceof CastOperator) {
			visit((CastOperator) node);
		}
	}

	public void visit(BinaryOperator node) throws IOException, ParseException {
		if (node instanceof AddOperator) {
			visit((AddOperator) node);
		} else if (node instanceof SubOperator) {
			visit((SubOperator) node);
		} else if (node instanceof MulOperator) {
			visit((MulOperator) node);
		} else if (node instanceof DivOperator) {
			visit((DivOperator) node);
		} else if (node instanceof ModOperator) {
			visit((ModOperator) node);
		} else if (node instanceof EqualsOperator) {
			visit((EqualsOperator) node);
		} else if (node instanceof NotEqualsOperator) {
			visit((NotEqualsOperator) node);
		} else if (node instanceof GreaterOperator) {
			visit((GreaterOperator) node);
		} else if (node instanceof GreaterEqualsOperator) {
			visit((GreaterEqualsOperator) node);
		} else if (node instanceof LessOperator) {
			visit((LessOperator) node);
		} else if (node instanceof LessEqualsOperator) {
			visit((LessEqualsOperator) node);
		} else if (node instanceof AndOperator) {
			visit((AndOperator) node);
		} else if (node instanceof OrOperator) {
			visit((OrOperator) node);
		} else if (node instanceof BitAndOperator) {
			visit((BitAndOperator) node);
		} else if (node instanceof BitOrOperator) {
			visit((BitOrOperator) node);
		} else if (node instanceof BitXorOperator) {
			visit((BitXorOperator) node);
		} else if (node instanceof RightShiftOperator) {
			visit((RightShiftOperator) node);
		} else if (node instanceof LeftShiftOperator) {
			visit((LeftShiftOperator) node);
		} else if (node instanceof UnsignShiftOperator) {
			visit((UnsignShiftOperator) node);
		} else if (node instanceof ArrayOperator) {
			visit((ArrayOperator) node);
		} else if (node instanceof ConditionOperator) {
			visit((ConditionOperator) node);
		} else if (node instanceof EntryOperator) {
			visit((EntryOperator) node);
		} else if (node instanceof InstanceofOperator) {
			visit((InstanceofOperator) node);
		} else if (node instanceof IndexOperator) {
			visit((IndexOperator) node);
		} else if (node instanceof SequenceOperator) {
			visit((SequenceOperator) node);
		} else if (node instanceof MethodOperator) {
			visit((MethodOperator) node);
		}
	}

	public void visit(Constant node) throws IOException, ParseException {
	}

	public void visit(Variable node) throws IOException, ParseException {
	}

	public void visit(PositiveOperator node) throws IOException, ParseException {
	}

	public void visit(NegativeOperator node) throws IOException, ParseException {
	}

	public void visit(NotOperator node) throws IOException, ParseException {
	}

	public void visit(BitNotOperator node) throws IOException, ParseException {
	}

	public void visit(ListOperator node) throws IOException, ParseException {
	}

	public void visit(NewOperator node) throws IOException, ParseException {
	}

	public void visit(StaticMethodOperator node) throws IOException, ParseException {
	}

	public void visit(CastOperator node) throws IOException, ParseException {
	}

	public void visit(AddOperator node) throws IOException, ParseException {
	}

	public void visit(SubOperator node) throws IOException, ParseException {
	}

	public void visit(MulOperator node) throws IOException, ParseException {
	}

	public void visit(DivOperator node) throws IOException, ParseException {
	}

	public void visit(ModOperator node) throws IOException, ParseException {
	}

	public void visit(EqualsOperator node) throws IOException, ParseException {
	}

	public void visit(NotEqualsOperator node) throws IOException,
			ParseException {
	}

	public void visit(GreaterOperator node) throws IOException, ParseException {
	}

	public void visit(GreaterEqualsOperator node) throws IOException,
			ParseException {
	}

	public void visit(LessOperator node) throws IOException, ParseException {
	}

	public void visit(LessEqualsOperator node) throws IOException,
			ParseException {
	}

	public void visit(AndOperator node) throws IOException, ParseException {
	}

	public void visit(OrOperator node) throws IOException, ParseException {
	}

	public void visit(BitAndOperator node) throws IOException, ParseException {
	}

	public void visit(BitOrOperator node) throws IOException, ParseException {
	}

	public void visit(BitXorOperator node) throws IOException, ParseException {
	}

	public void visit(RightShiftOperator node) throws IOException,
			ParseException {
	}

	public void visit(LeftShiftOperator node) throws IOException,
			ParseException {
	}

	public void visit(UnsignShiftOperator node) throws IOException,
			ParseException {
	}

	public void visit(ArrayOperator node) throws IOException, ParseException {
	}

	public void visit(ConditionOperator node) throws IOException,
			ParseException {
	}

	public void visit(EntryOperator node) throws IOException, ParseException {
	}

	public void visit(InstanceofOperator node) throws IOException,
			ParseException {
	}

	public void visit(IndexOperator node) throws IOException, ParseException {
	}

	public void visit(SequenceOperator node) throws IOException, ParseException {
	}

	public void visit(MethodOperator node) throws IOException, ParseException {
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