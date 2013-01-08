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
package httl.spi.parsers.templates;

import httl.Context;
import httl.Template;
import httl.spi.Listener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

/**
 * ListenerTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ListenerTemplate extends TemplateWrapper {

	private final Listener listener;

	public ListenerTemplate(Template template, Listener listener) {
		super(template);
		this.listener = listener;
	}

	@Override
	public void render(Map<String, Object> parameters, OutputStream stream)
			throws IOException, ParseException {
		Context context = Context.getContext();
		if (context.getOut() != stream) {
			context = Context.pushContext(this, parameters, stream);
			try {
				listener.render(context);
			} finally {
				Context.popContext();
			}
		} else {
			listener.render(context);
		}
	}

	@Override
	public void render(Map<String, Object> parameters, Writer writer)
			throws IOException, ParseException {
		Context context = Context.getContext();
		if (context.getOut() != writer) {
			context = Context.pushContext(this, parameters, writer);
			try {
				listener.render(context);
			} finally {
				Context.popContext();
			}
		} else {
			listener.render(context);
		}
	}

}