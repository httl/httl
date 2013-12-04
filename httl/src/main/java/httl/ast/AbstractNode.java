package httl.ast;

import httl.Node;
import httl.Visitor;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public abstract class AbstractNode implements Node {

	private final int offset;

	private Node parent;

	public AbstractNode(int offset) {
		this.offset = offset;
	}

	public void accept(Visitor visitor) throws IOException, ParseException {
		visitor.visit(this);
	}

	public int getOffset() {
		return offset;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) throws ParseException {
		if (this.parent != null)
			throw new ParseException("Can not modify parent.", getOffset());
		this.parent = parent;
	}

	public List<Node> getChildren() {
		return null;
	}

}