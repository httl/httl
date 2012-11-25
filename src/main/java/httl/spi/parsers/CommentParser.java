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

import httl.Resource;
import httl.Template;
import httl.spi.Parser;
import httl.spi.Translator;
import httl.spi.loaders.StringResource;
import httl.util.LinkedStack;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommentParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setParser(Parser)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CommentParser extends AbstractParser {
    
    protected static final Pattern STATEMENT_PATTERN = Pattern.compile("<!--#([a-z:]+)(.*?)-->", Pattern.DOTALL);
    
    protected Pattern getPattern() {
        return STATEMENT_PATTERN;
    }
    
    protected String getDiretive(String name, String value) {
        return "<!--#" + name + "(" + value + ")-->";
    }
    
    protected String doParse(Resource resource, boolean stream, String source, Translator translator, 
                             List<String> parameters, List<Class<?>> parameterTypes, 
                             Set<String> variables, Map<String, Class<?>> types, Map<String, Class<?>> returnTypes, Map<String, Class<?>> macros) throws IOException, ParseException {
        LinkedStack<String> nameStack = new LinkedStack<String>();
        LinkedStack<String> valueStack = new LinkedStack<String>();
        StringBuffer macro = null;
        int macroStart = 0;
        int macroParameterStart = 0;
        StringBuffer buf = new StringBuffer();
        Matcher matcher = getPattern().matcher(source);
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = matcher.group(2);
            value = value == null ? null : value.trim();
            int offset = matcher.end(1);
            if (value != null && value.length() > 0) {
                offset = matcher.start(2) + value.indexOf('(') + 1;
                value = value.trim();
                if (value.length() > 0) {
                    if (value.length() < 2 || ! value.startsWith("(") || ! value.endsWith(")")) {
                        throw new ParseException("Invalied statement " + matcher.group(), matcher.start());
                    }
                    value = value.substring(1, value.length() - 1);
                }
            } else {
                offset = matcher.end(1);
            }
            if (endName.equals(name)) {
                String startName = nameStack.pop();
                String startValue = valueStack.pop();
                while(elseifName.equals(startName) || elseName.equals(startName)) {
                    startName = nameStack.pop();
                    startValue = valueStack.pop();  
                }
                if (macro != null) {
                    if (macroName.equals(startName)) {
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
                        matcher.appendReplacement(macro, "");
                        String key = getMacroPath(resource.getName(), var);
                        String es = macro.toString();
                        if (param != null && param.length() > 0) {
                            es = getDiretive(varName, param) + es;
                        }
                        macros.put(var, parseClass(new StringResource(engine, key, resource.getEncoding(), resource.getLastModified(), es), stream, macroParameterStart));
                        Class<?> cls = types.get(var);
                        if (cls != null && ! cls.equals(Template.class)) {
                            throw new ParseException("Duplicate macro variable " + var + ", conflict types: " + cls.getName() + ", " + Template.class.getName(), macroParameterStart);
                        }
                        variables.add(var);
                        types.put(var, Template.class);
                        buf.append(LEFT);
                        buf.append(matcher.end() - macroStart);
                        buf.append(var + " = getMacros().get(\"" + var + "\");\n");
                        buf.append(RIGHT);
                        macro = null;
                        macroStart = 0;
                        macroParameterStart = 0;
                    } else {
                        matcher.appendReplacement(macro, "$0");
                    }
                } else {
                    matcher.appendReplacement(buf, "");
                    buf.append(LEFT);
                    buf.append(matcher.group().length());
                    String code = getStatementEndCode(startName, startValue);
                    buf.append(code);
                    buf.append(RIGHT);
                }
            } else {
                if (ifName.equals(name) || elseifName.equals(name) 
                        || elseName.equals(name) || foreachName.equals(name)
                        || macroName.equals(name)) {
                    nameStack.push(name);
                    valueStack.push(value);
                }
                if (macro != null) {
                    matcher.appendReplacement(macro, "$0");
                } else {
                    matcher.appendReplacement(buf, "");
                    if (macroName.equals(name)) {
                        if (value == null || value.trim().length() == 0) {
                            throw new ParseException("Macro name == null!", matcher.start(1));
                        }
                        macro = new StringBuffer();
                        macroStart = matcher.start();
                        macroParameterStart = matcher.start(1);
                    } else {
                        buf.append(LEFT);
                        buf.append(matcher.group().length());
                        String code = getStatementCode(name, value, matcher.start(1), offset, translator, variables, types, returnTypes, parameters, parameterTypes, true);
                        buf.append(code);
                        buf.append(RIGHT);
                    }
                }
            }
        }
        matcher.appendTail(buf);
        return buf.toString();
    }
    
}