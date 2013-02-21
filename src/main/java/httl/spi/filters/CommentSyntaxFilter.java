package httl.spi.filters;

import httl.internal.util.Reqiured;

public class CommentSyntaxFilter extends AbstractFilter {

	protected String commentLeft;

	protected String commentRight;

	@Reqiured
	public void setCommentLeft(String commentLeft) {
		this.commentLeft = commentLeft;
	}

	@Reqiured
	public void setCommentRight(String commentRight) {
		this.commentRight = commentRight;
	}

	public String filter(String key, String value) {
		if (value.startsWith(commentRight)) {
			value = value.substring(commentRight.length());
		}
		if (value.endsWith(commentLeft)) {
			value = value.substring(0, value.length() - commentLeft.length());
		}
		return value;
	}

}
