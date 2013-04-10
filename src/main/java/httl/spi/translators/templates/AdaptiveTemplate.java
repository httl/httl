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
import httl.Engine;
import httl.Node;
import httl.Template;
import httl.Visitor;
import httl.spi.Converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adaptive Template. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class AdaptiveTemplate implements Template, Serializable {

	private static final long serialVersionUID = 3094907176375413567L;

	private final Template writerTemplate;

	private final Template streamTemplate;
	
	private final Converter<Object, Object> outConverter;

	public AdaptiveTemplate(Template writerTemplate, Template streamTemplate, Converter<Object, Object> outConverter) {
		if (writerTemplate == null)
			throw new IllegalArgumentException("writer template == null");
		if (streamTemplate == null)
			throw new IllegalArgumentException("stream template == null");
		this.writerTemplate = writerTemplate;
		this.streamTemplate = streamTemplate;
		this.outConverter = outConverter;
	}

	public String getName() {
		return writerTemplate.getName();
	}

	public String getEncoding() {
		return writerTemplate.getEncoding();
	}

	public Locale getLocale() {
		return writerTemplate.getLocale();
	}

	public long getLastModified() {
		return writerTemplate.getLastModified();
	}

	public long getLength() {
		return writerTemplate.getLength();
	}

	public String getSource() throws IOException {
		return writerTemplate.getSource();
	}

	public Reader getReader() throws IOException {
		return writerTemplate.getReader();
	}

	public InputStream getInputStream() throws IOException {
		return streamTemplate.getInputStream();
	}

	public Engine getEngine() {
		return writerTemplate.getEngine();
	}

	public Object evaluate() throws ParseException {
		// Context.getOut() only OutputStream or Writer
		if (Context.getContext().getOut() instanceof OutputStream) {
			return streamTemplate.evaluate();
		} else {
			return writerTemplate.evaluate();
		}
	}

	public Object evaluate(Object context) throws ParseException {
		// Context.getOut() only OutputStream or Writer
		if (Context.getContext().getOut() instanceof OutputStream) {
			return streamTemplate.evaluate(context);
		} else {
			return writerTemplate.evaluate(context);
		}
	}

	public void render(Object out) throws IOException, ParseException {
		if (out instanceof OutputStream) {
			streamTemplate.render(out);
		} else if (out instanceof Writer) {
			writerTemplate.render(out);
		} else {
			out = outConverter.convert(out, getVariables());
			if (out instanceof OutputStream) {
				streamTemplate.render(out);
			} else {
				writerTemplate.render(out);
			}
		}
	}

	public void render(Object context, Object out)
			throws IOException, ParseException {
		if (out instanceof OutputStream) {
			streamTemplate.render(context, out);
		} else if (out instanceof Writer) {
			writerTemplate.render(context, out);
		} else {
			out = outConverter.convert(out, getVariables());
			if (out instanceof OutputStream) {
				streamTemplate.render(context, out);
			} else {
				writerTemplate.render(context, out);
			}
		}
	}

	public Map<String, Class<?>> getVariables() {
		return writerTemplate.getVariables();
	}

	private Map<String, Template> macros;

	public Map<String, Template> getMacros() {
		if (macros == null) { // allow duplicate on concurrent
			Map<String, Template> map = new HashMap<String, Template>();
			Map<String, Template> writerMacros = writerTemplate.getMacros();
			Map<String, Template> streamMacros = streamTemplate.getMacros();
			for (Map.Entry<String, Template> entry : writerMacros.entrySet()) {
				map.put(entry.getKey(), new AdaptiveTemplate(entry.getValue(), streamMacros.get(entry.getKey()), outConverter));
			}
			macros = Collections.unmodifiableMap(map);
		}
		return macros;
	}

	public int getOffset() {
		return writerTemplate.getOffset();
	}

	public boolean isMacro() {
		return writerTemplate.isMacro();
	}

	public void accept(Visitor visitor) throws IOException, ParseException {
		writerTemplate.accept(visitor);
	}

	public Template getParent() {
		return writerTemplate.getParent();
	}

	public List<Node> getChildren() {
		return writerTemplate.getChildren();
	}

	@Override
	public String toString() {
		return writerTemplate.toString();
	}

}