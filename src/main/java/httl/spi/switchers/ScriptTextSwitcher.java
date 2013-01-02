package httl.spi.switchers;

import httl.spi.Filter;

public class ScriptTextSwitcher extends ScriptSwitcher {

	/**
     * httl.properties: script.text.filter=httl.spi.filters.ScriptTextFilter
     */
	public void setScriptTextFilter(Filter filter) {
		setScriptFilter(filter);
	}

}
