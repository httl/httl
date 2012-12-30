package httl.spi.locators;

import httl.spi.Locator;
import httl.util.LocaleUtils;

import java.util.Locale;

public class LocaleLocator implements Locator {

	public String root(String suffix) {
        return null;
	}

	public String relocate(String name, Locale locale) {
		if (name == null || name.length() == 0) {
    		throw new IllegalArgumentException("resource name == null");
    	}
		return LocaleUtils.appendLocale(name, locale);
	}

}