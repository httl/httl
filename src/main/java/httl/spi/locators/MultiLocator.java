package httl.spi.locators;

import java.util.Locale;

import httl.spi.Locator;

public class MultiLocator implements Locator {
	
	private Locator[] locators;

	public void setLocators(Locator[] locators) {
		this.locators = locators;
	}

	public String root(String suffix) {
		if (locators == null) {
			return null;
		}
		if (locators.length == 1) {
			return locators[0].root(suffix);
		}
		for (Locator localor : locators) {
			String root = localor.root(suffix);
			if (root != null) {
				return root;
			}
		}
		return null;
	}

	public String relocate(String path, Locale locale) {
		if (locators == null) {
			return path;
		}
		if (locators.length == 1) {
			return locators[0].relocate(path, locale);
		}
		for (Locator localor : locators) {
			path = localor.relocate(path, locale);
		}
		return path;
	}

}
