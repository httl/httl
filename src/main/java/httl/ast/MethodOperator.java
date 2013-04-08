package httl.ast;

import httl.internal.util.StringUtils;

public class MethodOperator extends BinaryOperator {

	public MethodOperator(String name, int priority, int offset) {
		super(name, priority, offset);
	}

	@Override
	public String toString() {
		String parameter = getRightParameter().toString();
		return getLeftParameter() + "." + getName() + (StringUtils.isEmpty(parameter) ? "" : "(" + getRightParameter() + ")");
	}

}
