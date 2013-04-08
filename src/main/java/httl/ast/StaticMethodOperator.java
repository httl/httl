package httl.ast;

public class StaticMethodOperator extends UnaryOperator {

	public StaticMethodOperator(String name, int priority, int offset) {
		super(name, priority, offset);
	}

	@Override
	public String toString() {
		return getName() + "(" + getParameter() + ")";
	}

}
