package httl.spi.switchers;

import java.util.Arrays;
import java.util.List;

import httl.spi.Switcher;
import httl.spi.Filter;

public class ScriptSwitcher implements Switcher {

	private static final String START_TAG = "<script";

	private static final String END_TAG = "</script>";

	private List<String> scriptLocations = Arrays.asList(new String[] {START_TAG, END_TAG});

	private Filter scriptFilter;

	/**
	 * httl.properties: script.locations=&lt;script,&lt;/script&gt;
	 */
	public void setScriptLocations(String[] locations) {
		this.scriptLocations = Arrays.asList(locations);
	}

	/**
	 * httl.properties: script.filter=httl.spi.filters.ScriptFilter
	 */
	public void setScriptFilter(Filter filter) {
		this.scriptFilter = filter;
	}

	public List<String> locations() {
		return scriptLocations;
	}

	public Filter enter(String location, Filter defaultFilter) {
		if (START_TAG.equals(location)) {
			return scriptFilter;
		}
		return defaultFilter;
	}

}
