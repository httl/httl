package httl.spi.switchers;

import httl.spi.Filter;

public class StyleTextSwitcher extends StyleSwitcher {

	/**
     * httl.properties: style.text.filter=httl.spi.filters.StyleTextFilter
     */
	public void setStyleTextFilter(Filter filter) {
		setStyleFilter(filter);
	}

}
