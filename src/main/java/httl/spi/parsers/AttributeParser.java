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
import httl.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;


/**
 * AttributeParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setParser(Parser)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class AttributeParser extends AbstractParser {

    protected String doParse(Resource resource, boolean stream, String reader, Translator translator, 
                             List<String> parameters, List<Class<?>> parameterTypes, 
                             Set<String> variables, Map<String, Class<?>> types, Map<String, Class<?>> macros) throws IOException, ParseException {
        Source source = new Source(reader);
        OutputDocument document = new OutputDocument(source);
        parseAttribute(resource, stream, source, source, document, translator, parameters, parameterTypes, variables, types, macros);
        return document.toString();
    }

    // 替换子元素中的指令属性
    private void parseAttribute(Resource resource, boolean stream, Source source, 
                                 Segment segment, OutputDocument document, 
                                 Translator translator, 
                                 List<String> parameters, List<Class<?>> parameterTypes, 
                                 Set<String> variables, Map<String, Class<?>> types, Map<String, Class<?>> macros) throws IOException, ParseException {
        List<Element> elements = segment.getChildElements();
        if (elements == null) {
            return;
        }
        for (Iterator<Element> elementIterator = elements.iterator(); elementIterator.hasNext();) {
            Element element = elementIterator.next();
            if (element == null) {
                continue;
            }
            Attributes attributes = element.getAttributes();
            if (attributes == null) {
                continue;
            }
            List<Attribute> statements = new ArrayList<Attribute>();
            Attribute macro = null;
            for (Iterator<Attribute> attributeIterator = attributes.iterator(); attributeIterator.hasNext();) {
                Attribute attribute = (Attribute)attributeIterator.next();
                if (attribute == null) {
                    continue;
                }
                String name = attribute.getName().trim();
                if (macroName.equals(name)) {
                    macro = attribute;
                    break;
                }
                if (! ifName.equals(name) && ! elseifName.equals(name) && ! elseName.equals(name)
                        && ! foreachName.equals(name) && ! breakifName.equals(name)
                        && ! setName.equals(name) && ! varName.equals(name)) {
                    continue;
                }
                statements.add(attribute);
            }
            if (macro != null) {
                String value = macro.getValue();
                if (value == null || value.trim().length() == 0) {
                    throw new ParseException("Macro name == null!", macro.getBegin());
                }
                String var;
                String param;
                value = value.trim();
                int i = value.indexOf('(');
                if (i > 0) {
                    if (! value.endsWith(")")) {
                        throw new ParseException("Invalid macro parameters " + value, macro.getBegin());
                    }
                    var = value.substring(0, i).trim();
                    param = value.substring(i + 1, value.length() - 1).trim();
                } else {
                    var = value;
                    param = null;
                }
                if (var == null || var.trim().length() == 0) {
                    throw new ParseException("Macro name == null!", macro.getBegin());
                }
                if (! StringUtils.isNamed(var)) {
                    throw new ParseException("Invalid macro name " + var, macro.getBegin());
                }
                String key = getMacroPath(resource.getName(), var);
                String es = element.toString();
                es = es.substring(0, macro.getBegin() - 1 - element.getBegin()) 
                    + (param == null || param.length() == 0 ? "" : " in=\"" + param + "\"")
                    + es.substring(macro.getEnd() - element.getBegin()); // 去掉macro属性
                macros.put(var, parseClass(new StringResource(engine, key, resource.getEncoding(), resource.getLastModified(), es), stream));
                Class<?> cls = types.get(var);
                if (cls != null && ! cls.equals(Template.class)) {
                    throw new ParseException("Duplicate macro variable " + var + ", conflict types: " + cls.getName() + ", " + Template.class.getName(), macro.getBegin());
                }
                variables.add(var);
                types.put(var, Template.class);
                StringBuffer buf = new StringBuffer();
                buf.append(LEFT);
                buf.append(element.length());
                buf.append(var + " = getMacros().get(\"" + var + "\");\n");
                buf.append(RIGHT);
                document.insert(element.getBegin(), buf.toString()); // 插入块指令
                document.remove(element); // 移除宏
                continue;
            }
            int comment = 0;
            LinkedStack<String> ends = new LinkedStack<String>();
            for (Attribute attribute : statements) {
                String name = attribute.getName().trim();
                String value = attribute.getValue();
                if (value == null) {
                    value = "";
                }
                value = value.trim();
                StringBuffer buf = new StringBuffer();
                buf.append(LEFT);
                buf.append(comment + attribute.length() + 1);
                comment = 0;
                int offset = attribute.toString().indexOf("\"");
                if (offset <= 0) {
                    offset = attribute.getBegin() + name.length() + 2;
                } else {
                    offset ++;
                }
                String code = getStatementCode(name, value, attribute.getBegin(), offset, translator, variables, types, parameters, parameterTypes, false);
                buf.append(code);
                buf.append(RIGHT);
                document.insert(element.getBegin(), buf.toString()); // 插入块指令
                document.remove(new Segment(source, attribute.getBegin() - 1, attribute.getEnd())); // 移除属性
                String end = getStatementEndCode(name, value);
                if (end != null && end.length() > 0) {
                    ends.push(end); // 插入结束指令
                }
            }
            while (ends.size() > 0) {
                String end = ends.pop();
                document.insert(element.getEnd(), LEFT + end + RIGHT); // 插入结束指令
            }
            parseAttribute(resource, stream, source, element, document, translator, parameters, parameterTypes, variables, types, macros); // 递归处理子标签
        }
    }
    
}
