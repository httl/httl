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

import httl.Engine;
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.internal.util.StringSequence;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Logger;
import httl.spi.Parser;
import httl.spi.Switcher;
import httl.spi.Translator;
import httl.spi.translators.templates.InterpretedTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * InterpretedTranslator. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setTranslator(Translator)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class InterpretedTranslator implements Translator {

	private Parser templateParser;

	private Formatter<Object> formatter;

	private Filter textFilter;

	private Filter valueFilter;

	private Switcher<Filter> textFilterSwitcher;

	private Switcher<Filter> valueFilterSwitcher;

	private Switcher<Formatter<Object>> formatterSwitcher;

	private String filterVariable = "filter";

	private String formatterVariable = "formatter";

	private String[] forVariable = new String[] { "for" };

	private String ifVariable = "if";

	private String outputEncoding;

	private Engine engine;

	private Class<?> defaultVariableType;

	private final List<StringSequence> importSequences = new CopyOnWriteArrayList<StringSequence>();

	private final Map<Class<?>, Object> functions = new ConcurrentHashMap<Class<?>, Object>();

	private String[] importPackages;

	private String[] importMacros;
   
	private final Map<String, Template> importMacroTemplates = new ConcurrentHashMap<String, Template>();

	private Interceptor interceptor;

	private Converter<Object, Object> mapConverter;

	private Converter<Object, Object> outConverter;

	private Logger logger;

	private Filter templateFilter;

	/**
	 * httl.properties: template.filter=httl.spi.filters.AttributeSyntaxFilter
	 */
	public void setTemplateFilter(Filter templateFilter) {
		this.templateFilter = templateFilter;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setMapConverter(Converter<Object, Object> mapConverter) {
		this.mapConverter = mapConverter;
	}

	public void setOutConverter(Converter<Object, Object> outConverter) {
		this.outConverter = outConverter;
	}

	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}

	/**
	 * inited.
	 */
	public void inited() {
		if (importMacros != null && importMacros.length > 0) {
			for (String importMacro : importMacros) {
				try {
					Template importMacroTemplate = engine.getTemplate(importMacro);
					importMacroTemplates.putAll(importMacroTemplate.getMacros());
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
		}
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	public void setTextFilterSwitcher(Switcher<Filter> textFilterSwitcher) {
		this.textFilterSwitcher = textFilterSwitcher;
	}

	public void setValueFilterSwitcher(Switcher<Filter> valueFilterSwitcher) {
		this.valueFilterSwitcher = valueFilterSwitcher;
	}

	public void setFormatterSwitcher(Switcher<Formatter<Object>> formatterSwitcher) {
		this.formatterSwitcher = formatterSwitcher;
	}

	public void setFilterVariable(String filterVariable) {
		this.filterVariable = filterVariable;
	}

	public void setFormatterVariable(String formatterVariable) {
		this.formatterVariable = formatterVariable;
	}

	/**
	 * httl.properties: import.macros=common.httl
	 */
	public void setImportMacros(String[] importMacros) {
		this.importMacros = importMacros;
	}

	/**
	 * httl.properties: import.packages=java.util
	 */
	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	/**
	 * httl.properties: import.methods=java.lang.Math
	 */
	public void setImportMethods(String[] importMethods) {
		for (Object function : importMethods) {
			if (function instanceof Class) {
				this.functions.put((Class<?>) function, function);
			} else {
				this.functions.put(function.getClass(), function);
			}
		}
	}

	/**
	 * httl.properties: import.sequences=Mon Tue Wed Thu Fri Sat Sun Mon
	 */
	public void setImportSequences(String[] sequences) {
		for (String s : sequences) {
			s = s.trim();
			if (s.length() > 0) {
				String[] ts = s.split("\\s+");
				List<String> sequence = new ArrayList<String>();
				for (String t : ts) {
					t = t.trim();
					if (t.length() > 0) {
						sequence.add(t);
					}
				}
				this.importSequences.add(new StringSequence(sequence));
			}
		}
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

	public void setDefaultVariableType(Class<?> defaultVariableType) {
		this.defaultVariableType = defaultVariableType;
	}

	public Template translate(Resource resource,
			Map<String, Class<?>> parameterTypes) throws ParseException,
			IOException {
		if (logger != null && logger.isDebugEnabled()) {
			logger.debug("Interprete template " + resource.getName());
		}
		String source = resource.getSource();
		if (templateFilter != null) {
			source = templateFilter.filter(resource.getName(), source);
		}
		Node root = templateParser.parse(source, 0);
		InterpretedTemplate template = new InterpretedTemplate(resource, root, null);
		template.setInterceptor(interceptor);
		template.setMapConverter(mapConverter);
		template.setOutConverter(outConverter);
		template.setFormatter(formatter);
		template.setValueFilter(valueFilter);
		template.setTextFilter(textFilter);
		template.setForVariable(forVariable);
		template.setIfVariable(ifVariable);
		template.setOutputEncoding(outputEncoding);
		template.setImportSequences(importSequences);
		template.setImportMethods(functions);
		template.setImportMacros(importMacroTemplates);
		template.setImportPackages(importPackages);
		template.setTextFilterSwitcher(textFilterSwitcher);
		template.setValueFilterSwitcher(valueFilterSwitcher);
		template.setFormatterSwitcher(formatterSwitcher);
		template.setFilterVariable(filterVariable);
		template.setFormatterVariable(formatterVariable);
		template.setDefaultVariableType(defaultVariableType);
		template.init();
		return template;
	}

}