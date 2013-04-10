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
package httl.spi;

/**
 * Output Logger. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Logger {

	/**
	 * The httl log name space.
	 */
	public static final String NAME = "httl";

	/**
	 * trace.
	 * 
	 * @param msg
	 */
	void trace(String msg);

	/**
	 * trace.
	 * 
	 * @param e
	 */
	void trace(Throwable e);

	/**
	 * trace.
	 * 
	 * @param msg
	 * @param e
	 */
	void trace(String msg, Throwable e);

	/**
	 * debug.
	 * 
	 * @param msg
	 */
	void debug(String msg);

	/**
	 * debug.
	 * 
	 * @param e
	 */
	void debug(Throwable e);

	/**
	 * debug.
	 * 
	 * @param msg
	 * @param e
	 */
	void debug(String msg, Throwable e);

	/**
	 * info.
	 * 
	 * @param msg
	 */
	void info(String msg);

	/**
	 * info.
	 * 
	 * @param e
	 */
	void info(Throwable e);

	/**
	 * info.
	 * 
	 * @param msg
	 * @param e
	 */
	void info(String msg, Throwable e);

	/**
	 * warn.
	 * 
	 * @param msg
	 */
	void warn(String msg);

	/**
	 * warn.
	 * 
	 * @param e
	 */
	void warn(Throwable e);

	/**
	 * warn.
	 * 
	 * @param msg
	 * @param e
	 */
	void warn(String msg, Throwable e);

	/**
	 * error.
	 * 
	 * @param msg
	 */
	void error(String msg);

	/**
	 * error.
	 * 
	 * @param e
	 */
	void error(Throwable e);

	/**
	 * error.
	 * 
	 * @param msg
	 * @param e
	 */
	void error(String msg, Throwable e);

	/**
	 * isDebugEnabled.
	 * 
	 * @return debug enabled
	 */
	boolean isTraceEnabled();

	/**
	 * isDebugEnabled.
	 * 
	 * @return debug enabled
	 */
	boolean isDebugEnabled();

	/**
	 * isInfoEnabled.
	 * 
	 * @return info enabled
	 */
	boolean isInfoEnabled();

	/**
	 * isWarnEnabled.
	 * 
	 * @return warn enabled
	 */
	boolean isWarnEnabled();

	/**
	 * isErrorEnabled.
	 * 
	 * @return error nabled
	 */
	boolean isErrorEnabled();

}