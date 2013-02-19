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

import httl.Resource;
import httl.Template;
import httl.spi.Parser;
import httl.spi.Translator;
import httl.spi.loaders.resources.StringResource;
import httl.internal.util.DfaScanner;
import httl.internal.util.IOUtils;
import httl.internal.util.LinkedStack;
import httl.internal.util.StringUtils;
import httl.internal.util.Token;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TextParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setParser(Parser)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class TextParser extends AbstractParser {

	//单字母命名, 保证状态机图简洁
	
	// BREAK，结束片段，包含当前字符
	private static final int B = DfaScanner.BREAK - 1;

	// PUSH，压栈
	private static final int P = DfaScanner.PUSH - 4;

	// POP，弹栈
	private static final int O = DfaScanner.POP - 4;

	// 表达式语法状态机图
	// 行表示状态
	// 行列交点表示, 在该状态时, 遇到某类型的字符时, 切换到的下一状态(数组行号)
	// E/B/T表示接收前面经过的字符为一个片断, R表示错误状态(这些状态均为负数)
	static final int states[][] = {
				  // 0.空格, 1.字母, 2.井号, 3.左括号, 4.右括号, 5.双引号, 6.单引号, 7.反引号, 8.反斜线, 9.美元号, 10.感叹号, 11.左大括号, 12.右大括号, 9.其它
		/* 0.起始 */{ 1, 1, 2, 1, 1, 1, 1, 1, 1, 11, 1, 1, 1, 1 }, // 初始状态或上一片断刚接收完成状态
		/* 1.文本 */{ 1, 1, B, 1, 1, 1, 1, 1, 1, 11, 1, 1, 1, 1 }, // 非指令文本内容
		/* 2.指令 */{ 2, 3, 2, 1, 1, 1, 1, 1, 1, 1, 12, 13, 1, 1 }, // 指令提示符
		/* 3.名称 */{ B, 3, B, P, B, B, B, B, B, B, B, B, B, B }, // 指令名称
		/* 4.参数 */{ 4, 4, 4, P, O, 5, 6, 7, 4, 4, 4, 4, 4, 4 }, // 括号参数配对
		/* 5.字符 */{ 5, 5, 5, 5, 5, 4, 5, 5, 8, 5, 5, 5, 5, 5 }, // 双引号字符串识别
		/* 6.字符 */{ 6, 6, 6, 6, 6, 6, 4, 6, 9, 6, 6, 6, 6, 6 }, // 单引号字符串识别
		/* 7.字符 */{ 7, 7, 7, 7, 7, 7, 7, 4, 10, 7, 7, 7, 7, 7 }, // 反单引号字符串识别
		/* 8.转义 */{ 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }, // 双引号字符串转义
		/* 9.转义 */{ 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 }, // 单引号字符串转义
		/*10.转义 */{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, // 反单引号字符串转义
		
		/*11.插值 */{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 13, 1, 1 }, // 插值提示符
		/*12.非滤 */{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, B, 13, 7, 7 }, // 插值非过滤提示符
		/*13.变量 */{ 13, 13, 13, 13, 13, 14, 15, 16, 13, 13, 13, 13, 1, 13 }, // 插值表达式变量
		/*14.字符 */{ 14, 14, 14, 14, 14, 13, 14, 14, 17, 14, 14, 14, 14, 14 }, // 双引号字符串识别
		/*15.字符 */{ 15, 15, 15, 15, 15, 15, 13, 15, 18, 15, 15, 15, 15, 15 }, // 单引号字符串识别
		/*16.字符 */{ 16, 16, 16, 16, 16, 16, 16, 13, 19, 16, 16, 16, 16, 16 }, // 反单引号字符串识别
		/*17.转义 */{ 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14 }, // 双引号字符串转义
		/*18.转义 */{ 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15 }, // 单引号字符串转义
		/*19.转义 */{ 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 }, // 反单引号字符串转义
	};

	static int getCharType(char ch) {
		switch (ch) {
			case ' ': case '\t': case '\n': case '\r': case '\f': case '\b':
				return 0;
			case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : 
			case 'h' : case 'i' : case 'j' : case 'k' : case 'l' : case 'm' : case 'n' : 
			case 'o' : case 'p' : case 'q' : case 'r' : case 's' : case 't' : 
			case 'u' : case 'v' : case 'w' : case 'x' : case 'y' : case 'z' :
				return 1;
			case '#' : 
				return 2;
			case '(' : 
				return 3;
			case ')' : 
				return 4;
			case '\"' : 
				return 5;
			case '\'' : 
				return 6;
			case '`' : 
				return 7;
			case '\\' : 
				return 8;
			case '$' : 
				return 9;
			case '!' : 
				return 10;
			case '{' : 
				return 11;
			case '}' : 
				return 12;
			default:
				return 13;
		}
	}

	private static DfaScanner scanner = new DfaScanner() {
		@Override
		public int next(int state, char ch) {
			return states[state][getCharType(ch)];
		}
	};
	
	public static void main(String[] args) throws Exception {
		List<Token> tokens = scanner.scan(IOUtils.readToString(new InputStreamReader(TextParser.class.getClassLoader().getResourceAsStream("text.httl"))));
		for (Token token : tokens) {
			System.out.println(token.getMessage().replace("\n", "\\n").replace("\r", "\\r"));
		}
	}
	
	protected String doParse(Resource resource, boolean stream, String source, Translator translator, 
							 List<String> parameters, List<Class<?>> parameterTypes, 
							 Set<String> setVariables, Set<String> getVariables, Map<String, Class<?>> types, 
							 Map<String, Class<?>> returnTypes, Map<String, Class<?>> macros, StringBuilder textFields, AtomicInteger seq) throws IOException, ParseException {
		LinkedStack<String> nameStack = new LinkedStack<String>();
		LinkedStack<String> valueStack = new LinkedStack<String>();
		StringBuffer macro = null;
		int macroStart = 0;
		int macroParameterStart = 0;
		StringBuffer buf = new StringBuffer();
		List<Token> tokens = scanner.scan(source);
		for (Token token : tokens) {
			int type = token.getType();
			String message = token.getMessage();
			int startOffset = token.getOffset();
			int endOffset = token.getOffset() + message.length();
			if (type == 1) {
				if (macro != null) {
					macro.append(message);
				} else {
					buf.append(message);
				}
				continue;
			}
			if (message.startsWith("##")) {
				continue;
			}
			int s = message.indexOf('(');
			String name;
			String value;
			int exprOffset;
			if (s > 0) {
				exprOffset = startOffset + s;
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
			if (endDirective.equals(name)) {
				if (nameStack.isEmpty()) {
					throw new ParseException("The #end directive without start directive.", startOffset);
				}
				String startName = nameStack.pop();
				String startValue = valueStack.pop();
				while(elseifDirective.equals(startName) || elseDirective.equals(startName)) {
					if (nameStack.isEmpty()) {
						throw new ParseException("The #" + startName + " directive without #if directive.", startOffset);
					}
					String oldStartName = startName;
					startName = nameStack.pop();
					startValue = valueStack.pop();  
					if (! ifDirective.equals(startName) && ! elseifDirective.equals(startName)) {
						throw new ParseException("The #" + oldStartName + " directive without #if directive.", startOffset);
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
						buf.append(LEFT);
						buf.append(endOffset - macroStart);
						if (StringUtils.isNotEmpty(out)) {
							getVariables.add(var);
							String code = getExpressionCode(out, var, var, Template.class, stream, getVariables, textFields, seq);
							buf.append(code);
						} else if (StringUtils.isNotEmpty(set)) {
							getVariables.add(var);
							String setValue = set + " " + var + ".evaluate()";
							String code = getStatementCode(setDirective, setValue, startOffset, exprOffset, translator, setVariables, getVariables, types, returnTypes, parameters, parameterTypes, true);
							buf.append(code);
						}
						buf.append(RIGHT);
						macro = null;
						macroStart = 0;
						macroParameterStart = 0;
					} else {
						macro.append(message);
					}
				} else {
					String end = getStatementEndCode(startName);
					if (StringUtils.isNotEmpty(end)) {
						buf.append(LEFT);
						buf.append(message.length());
						buf.append(end);
						buf.append(RIGHT);
					} else {
						throw new ParseException("The #end directive without start directive.", startOffset);
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
							throw new ParseException("Macro name == null!", startOffset);
						}
						macro = new StringBuffer();
						macroStart = token.getOffset();
						macroParameterStart = exprOffset;
					} else {
						buf.append(LEFT);
						buf.append(message.length());
						String code = getStatementCode(name, value, startOffset, exprOffset, translator, setVariables, getVariables, types, returnTypes, parameters, parameterTypes, true);
						buf.append(code);
						buf.append(RIGHT);
					}
				}
			}
		}
		return buf.toString();
	}
	
}