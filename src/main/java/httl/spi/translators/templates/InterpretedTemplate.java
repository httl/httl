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
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.ast.MacroDirective;
import httl.internal.util.StringSequence;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Switcher;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InterpretedTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class InterpretedTemplate extends AbstractTemplate {

	private Map<String, Class<?>> variables;

	private Map<String, Template> macros;

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

	private List<StringSequence> importSequences;
	
	private Map<Class<?>, Object> importMethods;

	private Map<String, Template> importMacros;

	private String[] importPackages;
	
	private Class<?> defaultVariableType;

	public InterpretedTemplate(Resource resource, Node root, Template parent) throws IOException, ParseException {
		super(resource, root, parent);
	}
	
	public void init() throws IOException, ParseException {
		VariableVisitor visitor = new VariableVisitor(defaultVariableType, true);
		accept(visitor);
		this.variables = Collections.unmodifiableMap(visitor.getVariables());
		Map<String, Template> macros = new HashMap<String, Template>();
		for (Node node : getChildren()) {
			if (node instanceof MacroDirective) {
				InterpretedTemplate macro = new InterpretedTemplate(this, node, this);
				macros.put(((MacroDirective) node).getName(), macro);
			}
		}
		this.macros = Collections.unmodifiableMap(macros);
		for (Template m : macros.values()) {
			InterpretedTemplate macro = (InterpretedTemplate) m;
			macro.setInterceptor(getInterceptor());
			macro.setMapConverter(getMapConverter());
			macro.setOutConverter(getOutConverter());
			macro.setFormatter(formatter);
			macro.setValueFilter(valueFilter);
			macro.setTextFilter(textFilter);
			macro.setForVariable(forVariable);
			macro.setIfVariable(ifVariable);
			macro.setOutputEncoding(outputEncoding);
			macro.setImportSequences(importSequences);
			macro.setImportMethods(importMethods);
			macro.setImportMacros(importMacros);
			macro.setImportPackages(importPackages);
			macro.setTextFilterSwitcher(textFilterSwitcher);
			macro.setValueFilterSwitcher(valueFilterSwitcher);
			macro.setFormatterSwitcher(formatterSwitcher);
			macro.setFilterVariable(filterVariable);
			macro.setFormatterVariable(formatterVariable);
			macro.init();
		}
	}

	@Override
	protected void doRender(Context context) throws Exception {
		InterpretedVisitor visitor = new InterpretedVisitor();
		visitor.setTemplate(this);
		visitor.setOut(Context.getContext().getOut());
		visitor.setFormatter(formatter);
		visitor.setValueFilter(valueFilter);
		visitor.setTextFilter(textFilter);
		visitor.setForVariable(forVariable);
		visitor.setIfVariable(ifVariable);
		visitor.setOutputEncoding(outputEncoding);
		visitor.setImportSequences(importSequences);
		visitor.setImportMethods(importMethods);
		visitor.setImportMacros(importMacros);
		visitor.setImportPackages(importPackages);
		visitor.setTextFilterSwitcher(textFilterSwitcher);
		visitor.setValueFilterSwitcher(valueFilterSwitcher);
		visitor.setFormatterSwitcher(formatterSwitcher);
		visitor.setFilterVariable(filterVariable);
		visitor.setFormatterVariable(formatterVariable);
		accept(visitor);
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

	public void setImportMethods(Map<Class<?>, Object> importMethods) {
		this.importMethods = importMethods;
	}

	public void setImportSequences(List<StringSequence> importSequences) {
		this.importSequences = importSequences;
	}

	public void setImportMacros(Map<String, Template> importMacros) {
		this.importMacros = importMacros;
	}

	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
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

	public Map<String, Class<?>> getVariables() {
		return variables;
	}

	public Map<String, Template> getMacros() {
		return macros;
	}

	public void setDefaultVariableType(Class<?> defaultVariableType) {
		this.defaultVariableType = defaultVariableType;
	}

}