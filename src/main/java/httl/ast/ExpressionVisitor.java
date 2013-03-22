package httl.ast;

import java.text.ParseException;

import httl.Node;
import httl.Visitor;

public class ExpressionVisitor implements Visitor {

	public boolean visit(Node node) throws ParseException {
		if (node instanceof Constant) {
			visit((Constant) node);
		} else if (node instanceof Variable) {
			visit((Variable) node);
		} else if (node instanceof UnaryOperator) {
			visit((UnaryOperator) node);
		} else if (node instanceof BinaryOperator) {
			visit((BinaryOperator) node);
		}
		return true;
	}

	public void visit(Constant node) throws ParseException {}

	public void visit(Variable node) throws ParseException {}

	public void visit(UnaryOperator node) throws ParseException {}

	public void visit(BinaryOperator node) throws ParseException {}

}
