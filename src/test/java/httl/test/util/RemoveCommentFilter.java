package httl.test.util;

import httl.spi.filters.AbstractFilter;

public class RemoveCommentFilter extends AbstractFilter {

	public String filter(String key, String value) {
		if (key.endsWith("/comment.httl") || key.endsWith("/comment_cdata_escape.httl")) {
			return value;
		}
		return value.replace("<!--", "").replace("-->", "");
	}

}
