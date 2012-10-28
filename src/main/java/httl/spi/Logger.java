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
package httl.spi;

/**
 * Logger. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Logger {

    public static final String NAME = "httl";

	/**
	 * debug.
	 * 
	 * @param msg
	 */
	public void debug(String msg);

	/**
	 * debug.
	 * 
	 * @param msg
	 * @param e
	 */
	public void debug(String msg, Throwable e);

	/**
	 * info.
	 * 
	 * @param msg
	 */
	public void info(String msg);

	/**
	 * info.
	 * 
	 * @param msg
	 * @param e
	 */
	public void info(String msg, Throwable e);

	/**
	 * warn.
	 * 
	 * @param msg
	 */
	public void warn(String msg);

	/**
	 * warn.
	 * 
	 * @param msg
	 * @param e
	 */
	public void warn(String msg, Throwable e);

	/**
	 * error.
	 * 
	 * @param msg
	 */
	public void error(String msg);

	/**
	 * error.
	 * 
	 * @param msg
	 * @param e
	 */
	public void error(String msg, Throwable e);

	/**
	 * isDebugEnabled.
	 * 
	 * @return debug enabled
	 */
	public boolean isDebugEnabled();

	/**
	 * isInfoEnabled.
	 * 
	 * @return info enabled
	 */
	public boolean isInfoEnabled();

	/**
	 * isWarnEnabled.
	 * 
	 * @return warn enabled
	 */
	public boolean isWarnEnabled();

	/**
	 * isErrorEnabled.
	 * 
	 * @return error nabled
	 */
	public boolean isErrorEnabled();

}
