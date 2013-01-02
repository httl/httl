package httl.spi.switchers;

import httl.spi.Switcher;

public class MultiValueSwitcher extends MultiSwitcher {

    /**
     * httl.properties: value.switchers=httl.spi.switchers.JavascriptSwitcher
     */
	public void setValueSwitchers(Switcher[] switchers) {
		setSwitchers(switchers);
	}

}
