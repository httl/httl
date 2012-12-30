package httl.spi.locators;

import httl.spi.Locator;
import httl.util.UrlUtils;

import java.util.Locale;

public class DirectoryLocator implements Locator {

    private String templateDirectory;

    private String templateSuffix;

    private String messageDirectory;

    private String messageSuffix;

    /**
	 * httl.properties: template.directory=/META-INF/templates
	 */
    public void setTemplateDirectory(String directory) {
    	if (directory != null && directory.length() > 0) {
            this.templateDirectory = UrlUtils.cleanDirectory(directory);
        }
    }

    /**
	 * httl.properties: template.suffix=.httl
	 */
    public void setTemplateSuffix(String suffix) {
    	this.templateSuffix = suffix;
    }

    /**
	 * httl.properties: message.directory=/META-INF/messages
	 */
    public void setMessageDirectory(String directory) {
    	if (directory != null && directory.length() > 0) {
            this.messageDirectory = UrlUtils.cleanDirectory(directory);
        }
    }

    /**
	 * httl.properties: message.suffix=.properties
	 */
    public void setMessageSuffix(String suffix) {
    	this.messageSuffix = suffix;
    }

	public String root(String suffix) {
		if (templateDirectory != null && templateSuffix != null && templateSuffix.equals(suffix)) {
			return templateDirectory;
		} else if (messageDirectory != null && messageSuffix != null && messageSuffix.equals(suffix)) {
			return templateDirectory;
		}
		return null;
	}

	public String relocate(String name, Locale locale) {
		if (name == null || name.length() == 0) {
    		throw new IllegalArgumentException("resource name == null");
    	}
    	if (templateDirectory != null && templateSuffix != null && name.endsWith(templateSuffix)) {
			return templateDirectory + name;
		} else if (messageDirectory != null && messageSuffix != null && name.endsWith(messageSuffix)) {
			return messageDirectory + name;
		} else {
			return name;
		}
	}

}
