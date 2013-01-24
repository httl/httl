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
package httl.spi.parsers;

import httl.Context;
import httl.Engine;
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Logger;
import httl.spi.Parser;
import httl.spi.Switcher;
import httl.spi.Translator;
import httl.spi.parsers.templates.AbstractTemplate;
import httl.spi.parsers.templates.AdaptiveTemplate;
import httl.spi.parsers.templates.OutputStreamTemplate;
import httl.spi.parsers.templates.ResourceTemplate;
import httl.spi.parsers.templates.TemplateFormatter;
import httl.spi.parsers.templates.WriterTemplate;
import httl.spi.translators.expressions.ExpressionImpl;
import httl.util.ByteCache;
import httl.util.CharCache;
import httl.util.ClassUtils;
import httl.util.ForeachStatus;
import httl.util.IOUtils;
import httl.util.LocaleUtils;
import httl.util.OrderedMap;
import httl.util.StringCache;
import httl.util.StringUtils;
import httl.util.UnsafeStringWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AbstractParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setParser(Parser)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractParser implements Parser {
	
	protected static final char SPECIAL = '\27';

	protected static final char POUND = '#';
	
	protected static final char DOLLAR = '$';
	
	protected static final char POUND_SPECIAL = '\24';
	
	protected static final char DOLLAR_SPECIAL = '\25';
	
	protected static final String LEFT = "<" + SPECIAL;
	
	protected static final String RIGHT = SPECIAL + ">";
	
	protected static final Pattern DIRECTIVE_PATTERN = Pattern.compile(RIGHT + "([^" + SPECIAL + "]*)" + LEFT + "([0-9]*)([a-z]*)");
	
	protected static final Pattern EXPRESSION_PATTERN = Pattern.compile("([$#][!]?)\\{([^}]*)\\}");

	protected static final Pattern COMMA_PATTERN = Pattern.compile("\\s*\\,+\\s*");

	protected static final Pattern IN_PATTERN = Pattern.compile("(\\s+in\\s+)");

	protected static final Pattern ASSIGN_PATTERN = Pattern.compile(";\\s*(\\w+)\\s*(\\w*)\\s*([:\\.]?=)");

	protected static final Pattern ESCAPE_PATTERN = Pattern.compile("(\\\\+)([#$])");
	
	protected static final Pattern COMMENT_PATTERN = Pattern.compile("<!--##.*?-->", Pattern.DOTALL);

	protected static final Pattern CDATA_PATTERN = Pattern.compile("<!\\[CDATA\\[##(.*?)\\]\\]>", Pattern.DOTALL);
	
	protected static final Pattern VAR_PATTERN = Pattern.compile("([_0-9a-zA-Z>\\]]\\s[_0-9a-zA-Z]+)\\s?[,]?\\s?");

	protected static final Pattern BLANK_PATTERN = Pattern.compile("\\s+");
	
	protected static final String CDATA_LEFT = LEFT + "11" + RIGHT;
	
	protected static final String CDATA_RIGHT = LEFT + "3" + RIGHT;

	protected static final String VAR = "var";

	protected static final String SET = "set";

	protected static final String IF = "if";

	protected static final String ELSEIF = "elseif";

	protected static final String ELSE = "else";

	protected static final String FOREACH = "foreach";

	protected static final String BREAKIF = "breakif";

	protected static final String MACRO = "macro";

	protected static final String END = "end";

	protected String varDirective = VAR;

	protected String setDirective = SET;

	protected String ifDirective = IF;

	protected String elseifDirective = ELSEIF;

	protected String elseDirective = ELSE;

	protected String foreachDirective = FOREACH;

	protected String breakifDirective = BREAKIF;

	protected String macroDirective = MACRO;

	protected String endDirective = END;

	protected String foreachVariable = FOREACH;

	protected String filterVariable = "filter";

	protected String defaultFilterVariable = "$" + filterVariable;

	protected String version;
	
	protected Engine engine;
	
	protected Compiler compiler;
	
	protected Translator translator;
	
	protected Interceptor interceptor;
	
	protected Switcher textSwitcher;

	protected Switcher valueSwitcher;

	protected Filter templateFilter;

	protected Filter textFilter;

	protected Filter valueFilter;
	
	protected Converter<Object, Object> mapConverter;

	protected Converter<Object, Object> outConverter;

	protected Formatter<?> formatter;

	protected String[] importMacros;
   
	protected final Map<String, Template> importMacroTemplates = new ConcurrentHashMap<String, Template>();

	protected String[] importPackages;

	protected Set<String> importPackageSet;

	protected String[] importVariables;

	protected Map<String, Class<?>> importTypes;

	private final Map<Class<?>, Object> functions = new ConcurrentHashMap<Class<?>, Object>();

	protected static final String TEMPLATE_CLASS_PREFIX = AbstractTemplate.class.getPackage().getName() + ".Template_";
	
	protected static final Pattern SYMBOL_PATTERN = Pattern.compile("[^(_a-zA-Z0-9)]");

	protected final AtomicInteger TMP_VAR_SEQ = new AtomicInteger();
	
	protected boolean isOutputStream;

	protected boolean isOutputWriter;

	protected boolean sourceInClass;

	protected boolean textInClass;
	
	protected boolean removeDirectiveBlank;
	
	protected String outputEncoding;
	
	protected Logger logger;
	
	protected Class<?> defaultParameterType;

	/**
	 * httl.properties: default.parameter.type=java.lang.String
	 */
	public void setDefaultParameterType(String defaultParameterType) {
		this.defaultParameterType = ClassUtils.forName(defaultParameterType);
	}

	/**
	 * httl.properties: loggers=httl.spi.loggers.Log4jLogger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * httl.properties: import.macros=common.httl
	 */
	public void setImportMacros(String[] importMacros) {
		this.importMacros = importMacros;
	}

	/**
	 * httl.properties: output.encoding=UTF-8
	 */
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	/**
	 * httl.properties: engine=httl.spi.engines.DefaultEngine
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/**
	 * httl.properties: compiler=httl.spi.compilers.JdkCompiler
	 */
	public void setCompiler(Compiler compiler) {
		this.compiler = compiler;
	}

	/**
	 * httl.properties: translator=httl.spi.translators.DefaultTranslator
	 */
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}

	/**
	 * httl.properties: interceptors=httl.spi.interceptors.ExtendsInterceptor
	 */
	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}

	/**
	 * httl.properties: text.switchers=httl.spi.switchers.JavaScriptSwitcher
	 */
	public void setTextSwitcher(Switcher textSwitcher) {
		this.textSwitcher = textSwitcher;
	}

	/**
	 * httl.properties: value.switchers=httl.spi.switchers.JavaScriptSwitcher
	 */
	public void setValueSwitcher(Switcher valueSwitcher) {
		this.valueSwitcher = valueSwitcher;
	}

	/**
	 * httl.properties: template.filters=httl.spi.filters.CleanBlankLineFilter
	 */
	public void setTemplateFilter(Filter templateFilter) {
		this.templateFilter = templateFilter;
	}

	/**
	 * httl.properties: text.filters=httl.spi.filters.CompressBlankFilter
	 */
	public void setTextFilter(Filter filter) {
		this.textFilter = filter;
	}

	/**
	 * httl.properties: value.filters=httl.spi.filters.EscapeXmlFilter
	 */
	public void setValueFilter(Filter filter) {
		this.valueFilter = filter;
	}

	/**
	 * httl.properties: map.converters=httl.spi.converters.BeanMapConverter
	 */
	public void setMapConverter(Converter<Object, Object> mapConverter) {
		this.mapConverter = mapConverter;
	}

	/**
	 * httl.properties: out.converters=httl.spi.converters.ResponseOutConverter
	 */
	public void setOutConverter(Converter<Object, Object> outConverter) {
		this.outConverter = outConverter;
	}

	/**
	 * httl.properties: formatters=httl.spi.formatters.DateFormatter
	 */
	public void setFormatter(Formatter<?> formatter) {
		this.formatter = formatter;
	}

	/**
	 * httl.properties: output.stream=true
	 */
	public void setOutputStream(boolean outputStream) {
		this.isOutputStream = outputStream;
	}

	/**
	 * httl.properties: output.writer=true
	 */
	public void setOutputWriter(boolean outputWriter) {
		this.isOutputWriter = outputWriter;
	}

	/**
	 * httl.properties: source.in.class=false
	 */
	public void setSourceInClass(boolean sourceInClass) {
		this.sourceInClass = sourceInClass;
	}

	/**
	 * httl.properties: text.in.class=false
	 */
	public void setTextInClass(boolean textInClass) {
		this.textInClass = textInClass;
	}

	/**
	 * httl.properties: remove.directive.blank=true
	 */
	public void setRemoveDirectiveBlank(boolean removeDirectiveBlank) {
		this.removeDirectiveBlank = removeDirectiveBlank;
	}

	/**
	 * httl.properties: foreach.variable=foreach
	 */
	public void setForeachVariable(String foreachVariable) {
		this.foreachVariable = foreachVariable;
	}
	
	/**
	 * httl.properties: filter.variable=filter
	 */
	public void setFilterVariable(String filterVariable) {
		this.filterVariable = filterVariable;
		this.defaultFilterVariable = "$" + filterVariable;
	}

	/**
	 * httl.properties: java.version=1.7
	 */
	public void setJavaVersion(String version) {
		this.version = version;
	}

	/**
	 * httl.properties: import.packages=java.util
	 */
	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
		this.importPackageSet = new HashSet<String>(Arrays.asList(importPackages));
	}

	/**
	 * httl.properties: import.setVariables=javax.servlet.http.HttpServletRequest request
	 */
	public void setImportVariables(String[] importVariables) {
		this.importVariables = importVariables;
	}

	/**
	 * httl.properties: import.methods=java.lang.Math
	 */
	public void setImportMethods(Object[] importMethods) {
		for (Object function : importMethods) {
			if (function instanceof Class) {
				this.functions.put((Class<?>) function, function);
			} else {
				this.functions.put(function.getClass(), function);
			}
		}
	}
	
	/**
	 * init.
	 */
	public void init() {
		if (importVariables != null && importVariables.length > 0) {
			this.importTypes = new HashMap<String, Class<?>>();
			for (String var : importVariables) {
				int i = var.lastIndexOf(' ');
				if (i < 0) {
					throw new IllegalArgumentException("Illegal config import.setVariables");
				}
				this.importTypes.put(var.substring(i + 1), ClassUtils.forName(importPackages, var.substring(0, i)));
			}
		}
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
	
	protected String getDiretive(String name, String value) {
		return name + "(" + value + ")";
	}

	protected abstract String doParse(Resource resoure, boolean stream, String source, Translator translator, 
									  List<String> parameters, List<Class<?>> parameterTypes, 
									  Set<String> setVariables, Set<String> getVariables, Map<String, Class<?>> types, Map<String, Class<?>> returnTypes, Map<String, Class<?>> macros) throws IOException, ParseException;

	public Template parse(Resource resource, Map<String, Class<?>> parameterTypes) throws IOException, ParseException {
		try {
			Template writerTemplate = null;
			Template streamTemplate = null;
			if (isOutputWriter || ! isOutputStream) {
				Class<?> clazz = parseClass(resource, parameterTypes, false, 0);
				writerTemplate = (Template) clazz.getConstructor(Engine.class, Interceptor.class, Switcher.class, Filter.class, Formatter.class, Converter.class, Converter.class, Map.class, Map.class)
						.newInstance(engine, interceptor, valueSwitcher, valueFilter, formatter, mapConverter, outConverter, functions, importMacroTemplates);
			}
			if (isOutputStream) {
				Class<?> clazz = parseClass(resource, parameterTypes, true, 0);
				streamTemplate = (Template) clazz.getConstructor(Engine.class, Interceptor.class, Switcher.class, Filter.class, Formatter.class, Converter.class, Converter.class, Map.class, Map.class)
						.newInstance(engine, interceptor, valueSwitcher, valueFilter, formatter, mapConverter, outConverter, functions, importMacroTemplates);
			}
			if (writerTemplate != null && streamTemplate != null) {
				return new AdaptiveTemplate(writerTemplate, streamTemplate);
			} else if (streamTemplate != null) {
				return streamTemplate;
			} else {
				return writerTemplate;
			}
		} catch (IOException e) {
			throw e;
		} catch (ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new ParseException("Failed to parse template: " + resource.getName() + ", cause: " + ClassUtils.toString(e), 0);
		}
	}
	
	private String getTemplateClassName(Resource resource, boolean stream) {
		String name = resource.getName();
		String encoding = resource.getEncoding();
		Locale locale = resource.getLocale();
		long lastModified = resource.getLastModified();
		StringBuilder buf = new StringBuilder(name.length() + 40);
		buf.append(name);
		if (encoding != null) {
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
		return TEMPLATE_CLASS_PREFIX + SYMBOL_PATTERN.matcher(buf.toString()).replaceAll("_");
	}
	
	protected Class<?> parseClass(Resource resource, Map<String, Class<?>> types, boolean stream, int offset) throws IOException, ParseException {
		String name = getTemplateClassName(resource, stream);
		try {
			return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			Set<String> getVariables = new HashSet<String>();
			Set<String> setVariables = new HashSet<String>();
			if (types == null) {
				types = new HashMap<String, Class<?>>();
			}
			if (importTypes != null && importTypes.size() > 0) {
				types.putAll(importTypes);
			}
			Map<String, Class<?>> returnTypes = new HashMap<String, Class<?>>();
			StringBuilder statusInit = new StringBuilder();
			types.put("this", Template.class);
			types.put("super", Template.class);
			types.put(defaultFilterVariable, Filter.class);
			types.put(filterVariable, Filter.class);
			types.put(foreachVariable, ForeachStatus.class);
			StringBuilder macroFields = new StringBuilder();
			StringBuilder macroInits = new StringBuilder();
			for (String macro : importMacroTemplates.keySet()) {
				types.put(macro, Template.class);
			}
			List<String> parameters = new ArrayList<String>();
			List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
			Map<String, Class<?>> macros = new HashMap<String, Class<?>>();
			StringBuilder textFields = new StringBuilder();
			String source = IOUtils.readToString(resource.getReader());
			if (templateFilter != null) {
				source = templateFilter.filter(resource.getName(), source);
			}
			String src = source;
			src = filterCData(src);
			src = filterComment(src);
			src = filterEscape(src);
			src = doParse(resource, stream, src, translator, parameters, parameterTypes, setVariables, getVariables, types, returnTypes, macros);
			String code = filterStatement(src, textFields, getVariables, types, new AtomicInteger(), stream, resource);
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
			for (String var : parameters) {
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
			if (defaultParameterType != null) {
				for (String var : getVariables) {
					if (! defined.contains(var) && ! types.containsKey(var)) {
						defined.add(var);
						declare.append(getTypeCode(defaultParameterType, var));
					}
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
				funtionFileds.append(" _");
				funtionFileds.append(functionType.getName().replace('.','_'));
				funtionFileds.append(";\n");
				
				functionInits.append("	this._");
				functionInits.append(functionType.getName().replace('.','_'));
				functionInits.append(" = (");
				functionInits.append(typeName);
				functionInits.append(") functions.get(");
				functionInits.append(typeName);
				functionInits.append(".class);\n");
			}
			
			String methodCode = statusInit.toString() + declare + code;
			
			if (sourceInClass) {
				textFields.append("private static final String $SRC = \"" + StringUtils.escapeString(source) + "\";\n");
				textFields.append("private static final String $CODE = \"" + StringUtils.escapeString(methodCode) + "\";\n");
			} else {
				String sourceCodeId = StringCache.put(source);
				textFields.append("private static final String $SRC = " + StringCache.class.getName() +  ".getAndRemove(\"" + sourceCodeId + "\");\n");
				String methodCodeId = StringCache.put(methodCode);
				textFields.append("private static final String $CODE = " + StringCache.class.getName() +  ".getAndRemove(\"" + methodCodeId + "\");\n");
			}
			
			textFields.append("private static final Map $PTS = " + toTypeCode(parameters, parameterTypes) + ";\n");
			textFields.append("private static final Map $CTS = " + toTypeCode(returnTypes) + ";\n");
			
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
					+ Switcher.class.getName() + " switcher, " 
					+ Filter.class.getName() + " filter, "
					+ Formatter.class.getName() + " formatter, "
					+ Converter.class.getName() + " mapConverter, "
					+ Converter.class.getName() + " outConverter, "
					+ Map.class.getName() + " functions, " 
					+ Map.class.getName() + " importMacros) {\n" 
					+ "	super(engine, interceptor, switcher, filter, formatter, mapConverter, outConverter, functions, importMacros);\n"
					+ functionInits
					+ macroInits
					+ "}\n"
					+ "\n"
					+ "protected void doRender(" + Context.class.getName() + " $context, " 
					+ (stream ? OutputStream.class.getName() : Writer.class.getName())
					+ " $output) throws " + Exception.class.getName() + " {\n" 
					+ methodCode
					+ "}\n"
					+ "\n"
					+ "public " + String.class.getSimpleName() + " getName() {\n"
					+ "	return \"" + resource.getName() + "\";\n"
					+ "}\n"
					+ "\n"
					+ "public " + String.class.getSimpleName() + " getEncoding() {\n"
					+ "	return " + (resource.getEncoding() == null ? "null" : "\"" + resource.getEncoding() + "\"") + ";\n"
					+ "}\n"
					+ "\n"
					+ "public " + Locale.class.getName() + " getLocale() {\n"
					+ "	return " + (resource.getLocale() == null ? "null" : LocaleUtils.class.getName() + ".getLocale(\"" + resource.getLocale() + "\")") + ";\n"
					+ "}\n"
					+ "\n"
					+ "public long getLastModified() {\n"
					+ "	return " + resource.getLastModified() + "L;\n"
					+ "}\n"
					+ "\n"
					+ "public long getLength() {\n"
					+ "	return " + resource.getLength() + "L;\n"
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
					+ "public " + Map.class.getName() + " getParameterTypes() {\n"
					+ "	return $PTS;\n"
					+ "}\n"
					+ "\n"
					+ "public " + Map.class.getName() + " getContextTypes() {\n"
					+ "	return $CTS;\n"
					+ "}\n"
					+ "\n"
					+ "public " + Map.class.getName() + " getMacroTypes() {\n"
					+ "	return " + toTypeCode(macros) + ";\n"
					+ "}\n"
					+ "\n"
					+ "public boolean isMacro() {\n"
					+ "	return " + (offset > 0 || resource.getName().indexOf(POUND) >= 0) + ";\n"
					+ "}\n"
					+ "\n"
					+ "public int getOffset() {\n"
					+ "	return " + offset + ";\n"
					+ "}\n"
					+ "\n"
					+ "}\n";
			if (logger != null && logger.isDebugEnabled()) {
				logger.debug("\n================================\n" + resource.getName() + "\n================================\n" + sorceCode + "\n================================\n");
			}
			return compiler.compile(sorceCode);
		} catch (Exception e) {
			throw new ParseException("Filed to parse template: " + resource.getName() + ", cause: " + ClassUtils.toString(e), 0);
		}
	}
	
	private String getTypeCode(Class<?> type, String var) {
		String typeName = getTypeName(type);
		if (type.isPrimitive()) {
			return "	" + typeName + " " + ClassUtils.filterJavaKeyword(var) + " = " + ClassUtils.class.getName() + ".unboxed((" + ClassUtils.getBoxedClass(type).getSimpleName() + ") $context.get(\"" + var + "\"));\n";
		} else {
			return "	" + typeName + " " + ClassUtils.filterJavaKeyword(var) + " = (" + typeName + ") $context.get(\"" + var + "\");\n";
		}
	}
	
	private String getTypeName(Class<?> type) {
		Class<?> pkgClass = type.isArray() ? type.getComponentType() : type;
		String pkgName = pkgClass.getPackage() == null ? null : pkgClass.getPackage().getName();
		String typeName;
		if (pkgName != null && ("java.lang".equals(pkgName) 
				|| (importPackageSet != null && importPackageSet.contains(pkgName)))) {
			typeName = type.getSimpleName();
		} else {
			typeName = type.getCanonicalName();
		}
		return typeName;
	}

	protected String toTypeCode(Map<String, Class<?>> types) {
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
	
	protected String toTypeCode(List<String> names, List<Class<?>> types) {
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

	protected String filterComment(String source) {
		StringBuffer buf = new StringBuffer();
		Matcher matcher = COMMENT_PATTERN.matcher(source);
		while(matcher.find()) {
			matcher.appendReplacement(buf, LEFT + matcher.group().length() + RIGHT);
		}
		matcher.appendTail(buf);
		return buf.toString();
	}
	
	protected String filterCData(String source) {
		StringBuffer buf = new StringBuffer();
		Matcher matcher = CDATA_PATTERN.matcher(source);
		while(matcher.find()) {
			String target = matcher.group(1).replace(POUND, POUND_SPECIAL).replace(DOLLAR, DOLLAR_SPECIAL);
			matcher.appendReplacement(buf, CDATA_LEFT + Matcher.quoteReplacement(target) + CDATA_RIGHT);
		}
		matcher.appendTail(buf);
		return buf.toString();
	}
	
	protected String filterEscape(String source) {
		StringBuffer buf = new StringBuffer();
		Matcher matcher = ESCAPE_PATTERN.matcher(source);
		while(matcher.find()) {
			String slash = matcher.group(1);
			int length = slash.length();
			int half = (length - length % 2) / 2;
			slash = slash.substring(0, half);
			char symbol = matcher.group(2).charAt(0);
			if (length % 2 != 0) {
				if (symbol == DOLLAR) {
					symbol = DOLLAR_SPECIAL;
				} else {
					symbol = POUND_SPECIAL;
				}
			}
			matcher.appendReplacement(buf, LEFT + (length - half) + RIGHT + Matcher.quoteReplacement(slash + symbol));
		}
		matcher.appendTail(buf);
		return buf.toString();
	}
	
	protected String filterStatement(String message, StringBuilder textFields, Set<String> getVariables, Map<String, Class<?>> types, AtomicInteger seq, boolean stream, Resource resource) throws IOException, ParseException {
		int offset = 0;
		message = RIGHT + message + LEFT;
		StringBuffer buf = new StringBuffer();
		Matcher matcher = DIRECTIVE_PATTERN.matcher(message);
		while (matcher.find()) {
			String text = matcher.group(1);
			String len = matcher.group(2);
			String next = matcher.group(3);
			int length = 0;
			if (StringUtils.isNotEmpty(len)) {
				length = Integer.parseInt(len);
			}
			if (next == null) {
				next = "";
			}
			if ("else".equals(next)) {
				if (text != null && text.trim().length() > 0) {
					throw new ParseException("Found invaild text \"" + text.trim() + "\" before " + next + " directive!", offset);
				}
				matcher.appendReplacement(buf, next);
			} else {
				matcher.appendReplacement(buf, Matcher.quoteReplacement("	$output.write(" + filterExpression(text, translator, textFields, getVariables, types, offset, seq, stream, resource) + ");\n" + next));
			}
			if (text != null) {
				offset += text.length();
			}
			offset += length;
		}
		matcher.appendTail(buf);
		return buf.toString().replace("	$output.write();\n", "");
	}
	
	protected String getExpressionCode(String symbol, String expr, String code, Class<?> returnType, boolean stream, Set<String> getVariables) {
		StringBuilder buf = new StringBuilder();
		boolean nofilter = "$!".equals(symbol);
		if (nofilter && Template.class.isAssignableFrom(returnType)) {
			buf.append("	");
			buf.append("(");
			buf.append(code);
			buf.append(").render($output);\n");
		} else if (nofilter && Resource.class.isAssignableFrom(returnType)) {
			buf.append("	");
			buf.append(IOUtils.class.getName());
			buf.append(".copy((");
			buf.append(code);
			if (stream) {
				buf.append(").getInputStream()");
			} else {
				buf.append(").getReader()");
			}
			buf.append(", $output);\n");
		} else {
			if (Expression.class.isAssignableFrom(returnType)) {
				code = "(" + code + ").evaluate()";
				returnType = Object.class;
			} else if (Resource.class.isAssignableFrom(returnType)) {
				code = IOUtils.class.getName() + ".readToString((" + code + ").getReader())";
				returnType = String.class;
			}
			code = "$formatter." + (stream ? "toBytes" : "toChars") + "(" + code + ")";
			if (! nofilter) {
				getVariables.add(filterVariable);
				code = "doFilter(" + filterVariable + ", \"" + StringUtils.escapeString(expr) + "\", " + code + ")";
			}
			buf.append("	$output.write(");
			buf.append(code);
			buf.append(");\n");
		}
		return buf.toString();
	}
	
	@SuppressWarnings("unchecked")
	protected String filterExpression(String message, Translator translator, StringBuilder textFields, Set<String> getVariables, Map<String, Class<?>> types, int offset, AtomicInteger seq, boolean stream, Resource resource) throws IOException, ParseException {
		if (StringUtils.isEmpty(message)) {
			return "";
		}
		if (removeDirectiveBlank) {
			message = StringUtils.trimBlankLine(message);
			if (StringUtils.isEmpty(message)) {
				return "";
			}
		}
		TemplateFormatter templateFormatter = new TemplateFormatter(engine, formatter);
		StringBuffer buf = new StringBuffer();
		Matcher matcher = EXPRESSION_PATTERN.matcher(message);
		int last = 0;
		while (matcher.find()) {
			String symbol = matcher.group(1);
			String expression = matcher.group(2);
			int off = matcher.start(2) + offset;
			String txt = message.substring(last, matcher.start());
			appendSwitcher(buf, txt, textFields, seq, stream, getVariables);
			buf.append(");\n");
			if (symbol.charAt(0) == '$') {
				Expression expr = translator.translate(expression, types, off);
				getVariables.addAll(expr.getParameterTypes().keySet());
				String code = expr.getCode();
				Class<?> returnType = expr.getReturnType();
				buf.append(getExpressionCode(symbol, expression, code, returnType, stream, getVariables));
			} else {
				boolean nofilter = "#!".equals(symbol);
				Expression expr = translator.translate(expression, Collections.EMPTY_MAP, off);
				getVariables.addAll(expr.getParameterTypes().keySet());
				ResourceTemplate template = new ResourceTemplate(resource);
				UnsafeStringWriter writer = new UnsafeStringWriter();
				Context.pushContext(template, Collections.EMPTY_MAP, writer);
				try {
					Object value = expr.evaluate(Collections.EMPTY_MAP);
					if (value instanceof Expression) {
						value = ((Expression) value).evaluate(Collections.EMPTY_MAP);
					} else if (value instanceof Resource) {
						value = IOUtils.readToString(((Resource)value).getReader());
					}
					String str = templateFormatter.toString(value);
					if (! nofilter && valueFilter != null) {
						str = valueFilter.filter(str, str);
					}
					buf.append("	$output.write(");
					appendSwitcher(buf, str, textFields, seq, stream, getVariables);
					buf.append(");\n");
					String msg = writer.getBuffer().toString();
					if (StringUtils.isNotEmpty(msg)) {
						buf.append("	$output.write(");
						appendSwitcher(buf, msg, textFields, seq, stream, getVariables);
						buf.append(");\n");
					}
				} finally {
					Context.popContext();
				}
				
			}
			buf.append("	$output.write(");
			last = matcher.end();
		}
		String txt;
		if (last == 0) {
			txt = message;
		} else if (last < message.length()) {
			txt = message.substring(last);
		} else {
			txt = null;
		}
		appendSwitcher(buf, txt, textFields, seq, stream, getVariables);
		return buf.toString();
	}

	private void appendSwitcher(StringBuffer buf, String txt, StringBuilder textFields, AtomicInteger seq, boolean stream, Set<String> getVariables) {
		if (StringUtils.isEmpty(txt)) {
			return;
		}
		Filter filter = textFilter;
		if (valueSwitcher != null || textSwitcher != null) {
			Set<String> locations = new HashSet<String>();
			List<String> valueLocations = valueSwitcher == null ? null : valueSwitcher.locations();
			if (valueLocations != null) {
				locations.addAll(valueLocations);
			}
			List<String> textLocations = textSwitcher == null ? null : textSwitcher.locations();
			if (textLocations != null) {
				locations.addAll(textLocations);
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
						appendText(buf, txt.substring(begin, end), filter, textFields, seq, stream);
						begin = end;
						buf.append(");\n");
						for (String location : entry.getValue()) {
							if (textLocations != null && textLocations.contains(location)) {
								filter = textSwitcher.enter(location, textFilter);
							}
							if (valueLocations != null && valueLocations.contains(location)) {
								buf.append("	" + filterVariable + " = enter(\"" + StringUtils.escapeString(location) + "\", " + defaultFilterVariable + ");\n");
							}
						}
						buf.append("	$output.write(");
					}
					txt = txt.substring(begin);
				}
			}
		}
		appendText(buf, txt, filter, textFields, seq, stream);
	}

	private void appendText(StringBuffer buf, String txt, Filter filter, StringBuilder textFields, AtomicInteger seq, boolean stream) {
		if (StringUtils.isEmpty(txt)) {
			return;
		}
		txt = txt.replace(POUND_SPECIAL, POUND);
		txt = txt.replace(DOLLAR_SPECIAL, DOLLAR);
		if (filter != null) {
			txt = filter.filter(txt, txt);
		}
		if (StringUtils.isNotEmpty(txt)) {
			String var = "$TXT" + seq.incrementAndGet();
			if (stream) {
				if (textInClass) {
					textFields.append("private static final byte[] " + var + " = new byte[] {" + StringUtils.toByteString(StringUtils.toBytes(txt, outputEncoding)) + "};\n");
				} else {
					String txtId = ByteCache.put(StringUtils.toBytes(txt, outputEncoding));
					textFields.append("private static final byte[] " + var + " = " + ByteCache.class.getName() +  ".getAndRemove(\"" + txtId + "\");\n");
				}
			} else {
				if (textInClass) {
					textFields.append("private static final char[] " + var + " = \"" + StringUtils.escapeString(txt) + "\";\n");
				} else {
					String txtId = CharCache.put(txt.toCharArray());
					textFields.append("private static final char[] " + var + " = " + CharCache.class.getName() +  ".getAndRemove(\"" + txtId + "\");\n");
				}
			}
			buf.append(var);
		}
	}
	
	protected String getStatementEndCode(String name) throws IOException, ParseException {
		if (ifDirective.equals(name) || elseifDirective.equals(name) || elseDirective.equals(name)) {
			return "	}\n"; // 插入结束指令
		} else if (foreachDirective.equals(name)) {
			return "	" + foreachVariable + ".increment();\n	}\n	" + foreachVariable + " = " + foreachVariable + ".getParent();\n"; // 插入结束指令
		}
		return "";
	}
	
	protected String getStatementCode(String name, String value, int begin, int offset,
									Translator translator, Set<String> setVariables, Set<String> getVariables, Map<String, Class<?>> types, 
									Map<String, Class<?>> returnTypes, List<String> parameters, List<Class<?>> parameterTypes, boolean comment) throws IOException, ParseException {
		name = name == null ? null : name.trim();
		value = value == null ? null : value.trim();
		StringBuilder buf = new StringBuilder();
		if (ifDirective.equals(name)) {
			if (StringUtils.isEmpty(value)) {
				throw new ParseException("The if expression == null!", begin);
			}
			Expression expr = translator.translate(value, types, offset);
			getVariables.addAll(expr.getParameterTypes().keySet());
			buf.append("	if (");
			buf.append(getConditionCode(expr));
			buf.append(") {\n");
		} else if (elseifDirective.equals(name)) {
			if (StringUtils.isEmpty(value)) {
				throw new ParseException("The elseif expression == null!", begin);
			}
			Expression expr = translator.translate(value, types, offset);
			getVariables.addAll(expr.getParameterTypes().keySet());
			if (comment) {
				buf.append("	} ");
			}
			buf.append("else if (");
			buf.append(getConditionCode(expr));
			buf.append(") {\n");
		} else if (elseDirective.equals(name)) {
			if (StringUtils.isNotEmpty(value)) {
				throw new ParseException("Unsupported else expression " + value, offset);
			}
			if (comment) {
				buf.append("	} ");
			}
			buf.append("else {\n");
		} else if (foreachDirective.equals(name)) {
			if (StringUtils.isEmpty(value)) {
				throw new ParseException("The foreach expression == null!", begin);
			}
			Matcher matcher = IN_PATTERN.matcher(value);
			if (! matcher.find()) {
				throw new ParseException("Not found \"in\" in foreach", offset);
			}
			int start = matcher.start(1);
			int end = matcher.end(1);
			Expression expression = translator.translate(value.substring(end).trim(), types, offset + end);
			getVariables.addAll(expression.getParameterTypes().keySet());
			Class<?> returnType = expression.getReturnType();
			String code = expression.getCode();
			String[] tokens = value.substring(0, start).trim().split("\\s+");
			String type;
			String var;
			String varname = code.trim();
			if (expression instanceof ExpressionImpl) {
				String vn = ((ExpressionImpl) expression).getNode().getGenericVariableName();
				if (vn != null) {
					varname = vn;
				}
			}
			if (tokens.length == 1) {
				// TODO 获取in参数List的泛型
				if (returnType.isArray()) {
					type = returnType.getComponentType().getName();
				} else if (Map.class.isAssignableFrom(returnType)) {
					type = Map.class.getName() + ".Entry";
				} else if (Collection.class.isAssignableFrom(returnType)
						&& types.get(varname + ":0") != null) {
					type = types.get(varname + ":0").getName();
				} else {
					type = Object.class.getSimpleName();
				}
				var = tokens[0].trim();
			} else if (tokens.length == 2) {
				type = tokens[0].trim();
				var = tokens[1].trim();
			} else {
				throw new ParseException("Illegal: " + value, offset);
			}
			Class<?> clazz = ClassUtils.forName(importPackages, type);
			types.put(var, clazz);
			if (Map.class.isAssignableFrom(returnType)) {
				Class<?> keyType = types.get(varname + ":0");
				if (keyType != null) {
					types.put(var + ":0", keyType);
				}
				Class<?> valueType = types.get(varname + ":1");
				if (valueType != null) {
					types.put(var + ":1", valueType);
				}
				code = ClassUtils.class.getName() + ".entrySet(" + code + ")";
			}
			setVariables.add(foreachVariable);
			buf.append(getForeachCode(type, clazz, var, code));
		} else if (breakifDirective.equals(name)) {
			if (StringUtils.isEmpty(value)) {
				throw new ParseException("The breakif expression == null!", begin);
			}
			Expression expr = translator.translate(value, types, offset);
			getVariables.addAll(expr.getParameterTypes().keySet());
			buf.append("	if (");
			buf.append(getConditionCode(expr));
			buf.append(") break;\n");
		} else if (setDirective.equals(name)) {
			Matcher matcher = ASSIGN_PATTERN.matcher(";" + value);
			List<Object[]> list = new ArrayList<Object[]>();
			Object[] pre = null;
			while (matcher.find()) {
				if (pre != null) {
					pre[4] = value.substring(((Integer) pre[3]) - 1, matcher.start() - 1).trim();
				}
				Object[] item = new Object[6];
				if (matcher.group(2) == null || matcher.group(2).length() == 0) {
					item[0] = null;
					item[1] = matcher.group(1);
				} else {
					item[0] = matcher.group(1);
					item[1] = matcher.group(2);
				}
				item[2] = matcher.group(3);
				item[3] = matcher.end();
				item[5] = matcher.start(1) - 1; // 减掉前面追加的分号
				list.add(item);
				pre = item;
			}
			if (pre != null) {
				pre[4] = value.substring(((Integer) pre[3]) - 1).trim();
			}
			if (list.isEmpty()) {
				throw new ParseException("Not found \"=\" in set", offset);
			}
			for (Object[] item : list) {
				String type = (String) item[0];
				String var = (String) item[1];
				String oper = (String) item[2];
				int end = (Integer) item[3];
				String expr = (String) item[4];
				int start = (Integer) item[5];
				Expression expression = translator.translate(expr, types, offset + end);
				getVariables.addAll(expression.getParameterTypes().keySet());
				if (StringUtils.isEmpty(type)) {
					type = expression.getReturnType().getCanonicalName();
				}
				Class<?> clazz = ClassUtils.forName(importPackages, type);
				Class<?> cls = types.get(var);
				if (cls != null && ! cls.equals(clazz)) {
					throw new ParseException("Set different type value to variable " + var + ", conflict types: " + cls.getName() + ", " + clazz.getName(), offset + start);
				}
				types.put(var, clazz);
				setVariables.add(var);
				buf.append("	" + var + " = (" + type + ")(" + expression.getCode() + ");\n");
				String ctx = null;
				if (":=".equals(oper)) {
					ctx = "($context.getParent() != null ? $context.getParent() : $context)";
					returnTypes.put(var, clazz);
				} else if (! ".=".equals(oper)) {
					ctx = "$context";
				}
				if (StringUtils.isNotEmpty(ctx)) {
					buf.append("	" + ctx + ".put(\"");
					buf.append(var);
					buf.append("\", ");
					buf.append(ClassUtils.class.getName() + ".boxed(" + var + ")");
					buf.append(");\n");
				}
			}
		} else if (varDirective.equals(name)) {
			if (StringUtils.isEmpty(value)) {
				throw new ParseException("The in parameters == null!", begin);
			}
			value = BLANK_PATTERN.matcher(value).replaceAll(" ");
			List<String> vs = new ArrayList<String>();
			List<Integer> os = new ArrayList<Integer>();
			Matcher matcher = VAR_PATTERN.matcher(value);
			while (matcher.find()) {
				StringBuffer rep = new StringBuffer();
				matcher.appendReplacement(rep, "$1");
				String v = rep.toString();
				vs.add(v);
				os.add(offset + matcher.end(1) - v.length());
			}
			for (int n = 0; n < vs.size(); n ++) {
				String v = vs.get(n).trim();
				int o = os.get(n);
				String var;
				String type;
				int i = v.lastIndexOf(' ');
				if (i <= 0) {
					type = defaultParameterType == null ? Object.class.getSimpleName() : defaultParameterType.getCanonicalName();
					var = v;
				} else {
					type = v.substring(0, i).trim();
					var = v.substring(i + 1).trim();
				}
				type = parseGenericType(type, var, types, o);
				Class<?> clazz = ClassUtils.forName(importPackages, type);
				Class<?> cls = types.get(var);
				if (cls != null && ! cls.equals(clazz)) {
					throw new ParseException("Defined different type to variable " + var + ", conflict types: " + cls.getName() + ", " + clazz.getName(), o);
				}
				types.put(var, clazz);
				parameters.add(var);
				parameterTypes.add(clazz);
			}
		}else {
			throw new ParseException("Unsupported directive " + name, begin);
		}
		return buf.toString();
	}
	
	private void parseGenericTypeString(String type, int offset, List<String> types, List<Integer> offsets) throws IOException, ParseException {
		StringBuilder buf = new StringBuilder();
		int begin = 0;
		for (int j = 0; j < type.length(); j ++) {
			char ch = type.charAt(j);
			if (ch == '<') {
				begin ++;
			} else if (ch == '>') {
				begin --;
				if (begin < 0) {
					 throw new ParseException("Illegal type: " + type, offset + j);
				}
			}
			if (ch == ',' && begin == 0) {
				String token = buf.toString();
				types.add(token.trim());
				offsets.add(offset + j - token.length());
				buf.setLength(0);
			} else {
				buf.append(ch);
			}
		}
		if (buf.length() > 0) {
			String token = buf.toString();
			types.add(token.trim());
			offsets.add(offset + type.length() - token.length());
			buf.setLength(0);
		}
	}
	
	protected String parseGenericType(String type, String var, Map<String, Class<?>> types, int offset) throws IOException, ParseException {
		int i = type.indexOf('<');
		if (i < 0) {
			return type;
		}
		if (! type.endsWith(">")) {
			throw new ParseException("Illegal type: " + type, offset);
		}
		String parameterType = type.substring(i + 1, type.length() - 1).trim();
		offset = offset + 1;
		List<String> genericTypes = new ArrayList<String>();
		List<Integer> genericOffsets = new ArrayList<Integer>();
		parseGenericTypeString(parameterType, offset, genericTypes, genericOffsets);
		if (genericTypes != null && genericTypes.size() > 0) {
			for (int k = 0; k < genericTypes.size(); k ++) {
				String genericVar = var + ":" + k;
				String genericType = parseGenericType(genericTypes.get(k), genericVar, types, genericOffsets.get(k));
				types.put(genericVar, ClassUtils.forName(importPackages, genericType));
			}
		}
		return type.substring(0, i);
	}
	
	protected String getConditionCode(Expression expression) throws IOException, ParseException {
		return StringUtils.getConditionCode(expression.getReturnType(), expression.getCode());
	}

	protected String getForeachCode(String type, Class<?> clazz, String var, String code) {
		StringBuilder buf = new StringBuilder();
		String name = "_i_" + var;
		buf.append("	for (" + Iterator.class.getName() + " " + name + " = " + ClassUtils.class.getName() + ".toIterator((" + foreachVariable + " = new " + ForeachStatus.class.getName() + "(" + foreachVariable + ", " + code + ")).getData()); " + name + ".hasNext();) {\n");
		if (clazz.isPrimitive()) {
			buf.append("	" + type + " " + var + " = " + ClassUtils.class.getName() + ".unboxed((" + ClassUtils.getBoxedClass(clazz).getSimpleName() + ")" + name + ".next());\n");
		} else {
			buf.append("	" + type + " " + var + " = (" + type + ") " + name + ".next();\n");
		}
		return buf.toString();
	}

	protected String getMacroPath(String template, String value) {
		if (value == null) {
			value = "";
		}
		value = value.trim();
		return template + POUND + value;
	}

}