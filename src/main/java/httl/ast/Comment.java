package httl.ast;

public class Comment extends Statement {

	private final String content;

	private final boolean block;

	public Comment(String content, boolean block, int offset) {
		super(offset);
		this.content = content;
		this.block = block;
	}

	public String getContent() {
		return content;
	}

	public boolean isBlock() {
		return block;
	}

	@Override
	public String toString() {
		return block ? "#[" + content + "]#" : "##" + content + "\n";
	}
}