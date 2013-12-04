package httl.ast;

import httl.Node;

import java.text.ParseException;
import java.util.List;

public class Parameters extends Expression {

	private List<Expression> parameters;

	public Parameters(int offset) {
		super(offset);
	}

	public List<Expression> getParameters() {
		return parameters;
	}

	public void setParameters(List<Expression> parameters) throws ParseException {
		if (this.parameters != null)
			throw new ParseException("Can not modify parameters.", getOffset());
		this.parameters = parameters;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Node> getChildren() {
		return (List) parameters;
	}

}
