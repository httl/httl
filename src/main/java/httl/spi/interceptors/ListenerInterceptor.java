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
package httl.spi.interceptors;

import httl.Context;
import httl.spi.Interceptor;
import httl.spi.Listener;
import httl.spi.Logger;
import httl.spi.Rendition;

import java.io.IOException;
import java.text.ParseException;

/**
 * Listener Interceptor. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.parsers.AbstractParser#setInterceptor(Interceptor)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ListenerInterceptor implements Interceptor {

	private Listener listener;

	private Logger logger;

	/**
	 * httl.properties: listeners=httl.spi.listeners.ExtendsListener
	 */
	public void setListener(Listener listener) {
		this.listener = listener;
	}

	/**
	 * httl.properties: loggers=httl.spi.loggers.Log4jListener
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void render(Context context, Rendition rendition) throws IOException, ParseException {
		if (listener != null) {
			try {
				listener.rendering(context);
			} catch (Exception e) {
				if (logger != null && logger.isErrorEnabled()) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		try {
			rendition.render(context);
		} finally {
			if (listener != null) {
				try {
					listener.rendered(context);
				} catch (Exception e) {
					if (logger != null && logger.isErrorEnabled()) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}

}
