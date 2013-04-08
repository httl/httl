package httl.ast;

public class IndexOperator extends BinaryOperator {

	public IndexOperator(String name, int priority, int offset) {
		super(name, priority, offset);
	}

	@Override
	public String toString() {
		return getLeftParameter() + "[" + getRightParameter() + "]";
	}

}
