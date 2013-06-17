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

/**
 * SimpleLogger. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setLogger(Logger)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class SimpleLogger implements Logger {

	private static final String PREFIX = "[" + NAME + "] ";

	private static final int TRACE = -2;

	private static final int DEBUG = -1;

	private static final int INFO = 0;

	private static final int WARN = 1;

	private static final int ERROR = 2;

	private int level = INFO;

	/**
	 * httl.properties: logger.level=DEBUG
	 */
	public void setLoggerLevel(String level) {
		if ("TRACE".equalsIgnoreCase(level)) {
			this.level = TRACE;
		} else if ("DEBUG".equalsIgnoreCase(level)) {
			this.level = DEBUG;
		} else if ("INFO".equalsIgnoreCase(level)) {
			this.level = INFO;
		} else if ("WARN".equalsIgnoreCase(level)) {
			this.level = WARN;
		} else if ("ERROR".equalsIgnoreCase(level)) {
			this.level = ERROR;
		}
	}

	private String getMessage(String msg) {
		if (PREFIX == null)
			return msg;
		return PREFIX + msg;
	}

	public void trace(String msg) {
		if (level > TRACE)
			return;
		System.out.println(getMessage(msg));
	}

	public void trace(Throwable e) {
		if (level > TRACE)
			return;
		if (e != null)
			e.printStackTrace();
	}

	public void trace(String msg, Throwable e) {
		if (level > TRACE)
			return;
		System.out.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
	}

	public void debug(String msg) {
		if (level > DEBUG)
			return;
		System.out.println(getMessage(msg));
	}

	public void debug(Throwable e) {
		if (level > DEBUG)
			return;
		if (e != null)
			e.printStackTrace();
	}

	public void debug(String msg, Throwable e) {
		if (level > DEBUG)
			return;
		System.out.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
	}

	public void info(String msg) {
		if (level > INFO)
			return;
		System.out.println(getMessage(msg));
	}

	public void info(Throwable e) {
		if (level > INFO)
			return;
		if (e != null)
			e.printStackTrace();
	}

	public void info(String msg, Throwable e) {
		if (level > INFO)
			return;
		System.out.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
	}

	public void warn(String msg) {
		if (level > WARN)
			return;
		System.err.println(getMessage(msg));
	}

	public void warn(Throwable e) {
		if (level > WARN)
			return;
		if (e != null)
			e.printStackTrace();
	}

	public void warn(String msg, Throwable e) {
		if (level > WARN)
			return;
		System.err.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
	}

	public void error(String msg) {
		if (level > ERROR)
			return;
		System.err.println(getMessage(msg));
	}

	public void error(Throwable e) {
		if (level > ERROR)
			return;
		if (e != null)
			e.printStackTrace();
	}

	public void error(String msg, Throwable e) {
		if (level > ERROR)
			return;
		System.err.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
	}

	public boolean isTraceEnabled() {
		return true;
	}

	public boolean isDebugEnabled() {
		return true;
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public boolean isWarnEnabled() {
		return true;
	}

	public boolean isErrorEnabled() {
		return true;
	}

	public boolean isFatalEnabled() {
		return true;
	}

};