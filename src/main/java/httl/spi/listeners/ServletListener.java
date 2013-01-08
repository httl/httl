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
package httl.spi.listeners;

import httl.Context;
import httl.spi.Listener;
import httl.spi.resolvers.ServletResolver;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ServletListener. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ServletListener implements Listener {

	public void render(Context context) throws IOException, ParseException {
		if (! context.getTemplate().isMacro() 
				&& ServletResolver.getRequest() == null) {
			Object request = context.get(ServletResolver.REQUEST_KEY);
			if (request instanceof HttpServletRequest) {
				ServletResolver.setRequest((HttpServletRequest) request);
				Object response = context.get(ServletResolver.RESPONSE_KEY);
				if (response instanceof HttpServletResponse) {
					ServletResolver.setResponse((HttpServletResponse) response);
				}
			}
		}
	}

}
