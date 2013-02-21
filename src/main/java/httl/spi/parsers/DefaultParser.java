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
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.internal.util.ByteCache;
import httl.internal.util.CharCache;
import httl.internal.util.ClassUtils;
import httl.internal.util.CollectionUtils;
import httl.internal.util.DfaScanner;
import httl.internal.util.ForeachStatus;
import httl.internal.util.IOUtils;
import httl.internal.util.LinkedStack;
import httl.internal.util.LocaleUtils;
import httl.internal.util.OrderedMap;
import httl.internal.util.StringCache;
import httl.internal.util.StringUtils;
import httl.internal.util.Token;
import httl.internal.util.UnsafeStringWriter;
import httl.internal.util.VolatileReference;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Logger;
import httl.spi.Parser;
import httl.spi.Switcher;
import httl.spi.Translator;
import httl.spi.formatters.MultiFormatter;
import httl.spi.loaders.resources.StringResource;
import httl.spi.parsers.templates.AbstractTemplate;
import httl.spi.parsers.templates.AdaptiveTemplate;
import httl.spi.parsers.templates.OutputStreamTemplate;
import httl.spi.parsers.templates.ResourceTemplate;
import httl.spi.parsers.templates.WriterTemplate;
import httl.spi.translators.expressions.ExpressionImpl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
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
 * DefaultParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setParser(Parser)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DefaultParser implements Parser {

	//单字母命名, 保证状态机图简洁

	// END，结束片段，包含当前字符
	private static final int E = DfaScanner.BREAK;

	// BREAK，打断片段，不包含当前字符
	private static final int B = DfaScanner.BREAK - 1;

	// PUSH，压栈
	private static final int P = DfaScanner.PUSH - 4;

	// POP，弹栈
	private static final int O = DfaScanner.POP - 4;

	// 插值语法状态机图
	// 行表示状态
	// 行列交点表示, 在该状态时, 遇到某类型的字符时, 切换到的下一状态(数组行号)
	// E/B/T表示接收前面经过的字符为一个片断, R表示错误状态(这些状态均为负数)
	static final int states[][] = {
				  // 0.\s, 1.a-z, 2.#, 3.$, 4.!, 5.*, 6.(, 7.), 8.[, 9.], 10.{, 11.}, 12.", 13.', 14.`, 15.\, 16.\n, 17.其它
		/* 0.起始 */ { 1, 1, 2, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, }, // 初始状态或上一片断刚接收完成状态
		/* 1.文本 */ { 1, 1, B, B, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, }, // 非指令文本内容
		
		/* 2.指令 */ { 1, 3, 9, B, 6, 10, 1, 1, 12, 1, 7, 1, 1, 1, 1, 1, 1, 1, }, // 指令提示符
		/* 3.指名 */ { B, 3, B, B, B, B, P, B, B, B, B, B, B, B, B, B, B, B, }, // 指令名
		/* 4.指参 */ { 4, 4, 4, 4, 4, 4, P, O, 4, 4, 4, 4, 14, 16, 18, 4, 4, 4, }, // 指令参数
		
		/* 5.插值 */ { 1, 1, 1, 1, 6, 1, 1, 1, 1, 1, 7, 1, 1, 1, 1, 1, 1, 1, }, // 插值提示符
		/* 6.非滤 */ { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 7, 1, 1, 1, 1, 1, 1, 1, }, // 非过滤插值
		/* 7.插参 */ { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, E, 20, 22, 24, 7, 7, 7, }, // 插值参数
		
		/* 8.转义 */ { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, }, // 井号美元号转义
		/* 9.行注 */ { 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, B, 9, }, // 双井号行注释
		/* 10.块注 */ { 10, 10, 10, 10, 10, 11, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, }, // 井星号块注释
		/* 11.结块 */ { 10, 10, E, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, }, // 井星号块注释结束
		/* 12.字面 */ { 12, 12, 12, 12, 12, 12, 12, 12, 12, 13, 12, 12, 12, 12, 12, 12, 12, 12, }, // 井方号块字面不解析块
		/* 13.结字 */ { 12, 12, E, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, }, // 井方号块字面不解析块结束
		
		/* 14.字串 */ { 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 4, 14, 14, 15, 14, 14, }, // 指令参数双引号字符串
		/* 15.转字 */ { 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, }, // 指令参数双引号字符串转义
		/* 16.字串 */ { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 4, 16, 17, 16, 16, }, // 指令参数单引号字符串
		/* 17.转字 */ { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, }, // 指令参数单引号字符串转义
		/* 18.字串 */ { 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 4, 19, 18, 18, }, // 指令参数反单引号字符串
		/* 19.转字 */ { 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, }, // 指令参数反单引号字符串转义
		
		/* 20.字串 */ { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 7, 20, 20, 21, 20, 20, }, // 插值参数双引号字符串
		/* 21.转字 */ { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, }, // 插值参数双引号字符串转义
		/* 22.字串 */ { 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 7, 22, 23, 22, 22, }, // 插值参数单引号字符串
		/* 23.转字 */ { 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, }, // 插值参数单引号字符串转义
		/* 24.字串 */ { 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 7, 25, 24, 24, }, // 插值参数反单引号字符串
		/* 25.转字 */ { 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, }, // 插值参数反单引号字符串转义
	};

	static int getCharType(char ch) {
		switch (ch) {
			case ' ': case '\t': case '\r': case '\f': case '\b':
				return 0;
			case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : 
			case 'h' : case 'i' : case 'j' : case 'k' : case 'l' : case 'm' : case 'n' : 
			case 'o' : case 'p' : case 'q' : case 'r' : case 's' : case 't' : 
			case 'u' : case 'v' : case 'w' : case 'x' : case 'y' : case 'z' :
				return 1;
			case '#' : 
				return 2;
			case '$' : 
				return 3;
			case '!' : 
				return 4;
			case '*' : 
				return 5;
			case '(' : 
				return 6;
			case ')' : 
				return 7;
			case '[' : 
				return 8;
			case ']' : 
				return 9;
			case '{' : 
				return 10;
			case '}' : 
				return 11;
			case '\"' : 
				return 12;
			case '\'' : 
				return 13;
			case '`' : 
				return 14;
			case '\\' : 
				return 15;
			case '\n':
				return 16;
			default:
				return 17;
		}
	}

	private static DfaScanner scanner = new DfaScanner() {
		@Override
		public int next(int state, char ch) {
			return states[state][getCharType(ch)];
		}
	};
	
	public static void main(String[] args) throws Exception {
		List<Token> tokens = scanner.scan(IOUtils.readToString(new InputStreamReader(DefaultParser.class.getClassLoader().getResourceAsStream("text.httl"))));
		for (Token token : tokens) {
			System.out.println(token.getMessage().replace("\n", "\\n").replace("\r", "\\r") + "\n");
		}
	}
	
	private String doParse(Resource resource, boolean stream, String source, Translator translator, 
							 List<String> parameters, List<Class<?>> parameterTypes, 
							 Set<String> setVariables, Set<String> getVariables, Map<String, Class<?>> types, 
							 Map<String, Class<?>> returnTypes, Map<String, Class<?>> macros, StringBuilder textFields, AtomicInteger seq) throws IOException, ParseException {
		LinkedStack<String> nameStack = new LinkedStack<String>();
		LinkedStack<String> valueStack = new LinkedStack<String>();
		StringBuffer macro = null;
		int macroParameterStart = 0;
		VolatileReference<Filter> filterReference = new VolatileReference<Filter>();
		filterReference.set(textFilter);
		StringBuffer buf = new StringBuffer();
		List<Token> tokens = scanner.scan(source);
		for (Token token : tokens) {
			String message = token.getMessage();
			int offset = token.getOffset();
			if (message.length() > 1 && message.charAt(0) == '#'
					&& message.charAt(1) >= 'a' && message.charAt(1) <= 'z') {
				int s = message.indexOf('(');
				String name;
				String value;
				int exprOffset;
				if (s > 0) {
					exprOffset = offset + s;
					name = message.substring(1, s);
					if (! message.endsWith(")")) {
						throw new ParseException("The #" + name + " directive mismatch right parentheses.", exprOffset);
					}
					value = message.substring(s + 1, message.length() - 1);
				} else {
					exprOffset = token.getOffset() + message.length();
					name = message.substring(1);
					value = "";
				}
				if (! (name.startsWith(varDirective) || name.startsWith(setDirective)
						|| name.startsWith(ifDirective) || name.startsWith(elseifDirective)
						|| name.startsWith(elseDirective) || name.startsWith(foreachDirective)
						|| name.startsWith(breakifDirective) || name.startsWith(macroDirective)
						|| name.startsWith(endDirective))) {
					throw new ParseException("Unsupported directive #" + name + ".", offset);
				}
				if (endDirective.equals(name)) {
					if (nameStack.isEmpty()) {
						throw new ParseException("The #end directive without start directive.", offset);
					}
					String startName = nameStack.pop();
					String startValue = valueStack.pop();
					while(elseifDirective.equals(startName) || elseDirective.equals(startName)) {
						if (nameStack.isEmpty()) {
							throw new ParseException("The #" + startName + " directive without #if directive.", offset);
						}
						String oldStartName = startName;
						startName = nameStack.pop();
						startValue = valueStack.pop();  
						if (! ifDirective.equals(startName) && ! elseifDirective.equals(startName)) {
							throw new ParseException("The #" + oldStartName + " directive without #if directive.", offset);
						}
					}
					if (macro != null) {
						if (macroDirective.equals(startName) && 
								! nameStack.toList().contains(macroDirective)) {
							int i = startValue.indexOf('(');
							String var;
							String param;
							if (i > 0) {
								if (! startValue.endsWith(")")) {
									throw new ParseException("Invalid macro parameters " + startValue, macroParameterStart + i);
								}
								var = startValue.substring(0, i).trim();
								param = startValue.substring(i + 1, startValue.length() - 1).trim();
							} else {
								var = startValue;
								param = null;
							}
							String out = null;
							String set = null;
							if (var.startsWith("$")) {
								if (var.startsWith("$!")) {
									out = var.substring(0, 2);
									var = var.substring(2).trim();
								} else {
									out = var.substring(0, 1);
									var = var.substring(1).trim();
								}
							} else if (var.contains("=")) {
								int l = var.indexOf("=");
								set = var.substring(0, l + 1).trim();
								var = var.substring(l + 1).trim();
							}
							String key = getMacroPath(resource.getName(), var);
							String es = macro.toString();
							if (StringUtils.isNotEmpty(param)) {
								es = getDiretive(varDirective, param) + es;
							}
							macros.put(var, parseClass(new StringResource(getEngine(), key, resource.getLocale(), resource.getEncoding(), resource.getLastModified(), es), types, stream, macroParameterStart));
							Class<?> cls = types.get(var);
							if (cls != null && ! cls.equals(Template.class)) {
								throw new ParseException("Duplicate macro variable " + var + ", conflict types: " + cls.getName() + ", " + Template.class.getName(), macroParameterStart);
							}
							types.put(var, Template.class);
							if (StringUtils.isNotEmpty(out)) {
								getVariables.add(var);
								String code = getExpressionPart(out, var, var, Template.class, stream, getVariables, textFields, seq);
								buf.append(code);
							} else if (StringUtils.isNotEmpty(set)) {
								getVariables.add(var);
								String setValue = set + " " + var + ".evaluate()";
								String code = getStatementCode(setDirective, setValue, offset, exprOffset, translator, setVariables, getVariables, types, returnTypes, parameters, parameterTypes, true);
								buf.append(code);
							}
							macro = null;
							macroParameterStart = 0;
						} else {
							macro.append(message);
						}
					} else {
						String end = getStatementEndCode(startName);
						if (StringUtils.isNotEmpty(end)) {
							buf.append(end);
						} else {
							throw new ParseException("The #end directive without start directive.", offset);
						}
					}
				} else {
					if (ifDirective.equals(name) || elseifDirective.equals(name) 
							|| elseDirective.equals(name) || foreachDirective.equals(name)
							|| macroDirective.equals(name)) {
						nameStack.push(name);
						valueStack.push(value);
					}
					if (macro != null) {
						macro.append(message);
					} else {
						if (macroDirective.equals(name)) {
							if (value == null || value.trim().length() == 0) {
								throw new ParseException("Macro name == null!", offset);
							}
							macro = new StringBuffer();
							macroParameterStart = exprOffset;
						} else {
							String code = getStatementCode(name, value, offset, exprOffset, translator, setVariables, getVariables, types, returnTypes, parameters, parameterTypes, true);
							buf.append(code);
						}
					}
				}
			} else if (macro != null) {
				macro.append(message);
			} else if (message.endsWith("}") && (message.startsWith("${") || message.startsWith("$!{") 
					|| message.startsWith("#{") || message.startsWith("#!{"))) {
				int i = message.indexOf('{');
				buf.append(getExpressionCode(message.substring(0, i), message.substring(i + 1, message.length() - 1), translator, textFields, getVariables, types, offset + i, seq, stream, resource));
			} else if (message.startsWith("##") || (message.startsWith("#*") && message.endsWith("*#"))) {
				continue;
			} else {
				if (message.startsWith("#[") && message.endsWith("]#")) {
					message = message.substring(2, message.length() - 2);
				} else {
					message = filterEscape(message);
				}
				buf.append(getTextCode(message, filterReference, textFields, getVariables, seq, stream, false));
			}
		}
		return buf.toString();
	}

	private static final Pattern ASSIGN_PATTERN = Pattern.compile(";\\s*(\\w+)\\s*(\\w*)\\s*([:\\.]?=)");

	private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\\\+");

	private static final Pattern VAR_PATTERN = Pattern.compile("([_0-9a-zA-Z>\\]]\\s[_0-9a-zA-Z]+)\\s?[,]?\\s?");

	private static final Pattern BLANK_PATTERN = Pattern.compile("\\s+");

	private String varDirective = "var";

	private String setDirective = "set";

	private String ifDirective = "if";

	private String elseifDirective = "elseif";

	private String elseDirective = "else";

	private String foreachDirective = "foreach";

	private String breakifDirective = "breakif";

	private String macroDirective = "macro";

	private String endDirective = "end";

	private String foreachVariable = "foreach";

	private String foreachSeparator = "in";

	private String filterVariable = "filter";

	private String formatterVariable = "formatter";

	private String defaultFilterVariable;
	
	private String defaultFormatterVariable;

	private Pattern foreachSeparatorPattern;

	private Engine engine;
	
	private Compiler compiler;
	
	private Translator translator;
	
	private Interceptor interceptor;
	
	private Switcher<Filter> textFilterSwitcher;

	private Switcher<Filter> valueFilterSwitcher;

	private Switcher<Formatter<Object>> formatterSwitcher;

	private Filter templateFilter;

	private Filter textFilter;

	private Filter valueFilter;
	
	private Converter<Object, Object> mapConverter;

	private Converter<Object, Object> outConverter;

	private Formatter<Object> formatter;

	private String[] importMacros;
   
	private final Map<String, Template> importMacroTemplates = new ConcurrentHashMap<String, Template>();

	private String[] importPackages;

	private Set<String> importPackageSet;

	private String[] importVariables;

	private Map<String, Class<?>> importTypes;

	private final Map<Class<?>, Object> functions = new ConcurrentHashMap<Class<?>, Object>();

	private static final String TEMPLATE_CLASS_PREFIX = AbstractTemplate.class.getPackage().getName() + ".Template_";
	
	private final AtomicInteger TMP_VAR_SEQ = new AtomicInteger();
	
	private boolean isOutputStream;

	private boolean isOutputWriter;

	private boolean sourceInClass;

	private boolean textInClass;
	
	private String outputEncoding;
	
	private Logger logger;
	
	private Class<?> defaultVariableType;

	private String engineName;

	private String[] importSizers;
	
	/**
	 * httl.properties: import.sizers=size,length,getSize,getLength
	 */
	public void setImportSizers(String[] importSizers) {
		this.importSizers = importSizers;
	}

	/**
	 * httl.properties: var.directive=var
	 */
	public void setVarDirective(String varDirective) {
		this.varDirective = varDirective;
	}

	/**
	 * httl.properties: set.directive=set
	 */
	public void setSetDirective(String setDirective) {
		this.setDirective = setDirective;
	}

	/**
	 * httl.properties: if.directive=if
	 */
	public void setIfDirective(String ifDirective) {
		this.ifDirective = ifDirective;
	}

	/**
	 * httl.properties: elseif.directive=elseif
	 */
	public void setElseifDirective(String elseifDirective) {
		this.elseifDirective = elseifDirective;
	}

	/**
	 * httl.properties: else.directive=else
	 */
	public void setElseDirective(String elseDirective) {
		this.elseDirective = elseDirective;
	}

	/**
	 * httl.properties: foreach.directive=foreach
	 */
	public void setForeachDirective(String foreachDirective) {
		this.foreachDirective = foreachDirective;
	}

	/**
	 * httl.properties: breakif.directive=breakif
	 */
	public void setBreakifDirective(String breakifDirective) {
		this.breakifDirective = breakifDirective;
	}

	/**
	 * httl.properties: macro.directive=macro
	 */
	public void setMacroDirective(String macroDirective) {
		this.macroDirective = macroDirective;
	}

	/**
	 * httl.properties: end.directive=end
	 */
	public void setEndDirective(String endDirective) {
		this.endDirective = endDirective;
	}

	/**
	 * httl.properties: default.variable.type=java.lang.String
	 */
	public void setDefaultVariableType(String defaultVariableType) {
		this.defaultVariableType = ClassUtils.forName(defaultVariableType);
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
	
	private static final String PROPERTIES_SUFFIX = ".properties";

	/**
	 * httl.properties: engine=httl.spi.engines.DefaultEngine
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/**
	 * httl.properties: engine.name=httl.properties
	 */
	public void setEngineName(String name) {
		if (name.endsWith(PROPERTIES_SUFFIX)) {
			name = name.substring(0, name.length() - PROPERTIES_SUFFIX.length());
		}
		this.engineName = name;
	}

	private Engine getEngine() {
		return engine;
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
	 * httl.properties: text.filter.switchers=httl.spi.switchers.JavaScriptFilterSwitcher
	 */
	public void setTextFilterSwitcher(Switcher<Filter> textFilterSwitcher) {
		this.textFilterSwitcher = textFilterSwitcher;
	}

	/**
	 * httl.properties: value.filter.switchers=httl.spi.switchers.JavaScriptFilterSwitcher
	 */
	public void setValueFilterSwitcher(Switcher<Filter> valueFilterSwitcher) {
		this.valueFilterSwitcher = valueFilterSwitcher;
	}

	/**
	 * httl.properties: formatter.switchers=httl.spi.switchers.NumberFormatterSwitcher
	 */
	public void setFormatterSwitcher(Switcher<Formatter<Object>> formatterSwitcher) {
		this.formatterSwitcher = formatterSwitcher;
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
	public void setFormatter(Formatter<Object> formatter) {
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
	 * httl.properties: foreach.separator=in
	 */
	public void setForeachSeparator(String foreachSeparator) {
		this.foreachSeparator = foreachSeparator;
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
	}

	/**
	 * httl.properties: formatter.variable=formatter
	 */
	public void setFormatterVariable(String formatterVariable) {
		this.formatterVariable = formatterVariable;
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
		foreachSeparatorPattern = Pattern.compile("(\\s+" + Pattern.quote(foreachSeparator) + "\\s+)");
		defaultFilterVariable = "$" + filterVariable;
		defaultFormatterVariable = "$" + formatterVariable;
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
	
	private String getDiretive(String name, String value) {
		return "#" + name + "(" + value + ")";
	}

	public Template parse(Resource resource, Map<String, Class<?>> parameterTypes) throws IOException, ParseException {
		try {
			Template writerTemplate = null;
			Template streamTemplate = null;
			if (isOutputWriter || ! isOutputStream) {
				Class<?> clazz = parseClass(resource, parameterTypes, false, 0);
				writerTemplate = (Template) clazz.getConstructor(Engine.class, Interceptor.class, Compiler.class, Switcher.class, Switcher.class, Filter.class, Formatter.class, Converter.class, Converter.class, Map.class, Map.class)
						.newInstance(engine, interceptor, compiler, valueFilterSwitcher, formatterSwitcher, valueFilter, formatter, mapConverter, outConverter, functions, importMacroTemplates);
			}
			if (isOutputStream) {
				Class<?> clazz = parseClass(resource, parameterTypes, true, 0);
				streamTemplate = (Template) clazz.getConstructor(Engine.class, Interceptor.class, Compiler.class, Switcher.class, Switcher.class, Filter.class, Formatter.class, Converter.class, Converter.class, Map.class, Map.class)
						.newInstance(engine, interceptor, compiler, valueFilterSwitcher, formatterSwitcher, valueFilter, formatter, mapConverter, outConverter, functions, importMacroTemplates);
			}
			if (writerTemplate != null && streamTemplate != null) {
				return new AdaptiveTemplate(writerTemplate, streamTemplate, outConverter);
			} else if (streamTemplate != null) {
				return streamTemplate;
			} else {
				return writerTemplate;
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			ParseException pe;
			if (e instanceof ParseException)
				pe = (ParseException) e;
			else
				pe = new ParseException("Failed to parse template: " + resource.getName() + ", cause: " + ClassUtils.toString(e), 0);
			if (e.getMessage() != null 
					&& e.getMessage().contains("Occur to offset:")) {
				throw pe;
			}
			int offset = pe.getErrorOffset();
			if (offset <= 0) {
				throw pe;
			}
			String location = null;
			try {
				Reader reader = resource.getReader();
				try {
					location = StringUtils.getLocationMessage(resource.getName(), reader, offset);
				} finally {
					reader.close();
				}
			} catch (Throwable t) {
			}
			throw new ParseException(e.getMessage()  + ". \nOccur to offset: " + offset + 
									 (StringUtils.isEmpty(location) ? "" : ", " + location) 
									 + ", stack: " + ClassUtils.toString(e), offset);
		}
	}
	
	private String getTemplateClassName(Resource resource, boolean stream) {
		String name = resource.getName();
		String encoding = resource.getEncoding();
		Locale locale = resource.getLocale();
		long lastModified = resource.getLastModified();
		StringBuilder buf = new StringBuilder(name.length() + 40);
		buf.append(name);
		if (engineName != null) {
			buf.append("_");
			buf.append(engineName);
		}
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
		return TEMPLATE_CLASS_PREFIX + StringUtils.getVaildName(buf.toString());
	}
	
	private Class<?> parseClass(Resource resource, Map<String, Class<?>> types, boolean stream, int offset) throws IOException, ParseException {
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
			types.put(defaultFormatterVariable, Formatter.class);
			types.put(formatterVariable, Formatter.class);
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
			AtomicInteger seq = new AtomicInteger();
			String code = doParse(resource, stream, src, translator, parameters, parameterTypes, setVariables, getVariables, types, returnTypes, macros, textFields, seq);
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
			if (getVariables.contains(formatterVariable)) {
				defined.add(formatterVariable);
				defined.add(defaultFormatterVariable);
				declare.append("	" + MultiFormatter.class.getName() + " " + defaultFormatterVariable + " = getFormatter($context, \"" + formatterVariable + "\");\n");
				declare.append("	" + MultiFormatter.class.getName() + " " + formatterVariable + " = " + defaultFormatterVariable + ";\n");
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
			if (defaultVariableType != null) {
				for (String var : getVariables) {
					if (! defined.contains(var) && ! types.containsKey(var)) {
						defined.add(var);
						declare.append(getTypeCode(defaultVariableType, var));
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
			
			textFields.append("private static final " + Map.class.getName() + " $PTS = " + toTypeCode(parameters, parameterTypes) + ";\n");
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
					+ "	return " + (offset > 0 || resource.getName().indexOf('#') >= 0) + ";\n"
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

	private String filterEscape(String source) {
		StringBuffer buf = new StringBuffer();
		Matcher matcher = ESCAPE_PATTERN.matcher(source);
		while(matcher.find()) {
			String slash = matcher.group();
			int length = slash.length();
			int half = (length - length % 2) / 2;
			slash = slash.substring(0, half);
			matcher.appendReplacement(buf, Matcher.quoteReplacement(slash));
		}
		matcher.appendTail(buf);
		return buf.toString();
	}
	
	private String getExpressionPart(String symbol, String expr, String code, Class<?> returnType, boolean stream, Set<String> getVariables, StringBuilder textFields, AtomicInteger seq) {
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
			} else if (Resource.class.isAssignableFrom(returnType)) {
				code = IOUtils.class.getName() + ".readToString((" + code + ").getReader())";
			}
			getVariables.add(formatterVariable);
			String key = getTextPart(expr, null, textFields, seq, stream, true);
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
				buf.append(pre);
				buf.append("	if (" + var + " instanceof char[]) $output.write(");
				buf.append(charsCode);
				buf.append("); else $output.write(");
				buf.append(code);
				buf.append(");\n");
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
				buf.append("	$output.write(");
				buf.append(code);
				buf.append(");\n");
			}
		}
		return buf.toString();
	}

	@SuppressWarnings("unchecked")
	private String getExpressionCode(String symbol, String expression, Translator translator, StringBuilder textFields, Set<String> getVariables, Map<String, Class<?>> types, int offset, AtomicInteger seq, boolean stream, Resource resource) throws IOException, ParseException {
		if (StringUtils.isEmpty(expression)) {
			return "";
		}
		if (symbol.charAt(0) == '$') {
			Expression expr = translator.translate(expression, types, offset);
			getVariables.addAll(expr.getParameterTypes().keySet());
			String code = expr.getCode();
			Class<?> returnType = expr.getReturnType();
			return getExpressionPart(symbol, expression, code, returnType, stream, getVariables, textFields, seq);
		} else {
			boolean nofilter = "#!".equals(symbol);
			Expression expr = translator.translate(expression, Collections.EMPTY_MAP, offset);
			getVariables.addAll(expr.getParameterTypes().keySet());
			ResourceTemplate template = new ResourceTemplate(resource);
			UnsafeStringWriter writer = new UnsafeStringWriter();
			Context.pushContext(Collections.EMPTY_MAP, writer, template);
			try {
				Object value = expr.evaluate(Collections.EMPTY_MAP);
				if (value instanceof Expression) {
					value = ((Expression) value).evaluate(Collections.EMPTY_MAP);
				} else if (value instanceof Resource) {
					value = IOUtils.readToString(((Resource)value).getReader());
				}
				String str = formatter.toString(expression, value);
				if (! nofilter && valueFilter != null) {
					str = valueFilter.filter(expression, str);
				}
				return "	$output.write(" + getTextPart(str, null, textFields, seq, stream, false) + ");\n";
			} finally {
				Context.popContext();
			}
		}
	}

	private void appendText(StringBuilder buf, String txt, Filter filter, StringBuilder textFields, AtomicInteger seq, boolean stream, boolean string) {
		String part = getTextPart(txt, filter, textFields, seq, stream, string);
		if (StringUtils.isNotEmpty(part)) {
			buf.append("	$output.write(" + part + ");\n");
		}
	}
	
	private String getTextPart(String txt, Filter filter, StringBuilder textFields, AtomicInteger seq, boolean stream, boolean string) {
		if (StringUtils.isNotEmpty(txt)) {
			if (filter != null) {
				txt = filter.filter(txt, txt);
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

	private String getTextCode(String txt, VolatileReference<Filter> filterReference, StringBuilder textFields, Set<String> getVariables, AtomicInteger seq, boolean stream, boolean string) {
		if (StringUtils.isNotEmpty(txt)) {
			Filter filter = filterReference.get();
			StringBuilder buf = new StringBuilder();
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
							appendText(buf, txt.substring(begin, end), filter, textFields, seq, stream, false);
							begin = end;
							for (String location : entry.getValue()) {
								if (textLocations != null && textLocations.contains(location)) {
									filter = textFilterSwitcher.switchover(location, textFilter);
									filterReference.set(filter);
								}
								if (valueLocations != null && valueLocations.contains(location)) {
									buf.append("	" + filterVariable + " = switchFilter(\"" + StringUtils.escapeString(location) + "\", " + defaultFilterVariable + ");\n");
								}
								if (formatterLocations != null && formatterLocations.contains(location)) {
									buf.append("	" + formatterVariable + " = switchFormatter(\"" + StringUtils.escapeString(location) + "\", " + defaultFormatterVariable + ");\n");
								}
							}
						}
						txt = txt.substring(begin);
					}
				}
			}
			appendText(buf, txt, filter, textFields, seq, stream, false);
			return buf.toString();
		}
		return "";
	}

	private String getStatementEndCode(String name) throws IOException, ParseException {
		if (ifDirective.equals(name) || elseifDirective.equals(name) || elseDirective.equals(name)) {
			return "	}\n"; // 插入结束指令
		} else if (foreachDirective.equals(name)) {
			return "	" + foreachVariable + ".increment();\n	}\n	" + foreachVariable + " = " + foreachVariable + ".getParent();\n"; // 插入结束指令
		}
		return "";
	}
	
	private String getStatementCode(String name, String value, int begin, int offset,
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
			Matcher matcher = foreachSeparatorPattern.matcher(value);
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
				throw new ParseException("The #" + varDirective + " parameters == null!", begin);
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
					type = defaultVariableType == null ? Object.class.getSimpleName() : defaultVariableType.getCanonicalName();
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
	
	private String parseGenericType(String type, String var, Map<String, Class<?>> types, int offset) throws IOException, ParseException {
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
	
	private String getConditionCode(Expression expression) throws IOException, ParseException {
		return StringUtils.getConditionCode(expression.getReturnType(), expression.getCode(), importSizers);
	}

	private String getForeachCode(String type, Class<?> clazz, String var, String code) {
		StringBuilder buf = new StringBuilder();
		String name = "_i_" + var;
		buf.append("	for (" + Iterator.class.getName() + " " + name + " = " + CollectionUtils.class.getName() + ".toIterator((" + foreachVariable + " = new " + ForeachStatus.class.getName() + "(" + foreachVariable + ", " + code + ")).getData()); " + name + ".hasNext();) {\n");
		if (clazz.isPrimitive()) {
			buf.append("	" + type + " " + var + " = " + ClassUtils.class.getName() + ".unboxed((" + ClassUtils.getBoxedClass(clazz).getSimpleName() + ")" + name + ".next());\n");
		} else {
			buf.append("	" + type + " " + var + " = (" + type + ") " + name + ".next();\n");
		}
		return buf.toString();
	}

	private String getMacroPath(String template, String value) {
		if (value == null) {
			value = "";
		}
		value = value.trim();
		return template + '#' + value;
	}

}