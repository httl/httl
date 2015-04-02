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
import httl.Resource;
import httl.Template;
import httl.ast.AstVisitor;
import httl.ast.BinaryOperator;
import httl.ast.BreakDirective;
import httl.ast.Constant;
import httl.ast.ElseDirective;
import httl.ast.Expression;
import httl.ast.ForDirective;
import httl.ast.IfDirective;
import httl.ast.MacroDirective;
import httl.ast.Operator;
import httl.ast.SetDirective;
import httl.ast.Statement;
import httl.ast.Text;
import httl.ast.UnaryOperator;
import httl.ast.ValueDirective;
import httl.ast.Variable;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Switcher;
import httl.spi.formatters.MultiFormatter;
import httl.util.ByteCache;
import httl.util.CharCache;
import httl.util.ClassUtils;
import httl.util.CollectionUtils;
import httl.util.IOUtils;
import httl.util.LinkedStack;
import httl.util.OrderedMap;
import httl.util.ParameterizedTypeImpl;
import httl.util.Status;
import httl.util.StringCache;
import httl.util.StringUtils;
import httl.util.VolatileReference;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CompileVisitor
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CompiledVisitor extends AstVisitor {

	private LinkedStack<Type> typeStack = new LinkedStack<Type>();

	private LinkedStack<String> codeStack = new LinkedStack<String>();

	private Map<String, Class<?>> variableTypes = new HashMap<String, Class<?>>();

	private StringBuilder builder = new StringBuilder();

	private StringBuilder textFields = new StringBuilder();
	
	private String filterKey = null;
	
	private final VolatileReference<Filter> filterReference = new VolatileReference<Filter>();
	
	private final Set<String> setVariables = new HashSet<String>();

	private final Set<String> getVariables = new HashSet<String>();

	private final List<String> defVariables = new ArrayList<String>();
	
	private final List<Class<?>> defVariableTypes = new ArrayList<Class<?>>();
	
	private final Map<String, Type> types = new HashMap<String, Type>();

	private Map<String, Class<?>> parameterTypes;
	
	private final Map<String, Class<?>> returnTypes = new HashMap<String, Class<?>>();
	
	private final Map<String, Class<?>> macros = new HashMap<String, Class<?>>();
	
	private Resource resource;

	private Node node;

	private int offset;
	
	private boolean stream;

	private String engineName;

	private String[] forVariable;

	private String[] importGetters;

	private String[] importSizers;

	private String filterVariable;

	private String formatterVariable;

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

	private static final String TEMPLATE_CLASS_PREFIX = CompiledTemplate.class.getPackage().getName() + ".Template_";
	
	private final AtomicInteger seq = new AtomicInteger();

	private boolean sourceInClass;

	private boolean textInClass;
	
	private String outputEncoding;
	
	private Class<?> defaultVariableType;
	
	private Compiler compiler;

	public void setCompiler(Compiler compiler) {
		this.compiler = compiler;
	}

	public void setForVariable(String[] forVariable) {
		this.forVariable = forVariable;
	}

	public void setImportSizers(String[] importSizers) {
		this.importSizers = importSizers;
	}

	public void setTypes(Map<String, Class<?>> types) {
		this.parameterTypes = types;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setNode(Node node) {
		this.node = node;
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

	public CompiledVisitor() {
	}

	@Override
	public boolean visit(Statement node) throws IOException, ParseException {
		boolean result = super.visit(node);
		filterKey = node.toString();
		return result;
	}

	@Override
	public void visit(Text node) throws IOException, ParseException {
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
						String part = getTextPart(txt.substring(begin, end), filter, false);
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
		String part = getTextPart(txt, filter, false);
		if (StringUtils.isNotEmpty(part)) {
			builder.append("	$output.write(" + part + ");\n");
		}
	}

	@Override
	public void visit(ValueDirective node) throws IOException, ParseException {
		boolean nofilter = node.isNoFilter();
		String code = popExpressionCode();
		Class<?> returnType = popExpressionReturnClass();
		Map<String, Class<?>> variableTypes = popExpressionVariableTypes();
		getVariables.addAll(variableTypes.keySet());
		if (Template.class.isAssignableFrom(returnType)) {
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
				builder.append(".openStream()");
			} else {
				builder.append(".openReader()");
			}
			builder.append(", $output);\n");
		} else {
			if (Object.class.equals(returnType)) {
				if (! StringUtils.isNamed(code)) {
					code = "(" + code + ")";
				}
				builder.append("	if (");
				builder.append(code);
				builder.append(" instanceof ");
				builder.append(Template.class.getName());
				builder.append(") {\n	((");
				builder.append(Template.class.getName());
				builder.append(")");
				builder.append(code);
				builder.append(").render($output);\n	}");
				if (nofilter) {
					builder.append(" else if (");
					builder.append(code);
					builder.append(" instanceof ");
					builder.append(Resource.class.getName());
					builder.append(") {\n	");
					builder.append(IOUtils.class.getName());
					builder.append(".copy(((");
					builder.append(Resource.class.getName());
					builder.append(")");
					builder.append(code);
					if (stream) {
						builder.append(").openStream()");
					} else {
						builder.append(").openReader()");
					}
					builder.append(", $output);\n	}");
				} else {
					code = "(" + code + " instanceof " + Resource.class.getName() + " ? " 
							+ IOUtils.class.getName() + ".readToString(((" + Resource.class.getName() + ")" 
							+ code + ").openReader()) : " + code + ")";
				}
				builder.append(" else {\n");
			} else if (Resource.class.isAssignableFrom(returnType)) {
				if (! StringUtils.isNamed(code)) {
					code = "(" + code + ")";
				}
				code = "(" + code + " == null ? null : " + IOUtils.class.getName() + ".readToString(" + code + ".openReader()))";
			}
			getVariables.add(formatterVariable);
			String key = getTextPart(node.getExpression().toString(), null, true);
			if (! stream && Object.class.equals(returnType)) {
				String pre = "";
				String var = "$obj" + seq.getAndIncrement();
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
			if (Object.class.equals(returnType)) {
				builder.append("	}\n");
			}
		}
	}

	@Override
	public void visit(SetDirective node) throws IOException, ParseException {
		Type type = node.getType();
		Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
		if (node.getExpression() != null) {
			String code = popExpressionCode();
			Class<?> returnType = popExpressionReturnClass();
			Map<String, Class<?>> variableTypes = popExpressionVariableTypes();
			if (clazz == null) {
				clazz = returnType;
			}
			appendVar(clazz, node.getName(), code, node.isExport(), node.isHide(), node.getType() != null, node.getOffset());
			getVariables.addAll(variableTypes.keySet());
		} else {
			clazz = checkVar(clazz, node.getName(), node.getOffset());
			types.put(node.getName(), type);
			defVariables.add(node.getName());
			defVariableTypes.add(clazz);
		}
	}
	
	private Class<?> checkVar(Class<?> clazz, String var, int offset) throws IOException, ParseException {
		Type type = types.get(var);
		Class<?> cls = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
		if (cls != null && ! cls.equals(clazz) 
				&& ! cls.isAssignableFrom(clazz) 
				&& ! clazz.isAssignableFrom(cls)) {
			throw new ParseException("Defined different type variable " 
				+ var + ", conflict types: " + cls.getCanonicalName() + ", " + clazz.getCanonicalName() + ".", offset);
		}
		if (cls != null && clazz.isAssignableFrom(cls)) {
			return cls;
		}
		return clazz;
	}

	private void appendVar(Type type, String var, String code, boolean parent, boolean hide, boolean def, int offset) throws IOException, ParseException {
		Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
		clazz = checkVar(clazz, var, offset);
		String typeName = clazz.getCanonicalName();
		if (def || types.get(var) == null) {
			types.put(var, type);
		}
		setVariables.add(var);
		builder.append("	" + var + " = (" + typeName + ")(" + code + ");\n");
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
	public boolean visit(IfDirective node) throws IOException, ParseException {
		String code = popExpressionCode();
		Class<?> returnType = popExpressionReturnClass();
		Map<String, Class<?>> variableTypes = popExpressionVariableTypes();
		builder.append("	if(");
		builder.append(StringUtils.getConditionCode(returnType, code, importSizers));
		builder.append(") {\n");
		getVariables.addAll(variableTypes.keySet());
		return true;
	}

	@Override
	public void end(IfDirective node) throws IOException, ParseException {
		builder.append("	}\n");
	}

	@Override
	public boolean visit(ElseDirective node) throws IOException, ParseException {
		if (node.getExpression() == null) {
			builder.append("	else {\n");
		} else {
			String code = popExpressionCode();
			Class<?> returnType = popExpressionReturnClass();
			Map<String, Class<?>> variableTypes = popExpressionVariableTypes();builder.append("	else if (");
			builder.append(StringUtils.getConditionCode(returnType, code, importSizers));
			builder.append(") {\n");
			getVariables.addAll(variableTypes.keySet());
		}
		return true;
	}

	@Override
	public void end(ElseDirective node) throws IOException, ParseException {
		builder.append("	}\n");
	}

	private Class<?> findGenericTypeByName(String name, int index) {
		return findGenericType(types.get(name), index);
	}

	private Class<?> findGenericType(Type type, int index) {
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] ts = pt.getActualTypeArguments();
			if (index < ts.length) {
				Type t = ts[index];
				return (Class<?>) (t instanceof ParameterizedType ? ((ParameterizedType) t).getRawType() : t);
			}
		}
		return null;
	}

	@Override
	public boolean visit(ForDirective node) throws IOException, ParseException {
		String var = node.getName();
		Type type  = node.getType();
		Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
		String code = popExpressionCode();
		Type returnType = popExpressionReturnType();
		Class<?> returnClass = (Class<?>) (returnType instanceof ParameterizedType ? ((ParameterizedType)returnType).getRawType() : returnType);
		Map<String, Class<?>> variableTypes = popExpressionVariableTypes();
		if (type == null) {
			if (returnClass != null) {
				if (returnClass.isArray()) {
					type = returnClass.getComponentType();
				} else if (Map.class.isAssignableFrom(returnClass)) {
					if (returnType instanceof ParameterizedType) {
						type = new ParameterizedTypeImpl(Map.Entry.class, ((ParameterizedType) returnType).getActualTypeArguments());
					} else {
						type = Map.Entry.class;
					}
				} else if (Collection.class.isAssignableFrom(returnClass)) {
					type = findGenericType(returnType, 0); // Collection<T>泛型 
				}
			}
			if (type == null) {
				if (defaultVariableType == null) {
					throw new ParseException("Can not resolve the variable " + node.getName() + " type in the #for directive. Please explicit define the variable type #for(Xxx " + node.getName() + " : " + node.getExpression() + ") in your template.", node.getOffset());
				}
				type = defaultVariableType;
			}
		}
		clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
		if (Map.class.isAssignableFrom(returnClass)) {
			code = ClassUtils.class.getName() + ".entrySet(" + code + ")";
		}
		int i = seq.incrementAndGet();
		String dataName = "_d_" + i;
		String sizeName = "_s_" + i;
		String name = "_i_" + var;
		builder.append("	" + Object.class.getSimpleName() + " " + dataName + " = " + code + ";\n");
		builder.append("	int " + sizeName + " = " + ClassUtils.class.getName() + ".getSize(" + dataName + ");\n");
		builder.append("	if (" + dataName + " != null && " + sizeName + " != 0) {\n");
		builder.append("	");
		for (String fv : forVariable) {
			builder.append(ClassUtils.filterJavaKeyword(fv));
			builder.append(" = ");
		}
		builder.append("new " + Status.class.getName() + "(" + ClassUtils.filterJavaKeyword(forVariable[0]) + ", " + dataName + ", " + sizeName + ");\n");
		builder.append("	for (" + Iterator.class.getName() + " " + name + " = " + CollectionUtils.class.getName() + ".toIterator(" + dataName + "); " + name + ".hasNext();) {\n");
		String varCode;
		if (clazz.isPrimitive()) {
			varCode = ClassUtils.class.getName() + ".unboxed((" + ClassUtils.getBoxedClass(clazz).getSimpleName() + ")" + name + ".next())";
		} else {
			varCode = name + ".next()";
		}
		appendVar(type, var, varCode, false, false, node.getType() != null, node.getOffset());
		getVariables.addAll(variableTypes.keySet());
		for (String fv : forVariable) {
			setVariables.add(fv);
		}
		return true;
	}

	@Override
	public void end(ForDirective node) throws IOException, ParseException {
		builder.append("	" + ClassUtils.filterJavaKeyword(forVariable[0]) + ".increment();\n	}\n	");
		for (String fv : forVariable) {
			builder.append(ClassUtils.filterJavaKeyword(fv));
			builder.append(" = ");
		}
		builder.append(ClassUtils.filterJavaKeyword(forVariable[0]) + ".getParent();\n	}\n");
	}

	@Override
	public void visit(BreakDirective node) throws IOException, ParseException {
		String b = node.getParent() instanceof ForDirective ? "break" : "return";
		if (node.getExpression() == null) {
			if (! (node.getParent() instanceof IfDirective
					|| node.getParent() instanceof ElseDirective)) {
				throw new ParseException("Can not #break without condition. Please use #break(condition) or #if(condition) #break #end.", node.getOffset());
			}
			builder.append("	" + b + ";\n");
		} else {
			String code = popExpressionCode();
			Class<?> returnType = popExpressionReturnClass();
			Map<String, Class<?>> variableTypes = popExpressionVariableTypes();
			builder.append("	if(");
			builder.append(StringUtils.getConditionCode(returnType, code, importSizers));
			builder.append(") " + b + ";\n");
			getVariables.addAll(variableTypes.keySet());
		}
	}

	@Override
	public boolean visit(MacroDirective node)  throws IOException, ParseException {
		types.put(node.getName(), Template.class);
		CompiledVisitor visitor = new CompiledVisitor();
		visitor.setResource(resource);
		visitor.setNode(node);
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
		for (Node n : node.getChildren()) {
			n.accept(visitor);
		}
		Class<?> macroType = visitor.compile();
		macros.put(node.getName(), macroType);
		return false;
	}

	public void init() {
		if (importTypes != null && importTypes.size() > 0) {
			types.putAll(importTypes);
		}
		if (parameterTypes != null && parameterTypes.size() > 0) {
			types.putAll(parameterTypes);
		}
		types.put("this", Template.class);
		types.put("super", Template.class);
		types.put(defaultFilterVariable, Filter.class);
		types.put(filterVariable, Filter.class);
		types.put(defaultFormatterVariable, Formatter.class);
		types.put(formatterVariable, Formatter.class);
		for (String fv : forVariable) {
			types.put(fv, Status.class);
		}
		for (String macro : importMacroTemplates.keySet()) {
			types.put(macro, Template.class);
		}
	}

	public Class<?> compile() throws IOException, ParseException {
		String code = getCode();
		return compiler.compile(code);
	}

	private String getCode() throws IOException, ParseException {
		String name = getTemplateClassName(resource, node, stream);
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
				Type type = types.get(var);
				if (type == null) {
					type = defaultVariableType;
				}
				Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
				defined.add(var);
				declare.append(getTypeCode(clazz, var));
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
				Type type = types.get(var);
				Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
				String typeName = getTypeName(clazz);
				declare.append("	" + typeName + " " + ClassUtils.filterJavaKeyword(var) + " = " + ClassUtils.getInitCode(clazz) + ";\n");
			}
		}
		for (String var : getVariables) {
			if (! defined.contains(var)) {
				Type type = types.get(var);
				if (type == null) {
					type = defaultVariableType;
				}
				Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
				defined.add(var);
				declare.append(getTypeCode(clazz, var));
				defVariables.add(var);
				defVariableTypes.add(clazz);
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
		textFields.append("private static final " + Map.class.getName() + " $VARS = " + toTypeCode(defVariables, defVariableTypes) + ";\n");
		
		String templateName = resource.getName();
		Node macro = node;
		while (macro instanceof MacroDirective) {
			templateName += "#" + ((MacroDirective) macro).getName();
			macro = ((MacroDirective) macro).getParent();
		}
		
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
				+ Map.class.getName() + " importMacros, " 
				+ Resource.class.getName() + " resource, " 
				+ Template.class.getName() + " parent, " 
				+ Node.class.getName() + " root) {\n" 
				+ "	super(engine, interceptor, compiler, filterSwitcher, formatterSwitcher, filter, formatter, mapConverter, outConverter, functions, importMacros, resource, parent, root);\n"
				+ functionInits
				+ macroInits
				+ "}\n"
				+ "\n"
				+ "protected void doRender" 
				+ (stream ? "Stream" : "Writer")
				+ "(" + Context.class.getName() + " $context, " 
				+ (stream ? OutputStream.class.getName() : Writer.class.getName())
				+ " $output) throws " + Exception.class.getName() + " {\n" 
				+ methodCode
				+ "}\n"
				+ "\n"
				+ "public " + String.class.getSimpleName() + " getName() {\n"
				+ "	return \"" + templateName + "\";\n"
				+ "}\n"
				+ "\n"
				+ "public " + Map.class.getName() + " getVariables() {\n"
				+ "	return $VARS;\n"
				+ "}\n"
				+ "\n"
				+ "protected " + Map.class.getName() + " getMacroTypes() {\n"
				+ "	return " + toTypeCode(macros) + ";\n"
				+ "}\n"
				+ "\n"
				+ "public boolean isMacro() {\n"
				+ "	return " + (node instanceof MacroDirective) + ";\n"
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

	private String getTemplateClassName(Resource resource, Node node, boolean stream) {
		String name = resource.getName();
		String encoding = resource.getEncoding();
		Locale locale = resource.getLocale();
		long lastModified = resource.getLastModified();
		StringBuilder buf = new StringBuilder(name.length() + 40);
		buf.append(name);
		Node macro = node;
		while (macro instanceof MacroDirective) {
			buf.append("_");
			buf.append(((MacroDirective) macro).getName());
			macro = ((MacroDirective) macro).getParent();
		}
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
	
	private String getTextPart(String txt, Filter filter, boolean string) {
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

	private String popExpressionCode() {
		String code = codeStack.pop();
		if (! codeStack.isEmpty()) {
			throw new IllegalStateException("Illegal expression.");
		}
		return code;
	}

	private Type popExpressionReturnType() {
		Type type = typeStack.pop();
		if (! typeStack.isEmpty()) {
			throw new IllegalStateException("Illegal expression.");
		}
		return type;
	}
	
	private Class<?> popExpressionReturnClass() {
		Type type = popExpressionReturnType();
		return (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
	}

	private Map<String, Class<?>> popExpressionVariableTypes() {
		Map<String, Class<?>> types = variableTypes;
		variableTypes = new HashMap<String, Class<?>>();
		return types;
	}

	public void visit(Constant node) throws IOException, ParseException {
		Object value = node.getValue();
		Class<?> type;
		String code;
		if (value == null) {
			type = node.isBoxed() ? void.class : null;
			code = node.isBoxed() ? "" : "null";
		} else if (value.equals(Boolean.TRUE)) {
			type = node.isBoxed() ? Boolean.class : boolean.class;
			code = node.isBoxed() ? "Boolean.TRUE" : "true";
		} else if (value.equals(Boolean.FALSE)) {
			type = node.isBoxed() ? Boolean.class : boolean.class;
			code = node.isBoxed() ? "Boolean.FALSE" : "false";
		} else if (value instanceof String) {
			type = String.class;
			code = "\"" + StringUtils.escapeString((String) value) + "\"";
		} else if (value instanceof Character) {
			type = node.isBoxed() ? Character.class : char.class;
			code = node.isBoxed() ? "Character.valueOf('" + StringUtils.escapeString(String.valueOf(value)) + "')" 
					: "'" + StringUtils.escapeString(String.valueOf(value)) + "'";
		} else if (value instanceof Double) {
			type = node.isBoxed() ? Double.class : double.class;
			code = node.isBoxed() ? "Double.valueOf(" + value + "d)" : value + "d";
		} else if (value instanceof Float) {
			type = node.isBoxed() ? Float.class : float.class;
			code = node.isBoxed() ? "Float.valueOf(" + value + "f)" : value + "f";
		} else if (value instanceof Long) {
			type = node.isBoxed() ? Long.class : long.class;
			code = node.isBoxed() ? "Long.valueOf(" + value + "l)" : value + "l";
		} else if (value instanceof Short) {
			type = node.isBoxed() ? Short.class : short.class;
			code = node.isBoxed() ? "Short.valueOf((short)" + value + ")" : "((short)" + value + ")";
		} else if (value instanceof Byte) {
			type = node.isBoxed() ? Byte.class : byte.class;
			code = node.isBoxed() ? "Byte.valueOf((byte)" + value + ")" : "((byte)" + value + ")";
		} else if (value instanceof Integer) {
			type = node.isBoxed() ? Integer.class : int.class;
			code = node.isBoxed() ? "Integer.valueOf(" + value + ")" : String.valueOf(value);
		} else if (value instanceof Class) {
			type = node.isBoxed() ? ClassUtils.getBoxedClass((Class<?>) value) : (Class<?>) value;
			code = ((Class<?>) value).getCanonicalName();
		} else {
			throw new ParseException("Unsupported constant " + value, node.getOffset());
		}
		typeStack.push(type);
		codeStack.push(code);
	}
	
	public void visit(Variable node) throws IOException, ParseException {
		String name = node.getName();
		Type type = types.get(name);
		if (type == null) {
			if (defaultVariableType == null) {
				throw new ParseException("Can not resolve the " + node.getName() + " variable type. Please explicit define the variable type #set(Xxx " + node.getName() + ") in your template.", node.getOffset());
			}
			type = defaultVariableType;
		}
		Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
		String code = ClassUtils.filterJavaKeyword(name);
		typeStack.push(type);
		codeStack.push(code);
		variableTypes.put(name, clazz);
	}

	@Override
	public void visit(UnaryOperator node) throws IOException, ParseException {
		Type parameterType = typeStack.pop();
		String parameterCode = codeStack.pop();
		Class<?> parameterClass = (Class<?>) (parameterType instanceof ParameterizedType ? ((ParameterizedType)parameterType).getRawType() : parameterType);
		String name = node.getName();
		
		Type type = null;
		String code = null;
		Class<?>[] parameterTypes;
		if (parameterType instanceof ParameterizedType) {
			Type raw = ((ParameterizedType) parameterType).getRawType();
			if (raw == Object[].class) {
				parameterTypes = (Class<?>[]) ((ParameterizedType) parameterType).getActualTypeArguments();
			} else if (raw == void.class) {
				parameterTypes = new Class<?>[0];
			} else {
				parameterTypes = new Class<?>[] { (Class<?>) raw };
			}
		} else if (parameterClass == void.class) {
			parameterTypes = new Class<?>[0];
		} else {
			parameterTypes = new Class<?>[] { parameterClass };
		}
		if (name.startsWith("new ")) {
			String clsName = name.substring(4);
			type = ClassUtils.forName(importPackages, clsName);
			code = name + "(" + parameterCode + ")";
		} else if (name.startsWith("(") && name.endsWith(")")) {
			String clsName = name.substring(1, name.length() - 1);
			type = ClassUtils.forName(importPackages, clsName);
			code = "(" + name + "(" + parameterCode + "))";
		} else {
			Type macroType = types.get(name);
			Class<?> t = (Class<?>) (macroType instanceof ParameterizedType ? ((ParameterizedType) macroType).getRawType() : macroType);
			if (t != null && Template.class.isAssignableFrom(t)) {
				variableTypes.put(name, Template.class);
				type = Object.class;
				code = "(" + name + " == null ? null : " + name + ".evaluate(new Object" + (parameterCode.length() == 0 ? "[0]" : "[] { " + parameterCode + " }") + "))";
			} else {
				name = ClassUtils.filterJavaKeyword(name);
				type = null;
				code = null;
				if (functions != null && functions.size() > 0) {
					for (Class<?> function : functions.keySet()) {
						try {
							Method method = ClassUtils.searchMethod(function, name, parameterTypes, parameterTypes.length == 1);
							if (Object.class.equals(method.getDeclaringClass())) {
								break;
							}
							type = method.getReturnType();
							if (type == void.class) {
								throw new ParseException("Can not call void method " + method.getName() + " in class " + function.getName(), node.getOffset());
							}
							Class<?>[] pts = method.getParameterTypes();
							if (parameterTypes.length == 1 && parameterTypes[0].isPrimitive() 
									&& pts[0].isAssignableFrom(ClassUtils.getBoxedClass(parameterTypes[0]))) {
								parameterCode = ClassUtils.class.getName() + ".boxed(" + parameterCode + ")";
							}
							if (Modifier.isStatic(method.getModifiers())) {
								code = function.getName() + "." + method.getName() + "(" + parameterCode + ")";
							} else {
								code = "$" + function.getName().replace('.', '_') + "." + method.getName() + "(" + parameterCode + ")";
							}
							break;
						} catch (NoSuchMethodException e) {
						}
					}
				}
				if (code == null) {
					throw new ParseException("No such macro \"" + name + "\" or import method " + ClassUtils.getMethodFullName(name, parameterTypes) + ".", node.getOffset());
				}
			}
		}
		
		typeStack.push(type);
		codeStack.push(code);
	}

	@Override
	public void visit(BinaryOperator node) throws IOException, ParseException {
		Type rightType = typeStack.pop();
		String rightCode = codeStack.pop();
		
		Type leftType = typeStack.pop();
		String leftCode = codeStack.pop();
		
		Class<?> rightClass = (Class<?>) (rightType instanceof ParameterizedType ? ((ParameterizedType)rightType).getRawType() : rightType);
		Class<?> leftClass = (Class<?>) (leftType instanceof ParameterizedType ? ((ParameterizedType)leftType).getRawType() : leftType);

		if (leftClass == null)
			leftClass = Object.class;
		
		String name = node.getName();
		if ("null".equals(leftCode)) {
			leftCode = "((" + (leftClass == null ? Object.class.getSimpleName() : leftClass.getClass().getCanonicalName()) + ") " + leftCode + ")";
		} else if (node.getLeftParameter() instanceof Operator
				&& ((Operator) node.getLeftParameter()).getPriority() < node.getPriority()) {
			leftCode = "(" + leftCode + ")";
		}
		
		Type type = null;
		String code = null;
		if ("to".equals(name) 
				&& node.getRightParameter() instanceof Constant
				&& rightType == String.class
				&& rightCode.length() > 2
				&& rightCode.startsWith("\"") && rightCode.endsWith("\"")) {
			code = "((" + rightCode.substring(1, rightCode.length() - 1) + ")" + leftCode + ")";
			type = ClassUtils.forName(importPackages, rightCode.substring(1, rightCode.length() - 1));
		} else if ("class".equals(name)) {
			type = Class.class;
			if (leftClass.isPrimitive()) {
				code = leftClass.getCanonicalName() + ".class";
			} else {
				code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, leftCode + ".getClass()");
			}
		} else if (Map.Entry.class.isAssignableFrom(leftClass)
				&& ("key".equals(name) || "value".equals(name))) {
			String var = getGenericVariableName(node.getLeftParameter());
			if (var != null) {
				Class<?> keyType = findGenericTypeByName(var, 0); // Map<K,V>第一个泛型
				Class<?> valueType = findGenericTypeByName(var, 1); // Map<K,V>第二个泛型
				if ("key".equals(name) && keyType != null) {
					type = keyType;
					code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, "((" + keyType.getCanonicalName() + ")" + leftCode + ".getKey(" + rightCode + "))");
				} else if ("value".equals(name) && valueType != null) {
					type = valueType;
					code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, "((" + valueType.getCanonicalName() + ")" + leftCode + ".getValue(" + rightCode + "))");
				}
			}
		} else if (Map.class.isAssignableFrom(leftClass)
				&& "get".equals(name)) {
			String var = getGenericVariableName(node.getLeftParameter());
			if (var != null) {
				Class<?> varType = findGenericTypeByName(var, 1); // Map<K,V>第二个泛型 
				if (varType != null) {
					type = varType;
					if (rightClass.isPrimitive()) {
						rightCode = ClassUtils.class.getName() + ".boxed(" + rightCode + ")";
					}
					code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, "((" + varType.getCanonicalName() + ")" + leftCode + ".get(" + rightCode + "))");
				}
			}
		} else if (List.class.isAssignableFrom(leftClass)
				&& "get".equals(name)
				&& (int.class.equals(rightType) || Integer.class.equals(rightType))) {
			String var = getGenericVariableName(node.getLeftParameter());
			if (var != null) {
				Class<?> varType = findGenericTypeByName(var, 0); // List<T>第一个泛型
				if (varType != null) {
					type = varType;
					if (! rightClass.isPrimitive()) {
						rightCode = ClassUtils.class.getName() + ".unboxed(" + rightCode + ")";
					}
					code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, "((" + varType.getCanonicalName() + ")" + leftCode + ".get(" + rightCode + "))");
				}
			}
		}
		if (code == null) {
			Class<?>[] rightTypes;
			if (rightType instanceof ParameterizedType) {
				Type raw = ((ParameterizedType) rightType).getRawType();
				if (raw == Object[].class) {
					Type[] ts = ((ParameterizedType) rightType).getActualTypeArguments();
					rightTypes = new Class<?>[ts.length];
					for (int i = 0; i < ts.length; i ++) {
						Type t = ts[i];
						rightTypes[i] = (Class<?>) (t instanceof ParameterizedType ? ((ParameterizedType)t).getRawType() : t);
					}
				} else if (raw == void.class) {
					rightTypes = new Class<?>[0];
				} else {
					rightTypes = new Class<?>[] { (Class<?>) raw };
				}
			} else if (rightClass == void.class) {
				rightTypes = new Class<?>[0];
			} else {
				rightTypes = new Class<?>[] { rightClass };
			}
			if (Template.class.isAssignableFrom(leftClass)
					&& ! hasMethod(Template.class, name, rightTypes)) {
				type = Object.class;
				code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, CompiledVisitor.class.getName() + ".getMacro(" + leftCode + ", \"" + name + "\").evaluate(new Object" + (rightCode.length() == 0 ? "[0]" : "[] { " + rightCode + " }") + ")");
			} else if (Map.class.isAssignableFrom(leftClass)
					&& rightTypes.length == 0
					&& ! hasMethod(Map.class, name, rightTypes)) {
				type = Object.class;
				code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, leftCode + ".get(\"" + name + "\")");
				String var = getGenericVariableName(node.getLeftParameter());
				if (var != null) {
					Class<?> t = findGenericTypeByName(var, 1); // Map<K,V>第二个泛型 
					if (t != null) {
						type = t;
						code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, "((" + t.getCanonicalName() + ")" + leftCode + ".get(\"" + name + "\"))");
					}
				}
			} else if (importGetters != null && importGetters.length > 0
					&& rightTypes.length == 0 
					&& ! hasMethod(leftClass, name, rightTypes)) {
				for (String getter : importGetters) {
					if (hasMethod(leftClass, getter, new Class<?>[] { String.class })
							|| hasMethod(leftClass, getter, new Class<?>[] { Object.class })) {
						type = Object.class;
						code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, leftCode + "." + getter + "(\"" + name + "\")");
						break;
					}
				}
			}
			name = ClassUtils.filterJavaKeyword(name);
			if (functions != null && functions.size() > 0) {
				Class<?>[] allTypes;
				String allCode;
				if (rightTypes == null || rightTypes.length == 0) {
					allTypes = new Class<?>[] {leftClass};
					allCode = leftCode;
				} else {
					allTypes = new Class<?>[rightTypes.length + 1];
					allTypes[0] = leftClass;
					System.arraycopy(rightTypes, 0, allTypes, 1, rightTypes.length);
					allCode = leftCode + ", " + rightCode;
				}
				for (Class<?> function : functions.keySet()) {
					try {
						Method method = ClassUtils.searchMethod(function, name, allTypes, allTypes.length == 2);
						if (! Object.class.equals(method.getDeclaringClass())) {
							type = method.getReturnType();
							if (type == void.class) {
								throw new ParseException("Can not call void method " + method.getName() + " in class " + function.getName(), node.getOffset());
							}
							Type grt = method.getGenericReturnType();
							if (! (grt instanceof Class)) {
								Class<?>[] pts = method.getParameterTypes();
								Type[] gpts = method.getGenericParameterTypes();
								if (pts.length == allTypes.length && pts.length == gpts.length) {
									for (int i = 0; i < gpts.length; i ++) {
										Type pt = pts[i];
										Type gpt = gpts[i];
										if (pt.equals(type) && gpt.equals(grt)) {
											if (i == 0) {
												type = leftType;
											} else {
												type = allTypes[i];
											}
											break;
										}
									}
								}
							}
							Class<?>[] pts = method.getParameterTypes();
							if (allTypes.length == 2) {
								if (allTypes[1] == null) {
									allCode = leftCode + ", " + "((" + (rightClass == null ? Object.class.getSimpleName() : rightClass.getClass().getCanonicalName()) + ") " + rightCode + ")";
								} else if (allTypes[1].isPrimitive() && pts[1].isAssignableFrom(ClassUtils.getBoxedClass(allTypes[1]))) {
									allCode = leftCode + ", " + ClassUtils.class.getName() + ".boxed(" + rightCode + ")";
								}
							}
							
							if (Modifier.isStatic(method.getModifiers())) {
								code = function.getName() + "." + method.getName() + "(" + allCode + ")";
							} else {
								code = "$" + function.getName().replace('.', '_') + "." + method.getName() + "(" + allCode + ")";
							}
							break;
						}
					} catch (NoSuchMethodException e) {
					}
				}
			}
			if (code == null) {
				if (leftClass.isArray() && "length".equals(name)) {
					type = int.class;
					code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, leftCode + ".length");
				} else {
					try {
						Method method = ClassUtils.searchMethod(leftClass, name, rightTypes);
						type = method.getReturnType();
						code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, leftCode + "." + method.getName() + "(" + rightCode + ")");
						if (type == void.class) {
							throw new ParseException("Can not call void method " + method.getName() + " in class " + leftClass.getName(), node.getOffset());
						}
					} catch (NoSuchMethodException e) {
						String def = "";
						if (StringUtils.isNamed(leftCode) && leftClass.equals(defaultVariableType)) {
							def = "Can not resolve the " + leftCode + " variable type. Please explicit define the variable type #set(Xxx " + leftCode + ") in your template.";
						}
						if (rightTypes != null && rightTypes.length > 0) {
							throw new ParseException(def + " No such method " + ClassUtils.getMethodFullName(name, rightTypes) + " in class "
									+ leftClass.getName() + ".", node.getOffset());
						} else { // search property
							try {
								String getter = "get" + name.substring(0, 1).toUpperCase()
										+ name.substring(1);
								Method method = leftClass.getMethod(getter,
										new Class<?>[0]);
								type = method.getReturnType();
								code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, leftCode + "." + method.getName() + "()");
								if (type == void.class) {
									throw new ParseException("Can not call void method " + method.getName() + " in class " + leftClass.getName(), node.getOffset());
								}
							} catch (NoSuchMethodException e2) {
								try {
									String getter = "is"
											+ name.substring(0, 1).toUpperCase()
											+ name.substring(1);
									Method method = leftClass.getMethod(getter,
											new Class<?>[0]);
									type = method.getReturnType();
									code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, leftCode + "." + method.getName() + "()");
									if (type == void.class) {
										throw new ParseException("Can not call void method " + method.getName() + " in class " + leftClass.getName(), node.getOffset());
									}
								} catch (NoSuchMethodException e3) {
									try {
										Field field = leftClass.getField(name);
										type = field.getType();
										code = getNotNullCode(node.getLeftParameter(), leftClass, leftCode, type, leftCode + "." + field.getName());
									} catch (NoSuchFieldException e4) {
										throw new ParseException(
												def + " No such property "
														+ name
														+ " in class "
														+ leftClass.getName()
														+ ", because no such method get"
														+ name.substring(0, 1)
																.toUpperCase()
														+ name.substring(1)
														+ "() or method is"
														+ name.substring(0, 1)
																.toUpperCase()
														+ name.substring(1)
														+ "() or method " + name
														+ "() or filed " + name + ".", node.getOffset());
									}
								}
							}
						}
					}
				}
			}
		}
		
		typeStack.push(type);
		codeStack.push(code);
	}

	private static String getGenericVariableName(Expression node) {
		if (node instanceof Variable) {
			return ((Variable)node).getName();
		}
		while (node instanceof BinaryOperator) {
			String name = ((BinaryOperator)node).getName();
			if ("+".equals(name) || "||".equals(name)
					 || "&&".equals(name)
					 || ".entrySet".equals(name)) {
				node = ((BinaryOperator)node).getLeftParameter();
				if (node instanceof Variable) {
					return ((Variable)node).getName();
				}
			} else {
				return null;
			}
		}
		return null;
	}

	public static Template getMacro(Template template, String name) {
		Template macro = template.getMacros().get(name);
		if (macro == null) {
			throw new IllegalStateException("No such macro " + name + "in template " + template.getName());
		}
		return macro;
	}
	
	private String getNotNullCode(Node leftParameter, Class<?> leftClass, String leftCode, Type type, String code) throws IOException, ParseException {
		Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
		return getNotNullCode(leftParameter, leftClass, leftCode, type, code, ClassUtils.getInitCodeWithType(clazz));
	}
	
	private String getNotNullCode(Node leftParameter, Class<?> leftClass, String leftCode, Type type, String code, String nullCode) throws IOException, ParseException {
		if (leftParameter instanceof Constant
				|| (leftClass != null && leftClass.isPrimitive())) {
			return code;
		}
		return "(" + leftCode + " == null ? " + nullCode + " : " + code + ")";
	}

	private boolean hasMethod(Class<?> leftClass, String name, Class<?>[] rightTypes) {
		if (leftClass == null) {
			return false;
		}
		if (leftClass.isArray() && "length".equals(name)) {
			return true;
		}
		try {
			Method method = ClassUtils.searchMethod(leftClass, name, rightTypes, false);
			return method != null;
		} catch (NoSuchMethodException e) {
			if (rightTypes != null && rightTypes.length > 0) {
				return false;
			} else { // search property
				try {
					String getter = "get" + name.substring(0, 1).toUpperCase()
							+ name.substring(1);
					Method method = leftClass.getMethod(getter,
							new Class<?>[0]);
					return method != null;
				} catch (NoSuchMethodException e2) {
					try {
						String getter = "is"
								+ name.substring(0, 1).toUpperCase()
								+ name.substring(1);
						Method method = leftClass.getMethod(getter,
								new Class<?>[0]);
						return method != null;
					} catch (NoSuchMethodException e3) {
						try {
							Field field = leftClass.getField(name);
							return field != null;
						} catch (NoSuchFieldException e4) {
							return false;
						}
					}
				}
			}
		}
	}

}