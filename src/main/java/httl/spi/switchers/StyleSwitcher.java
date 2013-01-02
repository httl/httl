package httl.spi.switchers;

import java.util.Arrays;
import java.util.List;

import httl.spi.Switcher;
import httl.spi.Filter;

public class StyleSwitcher implements Switcher {

	private static final String START_TAG = "<style";

	private static final String END_TAG = "</style>";

	private List<String> styleLocations = Arrays.asList(new String[] {START_TAG, END_TAG});

	private Filter styleFilter;

	/**
     * httl.properties: style.locations=&lt;style,&lt;/style&gt;
     */
	public void setStyleLocations(String[] locations) {
		this.styleLocations = Arrays.asList(locations);
	}

	/**
     * httl.properties: style.filter=httl.spi.filters.StyleFilter
     */
	public void setStyleFilter(Filter filter) {
		this.styleFilter = filter;
	}

	public List<String> locations() {
		return styleLocations;
	}

	public Filter enter(String location, Filter defaultFilter) {
		if (START_TAG.equals(location)) {
			return styleFilter;
		}
		return defaultFilter;
	}

}
