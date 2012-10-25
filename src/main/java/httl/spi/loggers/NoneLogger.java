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

import httl.spi.Logger;

import java.io.Serializable;


/**
 * NoneLogger
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class NoneLogger implements Logger, Serializable {

	private static final long serialVersionUID = 1L;

	public NoneLogger(){}

	public void debug(String msg) {
	}

	public void debug(String msg, Throwable e) {
	}

	public void error(String msg) {
	}

	public void error(String msg, Throwable e) {
	}

	public void info(String msg) {
	}

	public void info(String msg, Throwable e) {
	}

	public boolean isDebugEnabled() {
		return false;
	}

	public boolean isErrorEnabled() {
		return false;
	}

	public boolean isFatalEnabled() {
		return false;
	}

	public boolean isInfoEnabled() {
		return false;
	}

	public boolean isWarnEnabled() {
		return false;
	}

	public void warn(String msg) {
	}

	public void warn(String msg, Throwable e) {
	}

}
