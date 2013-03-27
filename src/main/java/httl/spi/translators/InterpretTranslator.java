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
package httl.spi.translators;

import httl.Node;
import httl.Resource;
import httl.Template;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Parser;
import httl.spi.Translator;
import httl.spi.translators.templates.InterpretTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * InterpretTranslator
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class InterpretTranslator implements Translator {

	private Parser templateParser;

	private Converter<Object, Object> mapConverter;

	private Converter<Object, Object> outConverter;
	
	private Formatter<Object> formatter;

	private Filter textFilter;

	private Filter valueFilter;

	private String[] forVariable = new String[] { "for" };

	private String ifVariable = "if";

	private String outputEncoding;

	public void setMapConverter(Converter<Object, Object> mapConverter) {
		this.mapConverter = mapConverter;
	}

	public void setOutConverter(Converter<Object, Object> outConverter) {
		this.outConverter = outConverter;
	}

	public void setFormatter(Formatter<Object> formatter) {
		this.formatter = formatter;
	}

	public void setTextFilter(Filter textFilter) {
		this.textFilter = textFilter;
	}

	public void setValueFilter(Filter valueFilter) {
		this.valueFilter = valueFilter;
	}

	public void setForVariable(String[] forVariable) {
		this.forVariable = forVariable;
	}

	public void setIfVariable(String ifVariable) {
		this.ifVariable = ifVariable;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void setTemplateParser(Parser templateParser) {
		this.templateParser = templateParser;
	}

	public Template translate(Resource resource,
			Map<String, Class<?>> parameterTypes) throws ParseException,
			IOException {
		Node root = templateParser.parse(resource.getSource(), 0);
		InterpretTemplate template = new InterpretTemplate(resource, root, null);
		template.setMapConverter(mapConverter);
		template.setOutConverter(outConverter);
		template.setFormatter(formatter);
		template.setValueFilter(valueFilter);
		template.setTextFilter(textFilter);
		template.setForVariable(forVariable);
		template.setIfVariable(ifVariable);
		template.setOutputEncoding(outputEncoding);
		return template;
	}

}
