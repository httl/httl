/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.loggers;

import httl.spi.Constants;
import httl.spi.Logger;

import java.io.Serializable;


/**
 * SimpleLogger.
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class SimpleLogger implements Logger, Serializable {

	private static final long serialVersionUID = 1L;

	private static final String prefix = "[" + Constants.HTTL + "] ";

	private String getMessage(String msg) {
		if (prefix == null)
			return msg;
		return prefix + msg;
	}

	public void debug(String msg) {
		System.out.println(getMessage(msg));
	}

	public void debug(String msg, Throwable e) {
		System.out.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
	}

	public void info(String msg) {
		System.out.println(getMessage(msg));
	}

	public void info(String msg, Throwable e) {
		System.out.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
	}

	public void warn(String msg) {
		System.err.println(getMessage(msg));
	}

	public void warn(String msg, Throwable e) {
		System.err.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
	}

	public void error(String msg) {
		System.err.println(getMessage(msg));
	}

	public void error(String msg, Throwable e) {
		System.err.println(getMessage(msg));
		if (e != null)
			e.printStackTrace();
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
