/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.locators;

import httl.spi.Locator;
import httl.util.UrlUtils;

import java.util.Locale;

/**
 * DirectoryLocator. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.loaders.AbstractLoader#setLocator(Locator)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DirectoryLocator implements Locator {

	private String templateDirectory;

	private String templateSuffix;

	private String messageDirectory;

	private String messageSuffix;

	/**
	 * httl.properties: template.directory=/META-INF/templates
	 */
	public void setTemplateDirectory(String directory) {
		this.templateDirectory = UrlUtils.cleanDirectory(directory);
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
		this.messageDirectory = UrlUtils.cleanDirectory(directory);
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
