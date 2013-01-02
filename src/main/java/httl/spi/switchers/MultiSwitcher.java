package httl.spi.switchers;

import httl.spi.Switcher;
import httl.spi.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiSwitcher implements Switcher {

    private Switcher[] switchers;

    /**
     * httl.properties: switchers=httl.spi.switchers.ScriptSwitcher
     */
    public void setSwitchers(Switcher[] switchers) {
    	if (switchers != null && switchers.length > 0 
    			&& this.switchers != null && this.switchers.length > 0) {
    		Switcher[] oldSwitchers = this.switchers;
    		this.switchers = new Switcher[oldSwitchers.length + switchers.length];
    		System.arraycopy(oldSwitchers, 0, this.switchers, 0, oldSwitchers.length);
    		System.arraycopy(switchers, 0, this.switchers, oldSwitchers.length, switchers.length);
    	} else {
    		this.switchers = switchers;
    	}
    }

	@SuppressWarnings("unchecked")
	public List<String> locations() {
		if (switchers == null || switchers.length == 0) {
    		return Collections.EMPTY_LIST;
    	}
    	if (switchers.length == 1) {
    		return switchers[0].locations();
    	}
    	List<String> locations = new ArrayList<String>();
        for (Switcher switcher : switchers) {
        	locations.addAll(switcher.locations());
        }
        return locations;
	}

    public Filter enter(String location, Filter defaultFilter) {
    	if (switchers == null || switchers.length == 0) {
    		return defaultFilter;
    	}
    	if (switchers.length == 1) {
    		return switchers[0].enter(location, defaultFilter);
    	}
        for (Switcher switcher : switchers) {
        	if (switcher.locations().contains(location)) {
        		return switcher.enter(location, defaultFilter);
        	}
        }
        return defaultFilter;
    }

}
