package httl.test.util;

import httl.spi.filters.AbstractFilter;

public class ArrayToListFilter extends AbstractFilter {

	public String filter(String key, String value) {
		return value.replace("Book[]", "List<Map>").replace("Book", "Map").replace("User", "Map");
	}

}
