package httl.spi.switchers;

import httl.spi.Filter;

public class StyleValueSwitcher extends StyleSwitcher {

	/**
     * httl.properties: style.value.filter=httl.spi.filters.StyleValueFilter
     */
	public void setStyleValueFilter(Filter filter) {
		setStyleFilter(filter);
	}

}
