package httl.test.util;

import httl.spi.filters.AbstractFilter;

public class RemoveCommentFilter extends AbstractFilter {

	public String filter(String key, String value) {
		return value.replace("<!--", "").replace("-->", "");
	}

}
