package httl.spi.switchers;

import httl.spi.Filter;

public class ScriptValueSwitcher extends ScriptSwitcher {

	/**
     * httl.properties: script.value.filter=httl.spi.filters.ScriptValueFilter
     */
	public void setScriptValueFilter(Filter filter) {
		setScriptFilter(filter);
	}

}
