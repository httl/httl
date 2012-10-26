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
import httl.spi.Configurable;
import httl.spi.Filter;
import httl.spi.Parser;
import httl.spi.Translator;
import httl.spi.parsers.template.AbstractTemplate;
import httl.spi.parsers.template.ForeachStatus;
import httl.spi.parsers.template.OrderedTypeMap;
import httl.spi.parsers.template.OutputStreamTemplate;
import httl.spi.parsers.template.WriterTemplate;
import httl.util.ClassUtils;
import httl.util.IOUtils;
import httl.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public abstract class AbstractParser implements Parser, Configurable {
    
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
    
    protected String[] importPackages;

    protected Set<String> importPackageSet;

    protected static final String TEMPLATE_CLASS_PREFIX = AbstractTemplate.class.getPackage().getName() + ".Template_";
    
    protected static final Pattern SYMBOL_PATTERN = Pattern.compile("[^(_a-zA-Z0-9)]");
    
    protected boolean isOutput = false;
    
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void configure(Map<String, String> config) {
        String output = config.get(OUTPUT_STREAM);
        if (output != null && output.trim().length() > 0) {
            isOutput = "true".equalsIgnoreCase(output);
        }
        String namespace = config.get(ATTRIBUTE_NAMESPACE);
        if (namespace != null && namespace.trim().length() > 0) {
            namespace = namespace.trim() + ":";
            ifName = namespace + IF;
            elseifName = namespace + ELSEIF;
            elseName = namespace + ELSE;
            foreachName = namespace + FOREACH;
            breakifName = namespace + BREAKIF;
            setName = namespace + SET;
            varName = namespace + VAR;
            macroName = namespace + MACRO;
        }
        String status = config.get(FOREACH_STATUS);
        if (status != null && status.trim().length() > 0 
                && StringUtils.isNamed(status.trim())) {
            foreachStatus = status.trim();
        }
        version = config.get(JAVA_VERSION);
        if (version != null) {
            version = version.trim();
        }
        String packages = config.get(IMPORT_PACKAGES);
        if (packages != null && packages.trim().length() > 0) {
            importPackages = packages.trim().split("\\s*\\,\\s*");
            importPackageSet = new HashSet<String>(Arrays.asList(importPackages));
        }
    }
    
    protected abstract String doParse(String name, String source, Translator resolver, 
                                      List<String> parameters, List<Class<?>> parameterTypes, 
                                      Set<String> variables, Map<String, Class<?>> types) throws IOException, ParseException;

    public Template parse(Resource resource) throws IOException, ParseException {
        try {
            String name = TEMPLATE_CLASS_PREFIX + SYMBOL_PATTERN.matcher(resource.getName() + "_" + resource.getEncoding() + "_" + resource.getLastModified()).replaceAll("_");
            Class<?> clazz;
            try {
                clazz = Class.forName(name, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                Translator resolver = engine.getTranslator();
                Filter filter = engine.getTextFilter();
                Set<String> variables = new HashSet<String>();
                Map<String, Class<?>> types = new HashMap<String, Class<?>>();
                types.put(foreachStatus, ForeachStatus.class);
                List<String> parameters = new ArrayList<String>();
                List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
                StringBuilder fields = new StringBuilder();
                String src = IOUtils.readToString(resource.getSource());
                src = filterCData(src);
                src = filterComment(src);
                src = filterEscape(src);
                src = doParse(resource.getName(), src, resolver, parameters, parameterTypes, variables, types);
                String code = filterStatement(src, filter, resolver, fields, types, new AtomicInteger());
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
                List<String> returns = new ArrayList<String>();
                List<Class<?>> returnTypes = new ArrayList<Class<?>>();
                String methodCode = declare.toString() + code;
                String sorceCode = "package " + packageName + ";\n" 
                        + imports.toString()
                        + "public class " + className + " extends " + (isOutput ? OutputStreamTemplate.class.getName() : WriterTemplate.class.getName()) + " {\n" 
                         + fields
                        + "public " + className + "(" + Engine.class.getName() + " engine, " 
                        + Resource.class.getName() + " resource) {\n" 
                        + "super(engine, resource);\n" 
                        + "}\n"
                        + "protected void doRender(" + Map.class.getName() + " $parameters, " 
                        + (isOutput ? OutputStream.class.getName() : Writer.class.getName())
                        + " $output) throws " + Exception.class.getName() + " {\n" 
                        + ForeachStatus.class.getName() + " " + foreachStatus + " = new " + ForeachStatus.class.getName() + "();\n"
                        + methodCode 
                        + "}\n"
                        + "public " + String.class.getSimpleName() + " getCode() {\n"
                        + "return \"" + StringUtils.escapeString(methodCode) + "\";"
                        + "}\n"
                        + "public " + Map.class.getName() + " getParameterTypes() {\n"
                        + toTypeCode(parameters, parameterTypes)
                        + "}\n"
                        + "public " + Map.class.getName() + " getReturnTypes() {\n"
                        + toTypeCode(returns, returnTypes)
                        + "}\n"
                        + "}";
                Compiler compiler = engine.getCompiler();
                clazz = compiler.compile(sorceCode);
            }
            Constructor<?> constructor = clazz.getConstructor(new Class<?>[] { Engine.class, Resource.class});
            return (Template) constructor.newInstance(new Object[] { engine, resource });
        } catch (IOException e) {
            throw e;
        } catch (ParseException e) {
            throw e;
        } catch (Throwable e) {
            throw new ParseException("Filed to parse template: " + resource.getName() + ", cause: " + ClassUtils.toString(e), 0);
        }
    }
    
    protected String toTypeCode(List<String> names, List<Class<?>> types) {
        StringBuilder buf = new StringBuilder("return new " + OrderedTypeMap.class.getName() + "(");
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
    
    protected String filterStatement(String message, Filter filter, Translator resolver, StringBuilder fields, Map<String, Class<?>> types, AtomicInteger seq) throws ParseException {
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
                matcher.appendReplacement(buf, Matcher.quoteReplacement("$output.write(" + filterExpression(text, filter, resolver, fields, types, offset, seq) + ");\n" + next));
            }
            if (text != null) {
                offset += text.length();
            }
            offset += length;
        }
        matcher.appendTail(buf);
        return buf.toString().replace("$output.write();\n", "");
    }
    
    protected String filterExpression(String message, Filter filter, Translator resolver, StringBuilder fields, Map<String, Class<?>> types, int offset, AtomicInteger seq) throws ParseException {
        if (message == null || message.length() == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        Matcher matcher = EXPRESSION_PATTERN.matcher(message);
        int last = 0;
        while (matcher.find()) {
            int off = matcher.start(2) + offset;
            String expression = resolver.translate(matcher.group(2), types, off).getCode();
            expression = "format(" + expression + ")";
            if (! "$!".equals(matcher.group(1))) {
                expression = "filter(" + expression + ")";
            }
            if (isOutput) {
                expression = "serialize(" + expression + ")";
            }
            String txt = message.substring(last, matcher.start());
            appendText(buf, txt, filter, fields, seq);
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
        appendText(buf, txt, filter, fields, seq);
        return buf.toString();
    }
    
    private void appendText(StringBuffer buf, String txt, Filter filter, StringBuilder fields, AtomicInteger seq) {
        if (txt != null && txt.length() > 0) {
            txt = txt.replace(POUND_SPECIAL, POUND);
            txt = txt.replace(DOLLAR_SPECIAL, DOLLAR);
            txt = filter.filter(txt);
            if (txt != null && txt.length() > 0) {
                String var = "$TXT" + seq.incrementAndGet();
                if (isOutput) {
                    fields.append("protected static final byte[] " + var + " = new byte[] {" + StringUtils.toByteString(txt.getBytes()) + "};\n");
                } else {
                    fields.append("protected static final String " + var + " = \"" + StringUtils.escapeString(txt) + "\";\n");
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
    
    protected String getStatementCode(String name, String value, int begin, int offset, Translator resolver,
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
            buf.append(getConditionCode(resolver.translate(value, types, offset)));
            buf.append(") {\n");
        } else if (elseifName.equals(name)) {
            if (value == null || value.length() == 0) {
                throw new ParseException("The elseif expression == null!", begin);
            }
            if (comment) {
                buf.append("} ");
            }
            buf.append("else if (");
            buf.append(getConditionCode(resolver.translate(value, types, offset)));
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
            Expression expression = resolver.translate(value.substring(end).trim(), types, offset + end);
            Class<?> returnType = expression.getReturnType();
            String code = expression.getCode();
            if (Map.class.isAssignableFrom(returnType)) {
                code = "(" + code + ").entrySet()";
            }
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
            buf.append(getForeachCode(type, clazz, var, code));
        } else if (breakifName.equals(name)) {
            if (value == null || value.length() == 0) {
                throw new ParseException("The breakif expression == null!", begin);
            }
            buf.append("if (");
            buf.append(getConditionCode(resolver.translate(value, types, offset)));
            buf.append(") break;");
        } else if (setName.equals(name)) {
            Matcher matcher = ASSIGN_PATTERN.matcher(value);
            if (! matcher.find()) {
                throw new ParseException("Not found \"=\" in set", offset);
            }
            int start = matcher.start(1);
            int end = matcher.end(1);
            String expr = value.substring(end).trim();
            Expression expression = resolver.translate(expr, types, offset + end);
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
            value = value.replaceAll("(<\\s*[_.0-9a-zA-Z]+\\s*)\\,", "$1/");
            value = value.replaceAll("([_.0-9a-zA-Z]+\\s*>\\s*)\\,", "$1/");
            String[] vs = value.split("\\,");
            for (String v : vs) {
                v = v.trim().replaceAll("\\s+", " ").replaceAll("/", ",");
                String type;
                String var;
                int i = v.lastIndexOf(' ');
                if (i <= 0) {
                    type = String.class.getSimpleName();
                    var = v;
                } else {
                    type = v.substring(0, i);
                    var = v.substring(i + 1).trim();
                }
                type = parseGenericType(type, var, types, offset);
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
    
    protected String parseGenericType(String type, String var, Map<String, Class<?>> types, int offset) throws ParseException {
        int i = type.indexOf('<');
        if (i > 0) {
            if (! type.endsWith(">")) {
                throw new ParseException("Illegal type: " + type, offset);
            }
            String[] genericTypes = COMMA_PATTERN.split(type.substring(i + 1, type.length() - 1).trim());
            if (genericTypes != null && genericTypes.length > 0) {
                for (int k = 0; k < genericTypes.length; k ++) {
                    String genericVar = var + ":" + k;
                    String genericType = parseGenericType(genericTypes[k], genericVar, types, offset);
                    types.put(genericVar, ClassUtils.forName(importPackages, genericType));
                }
            }
            type = type.substring(0, i);
        }
        return type;
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