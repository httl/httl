package httl.ast;

public class ListOperator extends UnaryOperator {

	public ListOperator(String name, int priority, int offset) {
		super(name, priority, offset);
	}

	@Override
	public String toString() {
		return "[" + getParameter() + "]";
	}

}
