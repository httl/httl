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
import httl.spi.resolvers.ServletResolver;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ServletInterceptor. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setInterceptor(Interceptor)
 * @see httl.spi.translators.InterpretedTranslator#setInterceptor(Interceptor)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ServletInterceptor extends FirstInterceptor {

	@Override
	protected void doRender(Context context, Listener listener)
			throws IOException, ParseException {
		if (ServletResolver.getRequest() == null) {
			Object request = context.get("request");
			Object response = context.get("response");
			if (request instanceof HttpServletRequest
					&& response instanceof HttpServletResponse) {
				ServletResolver.set((HttpServletRequest) request, (HttpServletResponse) response);
				try {
					listener.render(context);
				} finally {
					ServletResolver.remove();
				}
				return;
			}
		}
		listener.render(context);
	}

}