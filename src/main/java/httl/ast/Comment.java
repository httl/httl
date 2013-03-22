package httl.ast;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

public class Comment extends Directive {

	private String content;

	private boolean block;

	public Comment(String content, boolean block, int offset) {
		super(offset);
		this.content = content;
		this.block = block;
	}

	public void render(Map<String, Object> context, Object out) throws IOException,
			ParseException {
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isBlock() {
		return block;
	}

	public void setBlock(boolean block) {
		this.block = block;
	}

	@Override
	public String toString() {
		return block ? "#[" + content + "]#" : "##" + content + "\n";
	}
}