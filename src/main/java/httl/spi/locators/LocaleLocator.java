package httl.spi.locators;

import httl.spi.Locator;

import java.util.Locale;

public class LocaleLocator implements Locator {

	public String root(String suffix) {
        return null;
	}

	public String relocate(String name, Locale locale) {
		if (name == null || name.length() == 0) {
    		throw new IllegalArgumentException("resource name == null");
    	}
		if (locale == null) {
			return name;
		}
		int i = name.lastIndexOf('.');
    	return i < 0 ? name + "_" + locale.toString() :
    		name.substring(0, i) + "_" + locale.toString() + name.substring(i);
	}

}