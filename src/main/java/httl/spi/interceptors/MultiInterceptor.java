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
package httl.spi.interceptors;

import httl.Context;
import httl.spi.Interceptor;
import httl.spi.Listener;
import httl.internal.util.Reqiured;

import java.io.IOException;
import java.text.ParseException;

/**
 * MultiInterceptor. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setInterceptor(Interceptor)
 * @see httl.spi.translators.InterpretedTranslator#setInterceptor(Interceptor)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiInterceptor implements Interceptor {

	private static final String LISTENER_KEY = "__listener__";

	private Listener chain;

	/**
	 * httl.properties: interceptors=httl.spi.interceptors.ExtendsInterceptor
	 */
	@Reqiured
	public void setInterceptors(Interceptor[] interceptors) {
		Listener last = null;
		for (int i = interceptors.length - 1; i >= 0; i--) {
			final Interceptor current = interceptors[i];
			final Listener next = last;
			last = new Listener() {
				public void render(Context context) throws IOException, ParseException {
					if (next == null) {
						Listener listener = (Listener) context.get(LISTENER_KEY);
						if (listener != null) {
							current.render(context, listener);
						}
					} else {
						current.render(context, next);
					}
				}
			};
		}
		this.chain = last;
	}

	public void render(Context context, Listener listener)
			throws IOException, ParseException {
		if (chain != null) {
			Object old = context.put(LISTENER_KEY, listener);
			try {
				chain.render(context);
			} finally {
				if ( old != null) {
					context.put(LISTENER_KEY, old);
				} else {
					context.remove(LISTENER_KEY);
				}
			}
		} else {
			listener.render(context);
		}
	}

}