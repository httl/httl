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

import httl.Context;

import java.io.IOException;
import java.text.ParseException;

/**
 * Render Listener. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.interceptors.ListenerInterceptor#setListener(Listener)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Listener {

	/**
	 * On template rendering.
	 * 
	 * @param context - render context
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void rendering(Context context) throws IOException, ParseException;

	/**
	 * On template rendered.
	 * 
	 * @param context - render context
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void rendered(Context context) throws IOException, ParseException;

}
