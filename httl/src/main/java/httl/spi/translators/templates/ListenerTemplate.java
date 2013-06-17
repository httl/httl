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
package httl.spi.translators.templates;

import httl.Context;
import httl.Template;
import httl.spi.Listener;

import java.io.IOException;
import java.text.ParseException;

/**
 * ListenerTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ListenerTemplate extends ProxyTemplate {

	private final Listener listener;

	public ListenerTemplate(Template template, Listener listener) {
		super(template);
		this.listener = listener;
	}

	@Override
	public void render(Object parameters, Object out)
			throws IOException, ParseException {
		listener.render(Context.getContext());
	}

}