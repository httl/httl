package httl.spi.switchers;

import httl.spi.Switcher;

public class MultiTextSwitcher extends MultiSwitcher {

	/**
	 * httl.properties: text.switchers=httl.spi.switchers.JavascriptSwitcher
	 */
	public void setTextSwitchers(Switcher[] switchers) {
		setSwitchers(switchers);
	}

}
