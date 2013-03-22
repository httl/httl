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
package httl.spi.parsers;

import httl.Context;
import httl.Engine;
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.ast.Break;
import httl.ast.Else;
import httl.ast.Expression;
import httl.ast.For;
import httl.ast.If;
import httl.ast.Macro;
import httl.ast.TemplateVisitor;
import httl.ast.Text;
import httl.ast.Value;
import httl.ast.Var;
import httl.internal.util.ByteCache;
import httl.internal.util.CharCache;
import httl.internal.util.ClassUtils;
import httl.internal.util.CollectionUtils;
import httl.internal.util.IOUtils;
import httl.internal.util.LocaleUtils;
import httl.internal.util.OrderedMap;
import httl.internal.util.Status;
import httl.internal.util.StringCache;
import httl.internal.util.StringSequence;
import httl.internal.util.StringUtils;
import httl.internal.util.VolatileReference;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Switcher;
import httl.spi.formatters.MultiFormatter;
import httl.spi.parsers.templates.AbstractTemplate;
import httl.spi.parsers.templates.OutputStreamTemplate;
import httl.spi.parsers.templates.WriterTemplate;

import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CompileTemplateVisitor
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CompileTemplateVisitor extends TemplateVisitor {

	private StringBuilder builder = new StringBuilder();

	private StringBuilder textFields = new StringBuilder();
	
	private String filterKey = null;
	
	private VolatileReference<Filter> filterReference = new VolatileReference<Filter>();
	
	private Set<String> setVariables = new HashSet<String>();

	private Set<String> getVariables = new HashSet<String>();

	private List<String> defVariables = new ArrayList<String>();
	
	private List<Class<?>> defVariableTypes = new ArrayList<Class<?>>();
	
	private Map<String, Class<?>> types = new HashMap<String, Class<?>>();

	private Map<String, Class<?>> returnTypes = new HashMap<String, Class<?>>();
	
	private Map<String, Class<?>> macros = new HashMap<String, Class<?>>();
	
	private Template template;

	private int offset;
	
	private boolean stream;

	private String engineName;

	private String forVariable = "for";

	private String[] importGetters = new String[] { "get" };

	private String[] importSizers = new String[] { "size", "length" };

	private String filterVariable = "filter";

	private String formatterVariable = "formatter";

	private String defaultFilterVariable;
	
	private String defaultFormatterVariable;

	private Switcher<Filter> textFilterSwitcher;

	private Switcher<Filter> valueFilterSwitcher;

	private Switcher<Formatter<Object>> formatterSwitcher;

	private Filter templateFilter;

	private Filter textFilter;

	private Map<String, Template> importMacroTemplates = new ConcurrentHashMap<String, Template>();

	private String[] importPackages;

	private Set<String> importPackageSet;

	private Map<String, Class<?>> importTypes;

	private Map<Class<?>, Object> functions = new ConcurrentHashMap<Class<?>, Object>();

	private List<StringSequence> sequences = new CopyOnWriteArrayList<StringSequence>();

	private static final String TEMPLATE_CLASS_PREFIX = AbstractTemplate.class.getPackage().getName() + ".Template_";
	
	private final AtomicInteger TMP_VAR_SEQ = new AtomicInteger();

	private boolean sourceInClass;

	private boolean textInClass;
	
	private String outputEncoding;
	
	private Class<?> defaultVariableType = Object.class;
	
	private Compiler compiler;

	public void setCompiler(Compiler compiler) {
		this.compiler = compiler;
	}

	public void setForVariable(String forVariable) {
		this.forVariable = forVariable;
	}

	public void setImportSizers(String[] importSizers) {
		this.importSizers = importSizers;
	}

	public void setTypes(Map<String, Class<?>> types) {
		this.types = types;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}

	public void setFilterVariable(String filterVariable) {
		this.filterVariable = filterVariable;
	}

	public void setFormatterVariable(String formatterVariable) {
		this.formatterVariable = formatterVariable;
	}

	public void setDefaultFilterVariable(String defaultFilterVariable) {
		this.defaultFilterVariable = defaultFilterVariable;
	}

	public void setDefaultFormatterVariable(String defaultFormatterVariable) {
		this.defaultFormatterVariable = defaultFormatterVariable;
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

	public void setTemplateFilter(Filter templateFilter) {
		this.templateFilter = templateFilter;
	}

	public void setTextFilter(Filter textFilter) {
		this.textFilter = textFilter;
		filterReference.set(textFilter);
	}

	public void setImportMacroTemplates(Map<String, Template> importMacroTemplates) {
		this.importMacroTemplates = importMacroTemplates;
	}

	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	public void setImportPackageSet(Set<String> importPackageSet) {
		this.importPackageSet = importPackageSet;
	}

	public void setImportTypes(Map<String, Class<?>> importTypes) {
		this.importTypes = importTypes;
	}

	public void setImportMethods(Map<Class<?>, Object> functions) {
		this.functions = functions;
	}

	public void setImportSequences(List<StringSequence> sequences) {
		this.sequences = sequences;
	}

	public void setImportGetters(String[] importGetters) {
		this.importGetters = importGetters;
	}

	public void setSourceInClass(boolean sourceInClass) {
		this.sourceInClass = sourceInClass;
	}

	public void setTextInClass(boolean textInClass) {
		this.textInClass = textInClass;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void setDefaultVariableType(Class<?> defaultVariableType) {
		this.defaultVariableType = defaultVariableType;
	}

	public CompileTemplateVisitor() {
	}

	@Override
	public void visit(Text node) throws ParseException {
		String txt = node.getContent();
		Filter filter = filterReference.get();
		if (textFilterSwitcher != null || valueFilterSwitcher != null || formatterSwitcher != null) {
			Set<String> locations = new HashSet<String>();
			List<String> textLocations = textFilterSwitcher == null ? null : textFilterSwitcher.locations();
			if (textLocations != null) {
				locations.addAll(textLocations);
			}
			List<String> valueLocations = valueFilterSwitcher == null ? null : valueFilterSwitcher.locations();
			if (valueLocations != null) {
				locations.addAll(valueLocations);
			}
			List<String> formatterLocations = formatterSwitcher == null ? null : formatterSwitcher.locations();
			if (formatterLocations != null) {
				locations.addAll(formatterLocations);
			}
			if (locations != null && locations.size() > 0) {
				Map<Integer, Set<String>> switchesd = new TreeMap<Integer, Set<String>>();
				for (String location : locations) {
					int i = -1;
					while ((i = txt.indexOf(location, i + 1)) >= 0) {
						Integer key = Integer.valueOf(i);
						Set<String> values = switchesd.get(key);
						if (values == null) {
							values = new HashSet<String>();
							switchesd.put(key, values);
						}
						values.add(location);
					}
				}
				if (switchesd.size() > 0) {
					getVariables.add(filterVariable);
					int begin = 0;
					for (Map.Entry<Integer, Set<String>> entry : switchesd.entrySet()) {
						int end = entry.getKey();
						String part = getTextPart(txt.substring(begin, end), filter, filterKey, textFields, TMP_VAR_SEQ, stream, false);
						if (StringUtils.isNotEmpty(part)) {
							builder.append("	$output.write(" + part + ");\n");
						}
						begin = end;
						for (String location : entry.getValue()) {
							if (textLocations != null && textLocations.contains(location)) {
								filter = textFilterSwitcher.switchover(location, textFilter);
								filterReference.set(filter);
							}
							if (valueLocations != null && valueLocations.contains(location)) {
								builder.append("	" + filterVariable + " = switchFilter(\"" + StringUtils.escapeString(location) + "\", " + defaultFilterVariable + ");\n");
							}
							if (formatterLocations != null && formatterLocations.contains(location)) {
								builder.append("	" + formatterVariable + " = switchFormatter(\"" + StringUtils.escapeString(location) + "\", " + defaultFormatterVariable + ");\n");
							}
						}
					}
					if (begin > 0) {
						txt = txt.substring(begin);
					}
				}
			}
		}
		String part = getTextPart(txt, filter, filterKey, textFields, TMP_VAR_SEQ, stream, false);
		if (StringUtils.isNotEmpty(part)) {
			builder.append("	$output.write(" + part + ");\n");
		}
	}
	
	private CompileExpressionVisitor createExpressionVisitor(Expression node) throws ParseException {
		CompileExpressionVisitor visitor = new CompileExpressionVisitor();
		visitor.setDefaultVariableType(defaultVariableType);
		visitor.setTypes(types);
		visitor.setImportPackages(importPackages);
		visitor.setImportMethods(functions.keySet());
		visitor.setImportGetters(importGetters);
		visitor.setImportSizers(importSizers);
		visitor.setImportSequences(sequences);
		node.accept(visitor);
		return visitor;
	}

	@Override
	public void visit(Value node) throws ParseException {
		boolean nofilter = node.isNoFilter();
		CompileExpressionVisitor expression = createExpressionVisitor(node.getExpression());
		Class<?> returnType = expression.getReturnType();
		getVariables.addAll(expression.getVariableTypes().keySet());
		String code = expression.getCode();
		if (nofilter && Template.class.isAssignableFrom(returnType)) {
			if (! StringUtils.isNamed(code)) {
				code = "(" + code + ")";
			}
			builder.append("	if (");
			builder.append(code);
			builder.append(" != null) ");
			builder.append(code);
			builder.append(".render($output);\n");
		} else if (nofilter && Resource.class.isAssignableFrom(returnType)) {
			if (! StringUtils.isNamed(code)) {
				code = "(" + code + ")";
			}
			builder.append("	");
			builder.append(IOUtils.class.getName());
			builder.append(".copy(");
			builder.append(code);
			if (stream) {
				builder.append(".getInputStream()");
			} else {
				builder.append(".getReader()");
			}
			builder.append(", $output);\n");
		} else {
			if (Template.class.isAssignableFrom(returnType)) {
				if (! StringUtils.isNamed(code)) {
					code = "(" + code + ")";
				}
				code = code + " == null ? null : " + code + ".evaluate()";
			} else if (Resource.class.isAssignableFrom(returnType)) {
				if (! StringUtils.isNamed(code)) {
					code = "(" + code + ")";
				}
				code = code + " == null ? null : " + IOUtils.class.getName() + ".readToString(" + code + ".getReader())";
			}
			getVariables.add(formatterVariable);
			String key = getTextPart(expression.getCode(), null, null, textFields, TMP_VAR_SEQ, stream, true);
			if (! stream && Object.class.equals(returnType)) {
				String pre = "";
				String var = "$obj" + TMP_VAR_SEQ.getAndIncrement();
				pre = "	Object " + var + " = " + code + ";\n";
				String charsCode = "formatter.toChars(" + key + ", (char[]) " + var + ")";
				code = "formatter.toString(" + key + ", " + var + ")";
				if (! nofilter) {
					getVariables.add(filterVariable);
					charsCode = "doFilter(" + filterVariable + ", " + key + ", " + charsCode + ")";
					code = "doFilter(" + filterVariable + ", " + key + ", " + code + ")";
				}
				builder.append(pre);
				builder.append("	if (" + var + " instanceof char[]) $output.write(");
				builder.append(charsCode);
				builder.append("); else $output.write(");
				builder.append(code);
				builder.append(");\n");
			} else {
				if (stream) {
					code = "formatter.toBytes(" + key + ", " + code + ")";
				} else if (char[].class.equals(returnType)) {
					code = "formatter.toChars(" + key + ", " + code + ")";
				} else {
					code = "formatter.toString(" + key + ", " + code + ")";
				}
				if (! nofilter) {
					getVariables.add(filterVariable);
					code = "doFilter(" + filterVariable + ", " + key + ", " + code + ")";
				}
				builder.append("	$output.write(");
				builder.append(code);
				builder.append(");\n");
			}
		}
	}

	@Override
	public void visit(Var node) throws ParseException {
		Class<?> type = node.getType();
		if (node.getExpression() != null) {
			CompileExpressionVisitor expression = createExpressionVisitor(node.getExpression());
			if (type == null) {
				type = expression.getReturnType();
			}
			appendVar(type, node.getName(), expression.getCode(), node.isParent(), node.isHide(), node.getOffset());
			getVariables.addAll(expression.getVariableTypes().keySet());
		} else {
			types.put(node.getName(), type);
			defVariables.add(node.getName());
			defVariableTypes.add(type);
		}
	}

	private void appendVar(Class<?> clazz, String var, String code, boolean parent, boolean hide, int offset) throws ParseException {
		String type = clazz.getCanonicalName();
		Class<?> cls = types.get(var);
		if (cls != null && ! cls.equals(clazz)) {
			throw new ParseException("Set different type value to variable " + var + ", conflict types: " + cls.getCanonicalName() + ", " + clazz.getCanonicalName(), offset);
		}
		types.put(var, clazz);
		setVariables.add(var);
		builder.append("	" + var + " = (" + type + ")(" + code + ");\n");
		String ctx = null;
		if (parent) {
			ctx = "($context.getParent() != null ? $context.getParent() : $context)";
			returnTypes.put(var, clazz);
		} else if (! hide) {
			ctx = "$context";
		}
		if (StringUtils.isNotEmpty(ctx)) {
			builder.append("	" + ctx + ".put(\"");
			builder.append(var);
			builder.append("\", ");
			builder.append(ClassUtils.class.getName() + ".boxed(" + var + ")");
			builder.append(");\n");
		}
	}
	
	@Override
	public boolean visit(If node) throws ParseException {
		CompileExpressionVisitor expression = createExpressionVisitor(node.getExpression());
		builder.append("	if(");
		builder.append(StringUtils.getConditionCode(expression.getReturnType(), expression.getCode(), importSizers));
		builder.append(") {\n");
		getVariables.addAll(expression.getVariableTypes().keySet());
		return true;
	}

	@Override
	public void end(If node) throws ParseException {
		builder.append("	}\n");
	}

	@Override
	public boolean visit(Else node) throws ParseException {
		if (node.getExpression() == null) {
			builder.append("	else {\n");
		} else {
			CompileExpressionVisitor expression = createExpressionVisitor(node.getExpression());
			builder.append("	else if (");
			builder.append(StringUtils.getConditionCode(expression.getReturnType(), expression.getCode(), importSizers));
			builder.append(") {\n");
			getVariables.addAll(expression.getVariableTypes().keySet());
		}
		return true;
	}

	@Override
	public void end(Else node) throws ParseException {
		builder.append("	}\n");
	}

	@Override
	public boolean visit(For node) throws ParseException {
		String var = node.getName();
		Class<?> clazz = node.getType();
		CompileExpressionVisitor expression = createExpressionVisitor(node.getExpression());
		String code = expression.getCode();
		Class<?> returnType = expression.getReturnType();
		String exprName = CompileExpressionVisitor.getGenericVariableName(node.getExpression());
		if (clazz == null) {
			if (returnType.isArray()) {
				clazz = returnType.getComponentType();
			} else if (Map.class.isAssignableFrom(returnType)) {
				clazz = Map.Entry.class;
			} else if (Collection.class.isAssignableFrom(returnType)) {
				clazz = types.get(exprName + ":0"); // Collection<T>泛型 
				if (clazz == null) {
					clazz = defaultVariableType;
				}
			} else {
				clazz = defaultVariableType;
			}
		}
		if (Map.class.isAssignableFrom(returnType)) {
			Class<?> keyType = types.get(exprName + ":0");
			if (keyType != null) {
				types.put(var + ":0", keyType);
			}
			Class<?> valueType = types.get(exprName + ":1");
			if (valueType != null) {
				types.put(var + ":1", valueType);
			}
			code = ClassUtils.class.getName() + ".entrySet(" + code + ")";
		}
		String name = "_i_" + var;
		builder.append("	for (" + Iterator.class.getName() + " " + name + " = " + CollectionUtils.class.getName() + ".toIterator((" + forVariable + " = new " + Status.class.getName() + "(" + forVariable + ", " + code + ")).getData()); " + name + ".hasNext();) {\n");
		String varCode;
		if (clazz.isPrimitive()) {
			varCode = ClassUtils.class.getName() + ".unboxed((" + ClassUtils.getBoxedClass(clazz).getSimpleName() + ")" + name + ".next())";
		} else {
			varCode = name + ".next()";
		}
		appendVar(clazz, var, varCode, false, false, node.getOffset());
		getVariables.addAll(expression.getVariableTypes().keySet());
		setVariables.add(forVariable);
		return true;
	}

	@Override
	public void end(For node) throws ParseException {
		builder.append("	" + forVariable + ".increment();\n	}\n	" + forVariable + " = " + forVariable + ".getParent();\n");
	}

	@Override
	public void visit(Break node) throws ParseException {
		CompileExpressionVisitor expression = createExpressionVisitor(node.getExpression());
		builder.append("	if(");
		builder.append(StringUtils.getConditionCode(expression.getReturnType(), expression.getCode(), importSizers));
		builder.append(") break;\n");
		getVariables.addAll(expression.getVariableTypes().keySet());
	}

	@Override
	public boolean visit(Macro node) throws ParseException {
		CompileTemplateVisitor visitor = new CompileTemplateVisitor();
		visitor.setTemplate(node);
		// visitor.setTypes(types);
		visitor.setStream(stream);
		visitor.setOffset(offset);
		visitor.setDefaultFilterVariable(defaultFilterVariable);
		visitor.setDefaultFormatterVariable(defaultFormatterVariable);
		visitor.setDefaultVariableType(defaultVariableType);
		visitor.setEngineName(engineName);
		visitor.setFilterVariable(filterVariable);
		visitor.setFormatterSwitcher(formatterSwitcher);
		visitor.setFormatterVariable(formatterVariable);
		visitor.setForVariable(forVariable);
		visitor.setImportMacroTemplates(importMacroTemplates);
		visitor.setImportPackages(importPackages);
		visitor.setImportPackageSet(importPackageSet);
		visitor.setImportSizers(importSizers);
		visitor.setImportGetters(importGetters);
		visitor.setImportTypes(importTypes);
		visitor.setImportMethods(functions);
		visitor.setOutputEncoding(outputEncoding);
		visitor.setSourceInClass(sourceInClass);
		visitor.setTemplateFilter(templateFilter);
		visitor.setTextFilter(textFilter);
		visitor.setTextFilterSwitcher(textFilterSwitcher);
		visitor.setTextInClass(textInClass);
		visitor.setValueFilterSwitcher(valueFilterSwitcher);
		visitor.setCompiler(compiler);
		visitor.init();
		for (Node n : node.getNodes()) {
			n.accept(visitor);
		}
		Class<?> macroType = visitor.compile();
		macros.put(node.getName(), macroType);
		return false;
	}

	public void init() {
		if (types == null) {
			types = new HashMap<String, Class<?>>();
		}
		if (importTypes != null && importTypes.size() > 0) {
			types.putAll(importTypes);
		}
		types.put("this", Template.class);
		types.put("super", Template.class);
		types.put(defaultFilterVariable, Filter.class);
		types.put(filterVariable, Filter.class);
		types.put(defaultFormatterVariable, Formatter.class);
		types.put(formatterVariable, Formatter.class);
		types.put(forVariable, Status.class);
		for (String macro : importMacroTemplates.keySet()) {
			types.put(macro, Template.class);
		}
		for (String macro : template.getMacros().keySet()) {
			types.put(macro, Template.class);
		}
	}

	public Class<?> compile() throws ParseException {
		String code = getCode();
		return compiler.compile(code);
	}

	private String getCode() throws ParseException {
		String name = getTemplateClassName(template, stream);
		String source = template.getSource();
		if (templateFilter != null) {
			source = templateFilter.filter(template.getName(), source);
		}
		String code = builder.toString();
		int i = name.lastIndexOf('.');
		String packageName = i < 0 ? "" : name.substring(0, i);
		String className = i < 0 ? name : name.substring(i + 1);
		StringBuilder imports = new StringBuilder();
		String[] packages = importPackages;
		if (packages != null && packages.length > 0) {
			for (String pkg : packages) {
				imports.append("import ");
				imports.append(pkg);
				imports.append(".*;\n");
			}
		}
		Set<String> defined = new HashSet<String>();
		StringBuilder statusInit = new StringBuilder();
		StringBuilder macroFields = new StringBuilder();
		StringBuilder macroInits = new StringBuilder();
		StringBuilder declare = new StringBuilder();
		if (getVariables.contains("this")) {
			defined.add("this");
			declare.append("	" + Template.class.getName() + " " + ClassUtils.filterJavaKeyword("this") + " = this;\n");
		}
		if (getVariables.contains("super")) {
			defined.add("super");
			declare.append("	" + Template.class.getName() + " " + ClassUtils.filterJavaKeyword("super") + " = ($context.getParent() == null ? null : $context.getParent().getTemplate());\n");
		}
		if (getVariables.contains(filterVariable)) {
			defined.add(filterVariable);
			defined.add(defaultFilterVariable);
			declare.append("	" + Filter.class.getName() + " " + defaultFilterVariable + " = getFilter($context, \"" + filterVariable + "\");\n");
			declare.append("	" + Filter.class.getName() + " " + filterVariable + " = " + defaultFilterVariable + ";\n");
		}
		if (getVariables.contains(formatterVariable)) {
			defined.add(formatterVariable);
			defined.add(defaultFormatterVariable);
			declare.append("	" + MultiFormatter.class.getName() + " " + defaultFormatterVariable + " = getFormatter($context, \"" + formatterVariable + "\");\n");
			declare.append("	" + MultiFormatter.class.getName() + " " + formatterVariable + " = " + defaultFormatterVariable + ";\n");
		}
		for (String var : defVariables) {
			if (getVariables.contains(var) && ! defined.contains(var)) {
				defined.add(var);
				declare.append(getTypeCode(types.get(var), var));
			}
		}
		Set<String> macroKeySet = macros.keySet();
		for (String macro : macroKeySet) {
			types.put(macro, Template.class);
			if (getVariables.contains(macro) && ! defined.contains(macro)) {
				defined.add(macro);
				macroFields.append("private final " + Template.class.getName() + " " + macro + ";\n");
				macroInits.append("	" + macro + " = getMacros().get(\"" + macro + "\");\n");
				declare.append("	" + Template.class.getName() + " " + macro + " = getMacro($context, \"" + macro + "\", this." + macro + ");\n");
			}
		}
		if (importTypes != null && importTypes.size() > 0) {
			for (Map.Entry<String, Class<?>> entry : importTypes.entrySet()) {
				String var = entry.getKey();
				if (getVariables.contains(var)  && ! defined.contains(var)) {
					defined.add(var);
					declare.append(getTypeCode(entry.getValue(), var));
				}
			}
		}
		for (String macro : importMacroTemplates.keySet()) {
			if (getVariables.contains(macro) && ! defined.contains(macro)) {
				defined.add(macro);
				macroFields.append("private final " + Template.class.getName() + " " + macro + ";\n");
				macroInits.append("	" + macro + " = getImportMacros().get(\"" + macro + "\");\n");
				declare.append("	" + Template.class.getName() + " " + macro + " = getMacro($context, \"" + macro + "\", this." + macro + ");\n");
			}
		}
		for (String var : setVariables) {
			if (! defined.contains(var)) {
				defined.add(var);
				Class<?> type = types.get(var);
				String typeName = getTypeName(type);
				declare.append("	" + typeName + " " + var + " = " + ClassUtils.getInitCode(type) + ";\n");
			}
		}
		for (String var : getVariables) {
			if (! defined.contains(var)) {
				Class<?> type = types.get(var);
				if (type == null) {
					type = defaultVariableType;
				}
				defined.add(var);
				declare.append(getTypeCode(type, var));
				defVariables.add(var);
				defVariableTypes.add(type);
			}
		}
		StringBuilder funtionFileds = new StringBuilder();
		StringBuilder functionInits = new StringBuilder();
		for (Map.Entry<Class<?>, Object> function : functions.entrySet()) {
			Class<?> functionType = function.getKey();
			if (function.getValue() instanceof Class) {
				continue;
			}
			String pkgName = functionType.getPackage() == null ? null : functionType.getPackage().getName();
			String typeName;
			if (pkgName != null && ("java.lang".equals(pkgName) 
					|| (importPackageSet != null && importPackageSet.contains(pkgName)))) {
				typeName = functionType.getSimpleName();
			} else {
				typeName = functionType.getCanonicalName();
			}
			funtionFileds.append("private final ");
			funtionFileds.append(typeName);
			funtionFileds.append(" $");
			funtionFileds.append(functionType.getName().replace('.','_'));
			funtionFileds.append(";\n");
			
			functionInits.append("	this.$");
			functionInits.append(functionType.getName().replace('.','_'));
			functionInits.append(" = (");
			functionInits.append(typeName);
			functionInits.append(") functions.get(");
			functionInits.append(typeName);
			functionInits.append(".class);\n");
		}
		
		String methodCode = statusInit.toString() + declare + code;
		
		if (sourceInClass) {
			textFields.append("private static final " + String.class.getSimpleName() + " $SRC = \"" + StringUtils.escapeString(source) + "\";\n");
			textFields.append("private static final " + String.class.getSimpleName() + " $CODE = \"" + StringUtils.escapeString(methodCode) + "\";\n");
		} else {
			String sourceCodeId = StringCache.put(source);
			textFields.append("private static final " + String.class.getSimpleName() + " $SRC = " + StringCache.class.getName() +  ".getAndRemove(\"" + sourceCodeId + "\");\n");
			String methodCodeId = StringCache.put(methodCode);
			textFields.append("private static final " + String.class.getSimpleName() + " $CODE = " + StringCache.class.getName() +  ".getAndRemove(\"" + methodCodeId + "\");\n");
		}
		
		textFields.append("private static final " + Map.class.getName() + " $PTS = " + toTypeCode(defVariables, defVariableTypes) + ";\n");
		textFields.append("private static final " + Map.class.getName() + " $CTS = " + toTypeCode(returnTypes) + ";\n");
		
		String sorceCode = "package " + packageName + ";\n" 
				+ "\n"
				+ imports.toString()
				+ "\n"
				+ "public final class " + className + " extends " + (stream ? OutputStreamTemplate.class.getName() : WriterTemplate.class.getName()) + " {\n" 
				+ "\n"
				+ textFields
				+ "\n"
				+ funtionFileds
				+ "\n"
				+ macroFields
				+ "\n"
				+ "public " + className + "("
				+ Engine.class.getName() + " engine, " 
				+ Interceptor.class.getName() + " interceptor, " 
				+ Compiler.class.getName() + " compiler, " 
				+ Switcher.class.getName() + " filterSwitcher, " 
				+ Switcher.class.getName() + " formatterSwitcher, " 
				+ Filter.class.getName() + " filter, "
				+ Formatter.class.getName() + " formatter, "
				+ Converter.class.getName() + " mapConverter, "
				+ Converter.class.getName() + " outConverter, "
				+ Map.class.getName() + " functions, " 
				+ Map.class.getName() + " importMacros) {\n" 
				+ "	super(engine, interceptor, compiler, filterSwitcher, formatterSwitcher, filter, formatter, mapConverter, outConverter, functions, importMacros);\n"
				+ functionInits
				+ macroInits
				+ "}\n"
				+ "\n"
				+ "public void doRender(" + Context.class.getName() + " $context, " 
				+ (stream ? OutputStream.class.getName() : Writer.class.getName())
				+ " $output) throws " + Exception.class.getName() + " {\n" 
				+ methodCode
				+ "}\n"
				+ "\n"
				+ "public " + String.class.getSimpleName() + " getName() {\n"
				+ "	return \"" + template.getName() + "\";\n"
				+ "}\n"
				+ "\n"
				+ "public " + String.class.getSimpleName() + " getEncoding() {\n"
				+ "	return " + (template.getEncoding() == null ? "null" : "\"" + template.getEncoding() + "\"") + ";\n"
				+ "}\n"
				+ "\n"
				+ "public " + Locale.class.getName() + " getLocale() {\n"
				+ "	return " + (template.getLocale() == null ? "null" : LocaleUtils.class.getName() + ".getLocale(\"" + template.getLocale() + "\")") + ";\n"
				+ "}\n"
				+ "\n"
				+ "public long getLastModified() {\n"
				+ "	return " + template.getLastModified() + "L;\n"
				+ "}\n"
				+ "\n"
				+ "public long getLength() {\n"
				+ "	return " + template.getLength() + "L;\n"
				+ "}\n"
				+ "\n"
				+ "public " + String.class.getSimpleName() + " getSource() {\n"
				+ "	return $SRC;\n"
				+ "}\n"
				+ "\n"
				+ "public " + String.class.getSimpleName() + " getCode() {\n"
				+ "	return $CODE;\n"
				+ "}\n"
				+ "\n"
				+ "public " + Map.class.getName() + " getVariableTypes() {\n"
				+ "	return $PTS;\n"
				+ "}\n"
				+ "\n"
				+ "public " + Map.class.getName() + " getExportTypes() {\n"
				+ "	return $CTS;\n"
				+ "}\n"
				+ "\n"
				+ "public " + Map.class.getName() + " getMacroTypes() {\n"
				+ "	return " + toTypeCode(macros) + ";\n"
				+ "}\n"
				+ "\n"
				+ "public boolean isMacro() {\n"
				+ "	return " + (offset > 0 || template.getName().indexOf('#') >= 0) + ";\n"
				+ "}\n"
				+ "\n"
				+ "public int getOffset() {\n"
				+ "	return " + offset + ";\n"
				+ "}\n"
				+ "\n"
				+ "}\n";
		return sorceCode;
	}

	private String getTypeName(Class<?> type) {
		return type.getCanonicalName();
	}

	private String getTypeCode(Class<?> type, String var) {
		String typeName = getTypeName(type);
		if (type.isPrimitive()) {
			return "	" + typeName + " " + ClassUtils.filterJavaKeyword(var) + " = " + ClassUtils.class.getName() + ".unboxed((" + ClassUtils.getBoxedClass(type).getSimpleName() + ") $context.get(\"" + var + "\"));\n";
		} else {
			return "	" + typeName + " " + ClassUtils.filterJavaKeyword(var) + " = (" + typeName + ") $context.get(\"" + var + "\");\n";
		}
	}
	
	private String toTypeCode(Map<String, Class<?>> types) {
		StringBuilder keyBuf = new StringBuilder();
		StringBuilder valueBuf = new StringBuilder();
		if (types == null || types.size() == 0) {
			keyBuf.append("new String[0]");
			valueBuf.append("new Class[0]");
		} else {
			keyBuf.append("new String[] {");
			valueBuf.append("new Class[] {");
			boolean first = true;
			for (Map.Entry<String, Class<?>> entry : types.entrySet()) {
				if (first) {
					first = false;
				} else {
					keyBuf.append(", ");
					valueBuf.append(", ");
				}
				keyBuf.append("\"");
				keyBuf.append(StringUtils.escapeString(entry.getKey()));
				keyBuf.append("\"");
				
				valueBuf.append(entry.getValue().getCanonicalName());
				valueBuf.append(".class");;
			}
			keyBuf.append("}");
			valueBuf.append("}");
		}
		StringBuilder buf = new StringBuilder();
		buf.append("new ");
		buf.append(OrderedMap.class.getName());
		buf.append("(");
		buf.append(keyBuf);
		buf.append(", ");
		buf.append(valueBuf);
		buf.append(")");
		return buf.toString();
	}
	
	private String toTypeCode(List<String> names, List<Class<?>> types) {
		StringBuilder buf = new StringBuilder();
		buf.append("new ");
		buf.append(OrderedMap.class.getName());
		buf.append("(");
		if (names == null || names.size() == 0) {
			buf.append("new String[0]");
		} else {
			buf.append("new String[] {");
			boolean first = true;
			for (String str : names) {
				if (first) {
					first = false;
				} else {
					buf.append(", ");
				}
				buf.append("\"");
				buf.append(StringUtils.escapeString(str));
				buf.append("\"");
			}
			buf.append("}");
		}
		buf.append(", ");
		if (names == null || names.size() == 0) {
			buf.append("new Class[0]");
		} else {
			buf.append("new Class[] {");
			boolean first = true;
			for (Class<?> cls : types) {
				if (first) {
					first = false;
				} else {
					buf.append(", ");
				}
				buf.append(cls.getCanonicalName());
				buf.append(".class");
			}
			buf.append("}");
		}
		buf.append(")");
		return buf.toString();
	}

	private String getTemplateClassName(Template template, boolean stream) {
		String name = template.getName();
		String encoding = template.getEncoding();
		Locale locale = template.getLocale();
		long lastModified = template.getLastModified();
		StringBuilder buf = new StringBuilder(name.length() + 40);
		if (template instanceof Macro) {
			Node parent = ((Macro) template).getParent();
			if (parent instanceof Template) {
				buf.append(((Template) parent).getName());
				buf.append("_");
			}
		}
		buf.append(name);
		if (StringUtils.isNotEmpty(engineName)) {
			buf.append("_");
			buf.append(engineName);
		}
		if (StringUtils.isNotEmpty(encoding)) {
			buf.append("_");
			buf.append(encoding);
		}
		if (locale != null) {
			buf.append("_");
			buf.append(locale);
		}
		if (lastModified > 0) {
			buf.append("_");
			buf.append(lastModified);
		}
		buf.append(stream ? "_stream" : "_writer");
		return TEMPLATE_CLASS_PREFIX + StringUtils.getVaildName(buf.toString());
	}
	
	private String getTextPart(String txt, Filter filter, String filterKey, StringBuilder textFields, AtomicInteger seq, boolean stream, boolean string) {
		if (StringUtils.isNotEmpty(txt)) {
			if (filter != null) {
				txt = filter.filter(filterKey, txt);
			}
			String var = "$TXT" + seq.incrementAndGet();
			if (string) {
				if (textInClass) {
					textFields.append("private static final String " + var + " = \"" + StringUtils.escapeString(txt) + "\";\n");
				} else {
					String txtId = StringCache.put(txt);
					textFields.append("private static final String " + var + " = " + StringCache.class.getName() +  ".getAndRemove(\"" + txtId + "\");\n");
				}
			} else if (stream) {
				if (textInClass) {
					textFields.append("private static final byte[] " + var + " = new byte[] {" + StringUtils.toByteString(StringUtils.toBytes(txt, outputEncoding)) + "};\n");
				} else {
					String txtId = ByteCache.put(StringUtils.toBytes(txt, outputEncoding));
					textFields.append("private static final byte[] " + var + " = " + ByteCache.class.getName() +  ".getAndRemove(\"" + txtId + "\");\n");
				}
			} else {
				if (textInClass) {
					textFields.append("private static final char[] " + var + " = new char[] {" + StringUtils.toCharString(txt.toCharArray()) + "};\n");
				} else {
					String txtId = CharCache.put(txt.toCharArray());
					textFields.append("private static final char[] " + var + " = " + CharCache.class.getName() +  ".getAndRemove(\"" + txtId + "\");\n");
				}
			}
			return var;
		}
		return "";
	}

}
