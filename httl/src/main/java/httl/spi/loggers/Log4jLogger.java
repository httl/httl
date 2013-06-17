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
package httl.spi.loggers;

import httl.spi.Logger;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

/**
 * Log4jLogger. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLogger(Logger)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Log4jLogger implements Logger {

	private final org.apache.log4j.Logger logger = LogManager.getLogger(NAME);

	private String FQCN = Log4jLogger.class.getName();

	public void setLogger(String name) {
		this.FQCN = name;
	}

	/**
	 * httl.properties: logger.level=DEBUG
	 */
	public void setLoggerLevel(String level) {
		logger.setLevel(Level.toLevel(level.toUpperCase()));
	}

	public void trace(String msg) {
		logger.log(FQCN, Level.TRACE, msg, null);
	}

	public void trace(Throwable e) {
		logger.log(FQCN, Level.TRACE, e == null ? null : e.getMessage(), e);
	}

	public void trace(String msg, Throwable e) {
		logger.log(FQCN, Level.TRACE, msg, e);
	}

	public void debug(String msg) {
		logger.log(FQCN, Level.DEBUG, msg, null);
	}

	public void debug(Throwable e) {
		logger.log(FQCN, Level.DEBUG, e == null ? null : e.getMessage(), e);
	}

	public void debug(String msg, Throwable e) {
		logger.log(FQCN, Level.DEBUG, msg, e);
	}

	public void info(String msg) {
		logger.log(FQCN, Level.INFO, msg, null);
	}

	public void info(Throwable e) {
		logger.log(FQCN, Level.INFO, e == null ? null : e.getMessage(), e);
	}

	public void info(String msg, Throwable e) {
		logger.log(FQCN, Level.INFO, msg, e);
	}

	public void warn(String msg) {
		logger.log(FQCN, Level.WARN, msg, null);
	}

	public void warn(Throwable e) {
		logger.log(FQCN, Level.WARN, e == null ? null : e.getMessage(), e);
	}

	public void warn(String msg, Throwable e) {
		logger.log(FQCN, Level.WARN, msg, e);
	}

	public void error(String msg) {
		logger.log(FQCN, Level.ERROR, msg, null);
	}

	public void error(Throwable e) {
		logger.log(FQCN, Level.ERROR, e == null ? null : e.getMessage(), e);
	}

	public void error(String msg, Throwable e) {
		logger.log(FQCN, Level.ERROR, msg, e);
	}

	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	public boolean isWarnEnabled() {
		return logger.isEnabledFor(Level.WARN);
	}
	
	public boolean isErrorEnabled() {
		return logger.isEnabledFor(Level.ERROR);
	}

}