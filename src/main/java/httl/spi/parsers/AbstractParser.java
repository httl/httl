/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.parsers;

import httl.Engine;
import httl.Expression;
import httl.Resource;
import httl.Template;
import httl.spi.Compiler;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Logger;
import httl.spi.Parser;
import httl.spi.Translator;
import httl.spi.parsers.template.AbstractTemplate;
import httl.spi.parsers.template.AdaptiveTemplate;
import httl.spi.parsers.template.ForeachStatus;
import httl.spi.parsers.template.OrderedTypeMap;
import httl.spi.parsers.template.OutputStreamTemplate;
import httl.spi.parsers.template.WriterTemplate;
import httl.util.ClassUtils;
import httl.util.IOUtils;
import httl.util.StringCache;
import httl.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AbstractParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setParser(Parser)
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
    
    protected static final Pattern EXPRESSION_PATTERN = Pattern.compile("(\\$[!]?)\\{([^}]*)\\}");

    protected static final Pattern COMMA_PATTERN = Pattern.compile("\\s*\\,+\\s*");

    protected static final Pattern IN_PATTERN = Pattern.compile("(\\s+in\\s+)");

    protected static final Pattern ASSIGN_PATTERN = Pattern.compile("(\\s*=\\s*)");

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

    protected String varName = VAR;

    protected String setName = SET;

    protected String ifName = IF;

    protected String elseifName = ELSEIF;

    protected String elseName = ELSE;

    protected String foreachName = FOREACH;

    protected String breakifName = BREAKIF;

    protected String macroName = MACRO;

    protected String endName = END;

    protected String foreachStatus = FOREACH;
    
    protected String version;
    
    protected Engine engine;
    
    protected Compiler compiler;
    
	protected Translator translator;

	protected Filter textFilter;

	protected Filter valueFilter;

    protected Formatter<?> formatter;

    protected String[] importMacros;
    
    protected final Map<String, Template> importMacroTemplates = new ConcurrentHashMap<String, Template>();

	protected String[] importPackages;

    protected Set<String> importPackageSet;

    private final Map<Class<?>, Object> functions = new ConcurrentHashMap<Class<?>, Object>();

    protected static final String TEMPLATE_CLASS_PREFIX = AbstractTemplate.class.getPackage().getName() + ".Template_";
    
    protected static final Pattern SYMBOL_PATTERN = Pattern.compile("[^(_a-zA-Z0-9)]");
    
    protected boolean isOutputStream;

    protected boolean isOutputWriter;

    protected boolean sourceInClass;

	protected boolean textInClass;
	
	protected String outputEncoding;
	
	protected Logger logger;

    public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setImportMacros(String[] importMacros) {
		this.importMacros = importMacros;
	}

    public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setCompiler(Compiler compiler) {
		this.compiler = compiler;
	}

    public void setTranslator(Translator translator) {
		this.translator = translator;
	}

    public void setTextFilter(Filter filter) {
		this.textFilter = filter;
	}

	public void setValueFilter(Filter filter) {
		this.valueFilter = filter;
	}

	public void setFormatter(Formatter<?> formatter) {
		this.formatter = formatter;
	}
	
	public void setOutputStream(boolean outputStream) {
		this.isOutputStream = outputStream;
	}
	
	public void setOutputWriter(boolean outputWriter) {
		this.isOutputWriter = outputWriter;
	}

    public void setSourceInClass(boolean sourceInClass) {
		this.sourceInClass = sourceInClass;
	}

	public void setTextInClass(boolean textInClass) {
		this.textInClass = textInClass;
	}

	public void setAttributeNamespace(String namespace) {
		if (namespace != null && namespace.length() > 0) {
            namespace = namespace + ":";
            ifName = namespace + IF;
            elseifName = namespace + ELSEIF;
            elseName = namespace + ELSE;
            foreachName = namespace + FOREACH;
            breakifName = namespace + BREAKIF;
            setName = namespace + SET;
            varName = namespace + VAR;
            macroName = namespace + MACRO;
        }
	}

	public void setForeachStatus(String foreachStatus) {
		this.foreachStatus = foreachStatus;
	}

	public void setJavaVersion(String version) {
		this.version = version;
	}

	public void setImportPackages(String packages) {
		if (packages != null && packages.trim().length() > 0) {
            importPackages = packages.trim().split("\\s*\\,\\s*");
            importPackageSet = new HashSet<String>(Arrays.asList(importPackages));
        }
	}

    public void setImportMethods(Object[] importMethods) {
    	for (Object function : importMethods) {
    		this.functions.put(function.getClass(), function);
    	}
    }

    public void init() {
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

    protected abstract String doParse(Resource resoure, boolean stream, String source, Translator translator, 
                                      List<String> parameters, List<Class<?>> parameterTypes, 
                                      Set<String> variables, Map<String, Class<?>> types, Map<String, Class<?>> macros) throws IOException, ParseException;

    public Template parse(Resource resource) throws IOException, ParseException {
    	try {
    		Template writerTemplate = null;
	    	Template streamTemplate = null;
	    	if (isOutputWriter || ! isOutputStream) {
	    		Class<?> clazz = parseClass(resource, false);
	        	writerTemplate = (Template) clazz.getConstructor(Engine.class, Filter.class, Formatter.class, Map.class, Map.class)
						.newInstance(engine, valueFilter, formatter, functions, importMacroTemplates);
	    	}
	    	if (isOutputStream) {
	    		Class<?> clazz = parseClass(resource, true);
	    		streamTemplate = (Template) clazz.getConstructor(Engine.class, Filter.class, Formatter.class, Map.class, Map.class)
						.newInstance(engine, valueFilter, formatter, functions, importMacroTemplates);
	    	}
	    	if (writerTemplate != null && streamTemplate != null) {
	    		return new AdaptiveTemplate(writerTemplate, streamTemplate);
	    	} else if (streamTemplate != null) {
	    		return streamTemplate;
	    	} else {
	    		return writerTemplate;
	    	}
    	} catch (Exception e) {
			throw new ParseException("Filed to parse template: " + resource.getName() + ", cause: " + ClassUtils.toString(e), 0);
		}
    }
    
    protected Class<?> parseClass(Resource resource, boolean stream) throws IOException, ParseException {
        String name = TEMPLATE_CLASS_PREFIX + SYMBOL_PATTERN.matcher(resource.getName() + "_" + resource.getEncoding() + "_" + resource.getLastModified() + "_for_" + (stream ? "stream" : "writer")).replaceAll("_");
        try {
            return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
        	Set<String> variables = new HashSet<String>();
        	Map<String, Class<?>> types = new HashMap<String, Class<?>>();
        	StringBuilder statusInit = new StringBuilder();
            types.put(foreachStatus, ForeachStatus.class);
            for (String macro : importMacroTemplates.keySet()) {
            	types.put(macro, Template.class);
            	statusInit.append(Template.class.getName() + " " + macro + " = getImportMacros().get(\"" + macro + "\");");
            }
            statusInit.append(ForeachStatus.class.getName() + " " + foreachStatus + " = new " + ForeachStatus.class.getName() + "();\n");
            List<String> parameters = new ArrayList<String>();
            List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
            Map<String, Class<?>> macros = new HashMap<String, Class<?>>();
            StringBuilder textFields = new StringBuilder();
            StringBuilder textInits = new StringBuilder();
            String source = IOUtils.readToString(resource.getSource());
            String src = source;
            src = filterCData(src);
            src = filterComment(src);
            src = filterEscape(src);
            src = doParse(resource, stream, src, translator, parameters, parameterTypes, variables, types, macros);
            String code = filterStatement(src, textFilter, translator, textFields, textInits, types, new AtomicInteger(), stream);
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
            StringBuilder declare = new StringBuilder();
            for (String var : variables) {
                Class<?> type = types.get(var);
                String pkgName = type.getPackage() == null ? null : type.getPackage().getName();
                String typeName;
                if (pkgName != null && ("java.lang".equals(pkgName) 
                        || (importPackageSet != null && importPackageSet.contains(pkgName)))) {
                    typeName = type.getSimpleName();
                } else {
                    typeName = type.getCanonicalName();
                }
                declare.append(typeName + " " + var + " = " + ClassUtils.getInitCode(type) + ";\n");
            }
            StringBuilder funtionFileds = new StringBuilder();
            StringBuilder functionInits = new StringBuilder();
            for (Map.Entry<Class<?>, Object> function : functions.entrySet()) {
            	Class<?> functionType = function.getKey();
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
            
            // TODO 将ForeachStatus中的Stack改成直接生成局部变量，使用JVM的线程栈
            String methodCode = statusInit.toString() + declare + code;
            
            if (sourceInClass) {
        		textFields.append("private static final String $SRC = \"" + StringUtils.escapeString(source) + "\";\n");
        		textFields.append("private static final String $CODE = \"" + StringUtils.escapeString(methodCode) + "\";\n");
        	} else {
        		String sourceCodeId = StringCache.put(source);
        		textFields.append("private static final String $SRC = " + StringCache.class.getName() +  ".get(\"" + sourceCodeId + "\");\n");
        		String methodCodeId = StringCache.put(methodCode);
        		textFields.append("private static final String $CODE = " + StringCache.class.getName() +  ".get(\"" + methodCodeId + "\");\n");
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
                    + "public " + className + "("
                    + Engine.class.getName() + " engine, " 
                    + Filter.class.getName() + " filter, "
                    + Formatter.class.getName() + " formatter, "
                    + Map.class.getName() + " functions, " 
                    + Map.class.getName() + " importMacros) {\n" 
                    + "	super(engine, filter, formatter, functions, importMacros);\n"
                    + functionInits
                    + textInits
                    + "}\n"
                    + "\n"
                    + "protected void doRender(" + Map.class.getName() + " $parameters, " 
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
    			    + "	return \"" + resource.getEncoding() + "\";\n"
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
    			    + "public " + Reader.class.getName() + " getSource() throws " + IOException.class.getName() + " {\n"
    			    + "	return new " + StringReader.class.getName() + "($SRC);\n"
    			    + "}\n"
                    + "\n"
                    + "public " + String.class.getSimpleName() + " getCode() {\n"
                    + "	return $CODE;\n"
                    + "}\n"
                    + "\n"
                    + "public " + Map.class.getName() + " getParameterTypes() {\n"
                    + toTypeCode(parameters, parameterTypes)
                    + "}\n"
                    + "\n"
                    + "public " + Map.class.getName() + " getMacroTypes() {\n"
                    + toTypeCode(macros)
                    + "}\n"
                    + "\n"
                    + "}\n";
            if (logger.isDebugEnabled()) {
            	logger.debug("\n================================\n" + resource.getName() + "\n================================\n" + sorceCode + "\n================================\n");
            }
            return compiler.compile(sorceCode);
        } catch (Exception e) {
            throw new ParseException("Filed to parse template: " + resource.getName() + ", cause: " + ClassUtils.toString(e), 0);
        }
    }

    protected String toTypeCode(Map<String, Class<?>> types) {
    	StringBuilder buf = new StringBuilder("	" + Map.class.getName() + " types = " + "new " + HashMap.class.getName() + "();\n");
    	for (Map.Entry<String, Class<?>> entry : types.entrySet()) {
    		buf.append("	types.put(\"" + entry.getKey() + "\", " + entry.getValue().getName() + ".class);\n");
    	}
    	buf.append("	return " + Collections.class.getName() + ".unmodifiableMap(types);\n");
    	return buf.toString();
    }
    
    protected String toTypeCode(List<String> names, List<Class<?>> types) {
        StringBuilder buf = new StringBuilder("	return new " + OrderedTypeMap.class.getName() + "(");
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
            buf.append("new Class[] {\n");
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
        buf.append(");\n");
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
    
    protected String filterStatement(String message, Filter filter, Translator translator, StringBuilder textFields, StringBuilder textInits, Map<String, Class<?>> types, AtomicInteger seq, boolean stream) throws ParseException {
        int offset = 0;
        message = RIGHT + message + LEFT;
        StringBuffer buf = new StringBuffer();
        Matcher matcher = DIRECTIVE_PATTERN.matcher(message);
        while (matcher.find()) {
            String text = matcher.group(1);
            String len = matcher.group(2);
            String next = matcher.group(3);
            int length = 0;
            if (len != null && len.length() > 0) {
                length = Integer.parseInt(len);
            }
            if ("else".equals(next)) {
                if (text != null && text.trim().length() > 0) {
                    throw new ParseException("Found invaild text \"" + text.trim() + "\" before " + next + " directive!", offset);
                }
                matcher.appendReplacement(buf, "" + next);
            } else {
                matcher.appendReplacement(buf, Matcher.quoteReplacement("$output.write(" + filterExpression(text, filter, translator, textFields, textInits, types, offset, seq, stream) + ");\n" + next));
            }
            if (text != null) {
                offset += text.length();
            }
            offset += length;
        }
        matcher.appendTail(buf);
        return buf.toString().replace("$output.write();\n", "");
    }
    
    protected String filterExpression(String message, Filter filter, Translator translator, StringBuilder textFields, StringBuilder textInits, Map<String, Class<?>> types, int offset, AtomicInteger seq, boolean stream) throws ParseException {
        if (message == null || message.length() == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        Matcher matcher = EXPRESSION_PATTERN.matcher(message);
        int last = 0;
        while (matcher.find()) {
            int off = matcher.start(2) + offset;
            String expression = translator.translate(matcher.group(2), types, off).getCode();
            expression = "format(" + expression + ")";
            if (! "$!".equals(matcher.group(1))) {
                expression = "filter(" + expression + ")";
            }
            if (stream) {
                expression = "serialize(" + expression + ")";
            }
            String txt = message.substring(last, matcher.start());
            appendText(buf, txt, filter, textFields, textInits, seq, stream);
            buf.append(");\n$output.write(" + expression + ");\n$output.write(");
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
        appendText(buf, txt, filter, textFields, textInits, seq, stream);
        return buf.toString();
    }
    
    private void appendText(StringBuffer buf, String txt, Filter filter, StringBuilder textFields, StringBuilder textInits, AtomicInteger seq, boolean stream) {
        if (txt != null && txt.length() > 0) {
            txt = txt.replace(POUND_SPECIAL, POUND);
            txt = txt.replace(DOLLAR_SPECIAL, DOLLAR);
            if (filter != null) {
            	txt = filter.filter(txt);
            }
            if (txt != null && txt.length() > 0) {
                String var = "$TXT" + seq.incrementAndGet();
                if (stream) {
                	if (textInClass) {
                		textFields.append("private static final byte[] " + var + " = new byte[] {" + StringUtils.toByteString(StringUtils.toBytes(txt, outputEncoding)) + "};\n");
                	} else {
                		String txtId = StringCache.put(txt);
                		textFields.append("private static final byte[] " + var + " = " + StringUtils.class.getName() + ".toBytes(" + StringCache.class.getName() +  ".get(\"" + txtId + "\"), \"" + outputEncoding + "\");\n");
                	}
                } else {
                	if (textInClass) {
                		textFields.append("private static final String " + var + " = \"" + StringUtils.escapeString(txt) + "\";\n");
                	} else {
                		String txtId = StringCache.put(txt);
                		textFields.append("private static final String " + var + " = " + StringCache.class.getName() +  ".get(\"" + txtId + "\");\n");
                	}
                }
                buf.append(var);
            }
        }
    }
    
    protected String getStatementEndCode(String name, String value) throws ParseException {
        if (ifName.equals(name) || elseifName.equals(name) || elseName.equals(name)) {
            return "}\n"; // 插入结束指令
        } else if (foreachName.equals(name)) {
            return foreachStatus + ".increment();\n}\n" + foreachStatus + ".pop();\n"; // 插入结束指令
        }
        return null;
    }
    
    protected String getStatementCode(String name, String value, int begin, int offset, Translator translator,
                                    Set<String> variables, Map<String, Class<?>> types, 
                                    List<String> parameters, List<Class<?>> parameterTypes, boolean comment) throws ParseException {
        name = name == null ? null : name.trim();
        value = value == null ? null : value.trim();
        StringBuilder buf = new StringBuilder();
        if (ifName.equals(name)) {
            if (value == null || value.length() == 0) {
                throw new ParseException("The if expression == null!", begin);
            }
            buf.append("if (");
            buf.append(getConditionCode(translator.translate(value, types, offset)));
            buf.append(") {\n");
        } else if (elseifName.equals(name)) {
            if (value == null || value.length() == 0) {
                throw new ParseException("The elseif expression == null!", begin);
            }
            if (comment) {
                buf.append("} ");
            }
            buf.append("else if (");
            buf.append(getConditionCode(translator.translate(value, types, offset)));
            buf.append(") {\n");
        } else if (elseName.equals(name)) {
            if (value != null && value.length() > 0) {
                throw new ParseException("Unsupported else expression " + value, begin);
            }
            if (comment) {
                buf.append("} ");
            }
            buf.append("else {\n");
        } else if (foreachName.equals(name)) {
            if (value == null || value.length() == 0) {
                throw new ParseException("The foreach expression == null!", begin);
            }
            Matcher matcher = IN_PATTERN.matcher(value);
            if (! matcher.find()) {
                throw new ParseException("Not found \"in\" in foreach", offset);
            }
            int start = matcher.start(1);
            int end = matcher.end(1);
            Expression expression = translator.translate(value.substring(end).trim(), types, offset + end);
            Class<?> returnType = expression.getReturnType();
            String code = expression.getCode();
            String[] tokens = value.substring(0, start).trim().split("\\s+");
            String type;
            String var;
            if (tokens.length == 1) {
                // TODO 获取in参数List的泛型
                if (returnType.isArray()) {
                    type = returnType.getComponentType().getName();
                } else if (Map.class.isAssignableFrom(returnType)) {
                    type = Map.class.getName() + ".Entry";
                } else if (Collection.class.isAssignableFrom(returnType)
                        && StringUtils.isNamed(code.trim()) 
                        && types.get(code.trim() + ":0") != null) {
                    type = types.get(code.trim() + ":0").getName();
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
            	Class<?> keyType = types.get(code.trim() + ":0");
            	if (keyType != null) {
            		types.put(var + ":0", keyType);
            	}
            	Class<?> valueType = types.get(code.trim() + ":1");
            	if (valueType != null) {
            		types.put(var + ":1", valueType);
            	}
                code = "(" + code + ").entrySet()";
            }
            buf.append(getForeachCode(type, clazz, var, code));
        } else if (breakifName.equals(name)) {
            if (value == null || value.length() == 0) {
                throw new ParseException("The breakif expression == null!", begin);
            }
            buf.append("if (");
            buf.append(getConditionCode(translator.translate(value, types, offset)));
            buf.append(") break;");
        } else if (setName.equals(name)) {
            Matcher matcher = ASSIGN_PATTERN.matcher(value);
            if (! matcher.find()) {
                throw new ParseException("Not found \"=\" in set", offset);
            }
            int start = matcher.start(1);
            int end = matcher.end(1);
            String expr = value.substring(end).trim();
            Expression expression = translator.translate(expr, types, offset + end);
            String[] tokens = value.substring(0, start).trim().split("\\s+");
            String type;
            String var;
            if (tokens.length == 1) {
                type = expression.getReturnType().getName();
                var = tokens[0].trim();
            } else if (tokens.length == 2) {
                type = tokens[0].trim();
                var = tokens[1].trim();
            } else {
                throw new ParseException("Illegal: " + value, offset);
            }
            Class<?> clazz = ClassUtils.forName(importPackages, type);
            Class<?> cls = types.get(var);
            if (cls != null && ! cls.equals(clazz)) {
                throw new ParseException("set different type value to variable " + var + ", conflict types: " + cls.getName() + ", " + clazz.getName(), begin);
            }
            variables.add(var);
            types.put(var, clazz);
            buf.append(var + " = (" + type + ")(" + expression.getCode() + ");\n");
            buf.append("$parameters.put(\"");
            buf.append(var);
            buf.append("\", ");
            buf.append(ClassUtils.class.getName() + ".boxed(" + var + ")");
            buf.append(");\n");
        } else if (varName.equals(name)) {
            if (value == null || value.length() == 0) {
                throw new ParseException("The in parameters == null!", begin);
            }
            value = BLANK_PATTERN.matcher(value).replaceAll(" ");
            List<String> vs = new ArrayList<String>();
            List<Integer> os = new ArrayList<Integer>();
            Matcher matcher = VAR_PATTERN.matcher(value);
            while (matcher.find()) {
            	StringBuffer rep = new StringBuffer();
            	matcher.appendReplacement(rep, "$1");
            	vs.add(rep.toString());
            	os.add(offset + matcher.start());
			}
            for (int n = 0; n < vs.size(); n ++) {
            	String v = vs.get(n).trim();
            	int o = os.get(n);
            	String var;
                String type;
                int i = v.lastIndexOf(' ');
                if (i <= 0) {
                    type = String.class.getSimpleName();
                    var = v;
                } else {
                    type = v.substring(0, i).trim();
                    var = v.substring(i + 1).trim();
                }
                type = parseGenericType(type, var, types, o);
                parameters.add(var);
                parameterTypes.add(ClassUtils.forName(importPackages, type));
                types.put(var, ClassUtils.forName(importPackages, type));
                buf.append(type);
                buf.append(" ");
                buf.append(var);
                buf.append(" = (");
                buf.append(type);
                buf.append(") $parameters.get(\"");
                buf.append(var);
                buf.append("\");\n");
            }
        }else {
            throw new ParseException("Unsupported directive " + name, begin);
        }
        return buf.toString();
    }
    
    private void parseGenericTypeString(String type, int offset, List<String> types, List<Integer> offsets) throws ParseException {
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
    
    protected String parseGenericType(String type, String var, Map<String, Class<?>> types, int offset) throws ParseException {
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
    
    protected String getConditionCode(Expression expression) throws ParseException {
        return StringUtils.getConditionCode(expression.getReturnType(), expression.getCode());
    }

    protected String getForeachCode(String type, Class<?> clazz, String var, String code) {
        StringBuilder buf = new StringBuilder();
        String name = "_i_" + var;
        buf.append("for (" + Iterator.class.getName() + " " + name + " = " + ClassUtils.class.getName() + ".toIterator(" + foreachStatus + ".push(" + code + ")); " + name + ".hasNext();) {\n");
        if (clazz.isPrimitive()) {
            buf.append(type + " " + var + " = " + ClassUtils.class.getName() + ".unboxed((" + ClassUtils.getBoxedClass(clazz).getSimpleName() + ")" + name + ".next());\n");
        } else {
            buf.append(type + " " + var + " = (" + type + ") " + name + ".next();\n");
        }
        return buf.toString();
    }

    protected String getMacroPath(String template, String value) {
        if (value == null) {
            value = "";
        }
        value = value.trim();
        return template + "#" + value;
    }

}