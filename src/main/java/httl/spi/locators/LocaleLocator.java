package httl.spi.locators;

import httl.spi.Locator;

import java.util.Locale;

public class LocaleLocator implements Locator {

	private String templateSuffix;

	private String messageSuffix;
	
	private boolean templateLocalized;

	private boolean messageLocalized;

	/**
	 * httl.properties: template.suffix=.httl
	 */
	public void setTemplateSuffix(String templateSuffix) {
		this.templateSuffix = templateSuffix;
	}

	/**
	 * httl.properties: message.suffix=.properties
	 */
	public void setMessageSuffix(String messageSuffix) {
		this.messageSuffix = messageSuffix;
	}

	/**
	 * httl.properties: template.localized=true
	 */
	public void setTemplateLocalized(boolean templateLocalized) {
		this.templateLocalized = templateLocalized;
	}

	/**
	 * httl.properties: message.localized=true
	 */
	public void setMessageLocalized(boolean messageLocalized) {
		this.messageLocalized = messageLocalized;
	}

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
		if ((templateLocalized && templateSuffix != null && name.endsWith(templateSuffix)) 
				|| (messageLocalized && messageSuffix != null && name.endsWith(messageSuffix))) {
			int i = name.lastIndexOf('.');
	    	return i < 0 ? name + "_" + locale.toString() :
	    		name.substring(0, i) + "_" + locale.toString() + name.substring(i);
		}
		return name;
	}

}