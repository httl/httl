/*
 * Copyright 2011-2012 HTTL Team.
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

import java.io.IOException;
import java.text.ParseException;

public class MultiInterceptor implements Interceptor {

	private static final String RENDITION_KEY = "__rendition__";

	private Listener interceptorRendition;

	public void setInterceptors(Interceptor[] interceptors) {
		Listener last = null;
		for (int i = interceptors.length - 1; i >= 0; i--) {
			final Interceptor current = interceptors[i];
			final Listener next = last;
			last = new Listener() {
				public void render(Context context) throws IOException, ParseException {
					if (next == null) {
						Listener rendition = (Listener) context.get(RENDITION_KEY);
						if (rendition != null) {
							current.render(context, rendition);
						}
					} else {
						current.render(context, next);
					}
				}
			};
		}
		this.interceptorRendition = last;
	}

	public void render(Context context, Listener rendition)
			throws IOException, ParseException {
		if (interceptorRendition != null) {
			Object old = context.put(RENDITION_KEY, rendition);
			try {
				interceptorRendition.render(context);
			} finally {
				if ( old != null) {
					context.put(RENDITION_KEY, old);
				} else {
					context.remove(RENDITION_KEY);
				}
			}
		} else {
			rendition.render(context);
		}
	}

}