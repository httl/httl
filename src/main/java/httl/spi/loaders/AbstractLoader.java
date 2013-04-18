/*
 * Copyright 2011-2013 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.spi.loaders;

import httl.Engine;
import httl.Resource;
import httl.internal.util.CollectionUtils;
import httl.internal.util.LocaleUtils;
import httl.internal.util.StringUtils;
import httl.internal.util.UrlUtils;
import httl.spi.Loader;
import httl.spi.Logger;
import httl.spi.loaders.resources.InputStreamResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * AbstractLoader. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLoader(Loader)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractLoader implements Loader {
	
	private Engine engine;

	private Logger logger;
	
	private String encoding;

	private boolean reloadable;

	private volatile boolean first = true;

	private String[] templateDirectory;

	private String[] templateSuffix;

	private String[] messageDirectory;

	private String messageBasename;

	private String[] messageSuffix;

	/**
	 * httl.properties: template.directory=/META-INF/templates
	 */
	public void setTemplateDirectory(String[] directory) {
		this.templateDirectory = UrlUtils.cleanDirectory(directory);
	}

	/**
	 * httl.properties: template.suffix=.httl
	 */
	public void setTemplateSuffix(String[] suffix) {
		this.templateSuffix = suffix;
	}

	/**
	 * httl.properties: message.directory=/META-INF/messages
	 */
	public void setMessageDirectory(String[] directory) {
		this.messageDirectory = UrlUtils.cleanDirectory(directory);
	}

	/**
	 * httl.properties: message.basename=messages
	 */
	public void setMessageBasename(String messageBasename) {
		this.messageBasename = UrlUtils.cleanName(messageBasename);
	}

	/**
	 * httl.properties: message.suffix=.properties
	 */
	public void setMessageSuffix(String[] suffix) {
		this.messageSuffix = suffix;
	}

	/**
	 * httl.properties: engine=httl.spi.engines.DefaultEngine
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/**
	 * httl.properties: loggers=httl.spi.loggers.Log4jLogger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * httl.properties: reloadable=true
	 */
	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}

	/**
	 * httl.properties: input.encoding=UTF-8
	 */
	public void setInputEncoding(String encoding) {
		if (StringUtils.isNotEmpty(encoding)) {
			Charset.forName(encoding);
			this.encoding = encoding;
		}
	}

	protected Engine getEngine() {
		return engine;
	}

	protected Logger getLogger() {
		return logger;
	}

	protected String getEncoding() {
		return encoding;
	}

	protected String relocate(String name, Locale locale, String[] directories) {
		if (CollectionUtils.isNotEmpty(directories) ) {
			for (String directory : directories) {
				try {
					if (doExists(name, locale, directory + name)) {
						return name = directory + name;
					}
				} catch (IOException e) {
					continue;
				}
			}
			return directories[0] + name;
		}
		return name;
	}

	protected String toPath(String name, Locale locale) {
		if (StringUtils.endsWith(name, templateSuffix)) {
			name = relocate(name, locale, templateDirectory);
		} else if (StringUtils.isNotEmpty(messageBasename) 
				&& name.startsWith(messageBasename)
				&& StringUtils.endsWith(name, messageSuffix)) {
			name = relocate(name, locale, messageDirectory);
		}
		return LocaleUtils.appendLocale(name, locale);
	}

	public List<String> list(String suffix) throws IOException {
		String[] directories;
		if (StringUtils.endsWith(suffix, templateSuffix)) {
			directories = templateDirectory;
		} else if (StringUtils.endsWith(suffix, messageSuffix)) {
			directories = messageDirectory;
		} else {
			directories = null;
		}
		if (CollectionUtils.isEmpty(directories)) {
			directories = new String[] { "/" };
		}
		List<String> result = new ArrayList<String>();
		for (String directory : directories) {
			List<String> list = doList(directory, suffix);
			if (CollectionUtils.isNotEmpty(list)) {
				for (String name : list) {
					if (StringUtils.isNotEmpty(name)) {
						result.add(UrlUtils.cleanName(name));
					}
				}
			}
		}
		return result;
	}

	public boolean exists(String name, Locale locale) {
		Locale cur = locale;
		while (cur != null) {
			if (_exists(name, locale, toPath(name, cur))) {
				return true;
			}
			cur = LocaleUtils.getParentLocale(cur);
		}
		return _exists(name, locale, toPath(name, null));
	}

	private boolean _exists(String name, Locale locale, String path) {
		try {
			return doExists(name, locale, path);
		} catch (Exception e) {
			return false;
		}
	}

	public Resource load(String name, Locale locale, String encoding) throws IOException {
		if (StringUtils.isEmpty(encoding)) {
			encoding = this.encoding;
		}
		Locale cur = locale;
		String path = toPath(name, cur);
		while (cur != null && ! _exists(name, locale, path)) {
			cur = LocaleUtils.getParentLocale(cur);
			path = toPath(name, cur);
		}
		Resource resource = doLoad(name, locale, encoding, path);
		logResourceDirectory(resource);
		return resource;
	}
	
	private void logResourceDirectory(Resource resource) {
		if (first) {
			first = false;
			if (logger != null && logger.isInfoEnabled()
					&& resource instanceof InputStreamResource) {
				File file = ((InputStreamResource) resource).getFile();
				if (file != null && file.exists()) {
					String uri = resource.getName().replace('\\', '/');
					String abs = file.getAbsolutePath().replace('\\', '/');
					if (abs.endsWith(uri)) {
						abs = abs.substring(0, abs.length() - uri.length());
					} else {
						int i = abs.lastIndexOf('/');
						if (i > 0) {
							abs = abs.substring(0, i);
						} else {
							abs = "/";
						}
					}
					logger.info("Load httl template from" + (reloadable ? " RELOADABLE" : "") + " directory " + abs + " by " + getClass().getSimpleName() + ".");
				}
			}
		}
	}

	protected abstract List<String> doList(String directory, String suffix) throws IOException;

	protected abstract boolean doExists(String name, Locale locale, String path) throws IOException;

	protected abstract Resource doLoad(String name, Locale locale, String encoding, String path) throws IOException;

}