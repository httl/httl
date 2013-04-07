package httl.ast;

import httl.Visitor;

import java.io.IOException;
import java.text.ParseException;

public abstract class Directive extends Statement {

	public Directive(int offset) {
		super(offset);
	}

	public void accept(Visitor visitor) throws IOException, ParseException {
		Expression expression = getExpression();
		if (expression != null) {
			expression.accept(visitor);
		}
		visitor.visit(this);
	}

	protected Expression getExpression() {
		return null;
	}

}
