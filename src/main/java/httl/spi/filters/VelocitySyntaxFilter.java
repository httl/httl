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
package httl.spi.filters;

import httl.internal.util.DfaScanner;
import httl.internal.util.Token;
import httl.spi.Filter;

import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * VelocitySyntaxFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTemplateFilter(Filter)
 * @see httl.spi.translators.InterpretedTranslator#setTemplateFilter(Filter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class VelocitySyntaxFilter extends AbstractFilter  {

	// 单字母命名, 保证状态机图简洁

	// END，结束片段，包含当前字符
	private static final int E = DfaScanner.BREAK;

	// BREAK，结束片段，并退回一个字符，即不包含当前字符
	private static final int B = DfaScanner.BREAK - 1;

	// PUSH，压栈1，即指令小括号栈，并回到状态4，即指令参数
	private static final int P = DfaScanner.PUSH - 4;

	// POP，弹栈1，即指令小括号栈，并回到状态4，即指令参数
	private static final int O = DfaScanner.POP - 4;

	// PUSH，压栈2，即插值大括号栈，并回到状态7，即插值参数
	private static final int P2 = DfaScanner.PUSH - 7;

	// POP，弹栈2，即插值大括号栈，并回到状态7，即插值参数
	private static final int O2 = DfaScanner.POP - 7;

	// PUSH，压栈3，即插值小括号栈，并回到状态28，即插值参数
	private static final int P3 = DfaScanner.PUSH - 28;

	// POP，弹栈3，即插值小括号栈，并回到状态28，即插值参数，栈空回到状态29，即点号连接
	private static final int O3 = DfaScanner.POP - 28 - DfaScanner.EMPTY * 29;

	// 插值语法状态机图
	// 行表示状态
	// 行列交点表示, 在该状态时, 遇到某类型的字符时, 切换到的下一状态(数组行号)
	// E/B/T表示接收前面经过的字符为一个片断, R表示错误状态(这些状态均为负数)
	static final int states[][] = {
				  // 0.\s, 1.a-z, 2.#, 3.$, 4.!, 5.*, 6.(, 7.), 8.[, 9.], 10.{, 11.}, 12.", 13.', 14.`, 15.\, 16.\r\n, 17.., 18.其它
		/* 0.起始 */ { 1, 1, 2, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 1, }, // 初始状态或上一片断刚接收完成状态
		/* 1.文本 */ { 1, 1, B, B, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 1, }, // 非指令文本内容
		
		/* 2.指令 */ { 1, 3, 9, B, 6, 10, 1, 1, 12, 1, 26, 1, 1, 1, 1, 1, 1, 1, 1, }, // 指令提示符
		/* 3.指名 */ { B, 3, B, B, B, B, P, B, B, B, B, B, B, B, B, B, B, B, B, }, // 指令名
		/* 4.指参 */ { 4, 4, 4, 4, 4, 4, P, O, 4, 4, 4, 4, 14, 16, 18, 4, 4, 4, 4, }, // 指令参数
		
		/* 5.插值 */ { 1, 27, B, B, 6, 1, 1, 1, 1, 1, P2, 1, 1, 1, 1, 1, 1, 1, 1, }, // 插值提示符
		/* 6.非滤 */ { 1, 1, B, B, 1, 1, 1, 1, 1, 1, P2, 1, 1, 1, 1, 1, 1, 1, 1, }, // 非过滤插值
		/* 7.插参 */ { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, P2, O2, 20, 22, 24, 7, 7, 7, 7, }, // 插值参数
		
		/* 8.转义 */ { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, }, // 井号美元号转义
		/* 9.行注 */ { 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, B, 9, 9, }, // 双井号行注释
		/* 10.块注 */ { 10, 10, 10, 10, 10, 11, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, }, // 井星号块注释
		/* 11.结块 */ { 10, 10, E, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, }, // 井星号块注释结束
		/* 12.字面 */ { 12, 12, 12, 12, 12, 12, 12, 12, 12, 13, 12, 12, 12, 12, 12, 12, 12, 12, 12, }, // 井方号块字面不解析块
		/* 13.结字 */ { 12, 12, E, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, }, // 井方号块字面不解析块结束
		
		/* 14.字串 */ { 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 4, 14, 14, 15, 14, 14, 14, }, // 指令参数双引号字符串
		/* 15.转字 */ { 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, }, // 指令参数双引号字符串转义
		/* 16.字串 */ { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 4, 16, 17, 16, 16, 16, }, // 指令参数单引号字符串
		/* 17.转字 */ { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, }, // 指令参数单引号字符串转义
		/* 18.字串 */ { 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 4, 19, 18, 18, 18, }, // 指令参数反单引号字符串
		/* 19.转字 */ { 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, }, // 指令参数反单引号字符串转义
		
		/* 20.字串 */ { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 7, 20, 20, 21, 20, 20, 20, }, // 插值参数双引号字符串
		/* 21.转字 */ { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, }, // 插值参数双引号字符串转义
		/* 22.字串 */ { 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 7, 22, 23, 22, 22, 22, }, // 插值参数单引号字符串
		/* 23.转字 */ { 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, }, // 插值参数单引号字符串转义
		/* 24.字串 */ { 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 7, 25, 24, 24, 24, }, // 插值参数反单引号字符串
		/* 25.转字 */ { 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, }, // 插值参数反单引号字符串转义
		
		/* 26.指名 */ { B, 26, B, B, B, B, P, B, B, B, B, E, B, B, B, B, B, B, B, }, // 带大括号指令名，如：#{else}
		/* 27.插参 */ { B, 27, B, B, B, B, P3, B, B, B, B, B, B, B, B, B, B, 27, B, }, // 无大括号插值参数，如：$aaa.bbb
		/* 28.函数 */ { 28, 28, 28, 28, 28, 28, P3, O3, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, }, // 小括号插值参数，如：$aaa.bbb((1 + 2) * (3 + 4))
		/* 29.连点 */ { B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, 28, B, }, // 点号连接插值参数，如：$aaa(arg1, arg2).bbb(arg1, arg2)
	};

	static int getCharType(char ch) {
		switch (ch) {
			case ' ': case '\t': case '\f': case '\b':
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
			case '\r': case '\n':
				return 16;
			case '.':
				return 17;
			default:
				return 18;
		}
	}

	private static DfaScanner scanner = new DfaScanner() {
		@Override
		public int next(int state, char ch) {
			return states[state][getCharType(ch)];
		}
	};

	private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$(\\w+)");

	public String filter(String key, String value) {
		try {
			StringBuilder buf = new StringBuilder();
			List<Token> tokens = scanner.scan(value);
			for (Token token : tokens) {
				String message = token.getMessage();
				if (message.length() > 1) {
					if (message.charAt(0) == '#') { // 指令
						boolean isDirective = false;
						if (message.charAt(1) >= 'a' && message.charAt(1) <= 'z') {
							// 将 #xxx($item) 转成 #xxx(item)
							message = REFERENCE_PATTERN.matcher(message).replaceAll("$1");
							isDirective = true;
						} else if (message.length() > 2 
								&& message.charAt(1) == '{' && message.charAt(message.length() - 1) == '}'
								&& message.charAt(2) >= 'a' && message.charAt(2) <= 'z') {
							// 将 #{else} 转成 #else()
							message = "#" + message.substring(2, message.length() - 1) + "()";
							isDirective = true;
						} else if (message.startsWith("#[[") && message.endsWith("]]#")) {
							// 将 #[[ ... ]]# 转成 #[ ... ]#
							message = "#[" + message.substring(3, message.length() - 3) + "]#";
						}
						if (isDirective) {
							int i = message.indexOf('(');
							String name = i < 0 ? message.substring(1) : message.substring(1, i);
							String expression = i < 0 ? "" : message.substring(i + 1, message.length() - 1).trim();
							if ("include".equals(name)) {
								// 将 #include("foo.txt") 转成 ${read("foo.txt")}
								message = "${read(" + expression + ")}";
							} else if ("parse".equals(name)) {
								// 将 #parse("foo.httl") 转成 ${include("foo.httl")}
								message = "${include(" + expression + ")}";
							} else if ("evaluate".equals(name)) {
								// 将 #evaluate("${name}") 转成 ${render("${name}")}
								message = "${render(" + expression + ")}";
							} else if ("foreach".equals(name)) {
								// 将#foreach(item in list) 转成 #for(item : list)
								message = "#for(" + expression + ")";
							} else if ("macro".equals(name)) {
								String[] args = expression.split("\\s+");
								StringBuilder sb = new StringBuilder();
								if (args.length > 1) {
									for (String arg : args) {
										if (sb.length() > 0) {
											sb.append(",");
										}
										sb.append("Object ");
										sb.append(arg);
									}
									// 将 #macro(name arg1 arg2) 转成 #macro(name(arg1, arg2))
									message = "#macro(" + args[0] + (sb.length() == 0 ? "" : "(" + sb + ")") + ")";
								}
							} else if ("define".equals(name)) {
								// 将 #define(name) 转成 #macro(name := _macro_name)
								message = "#macro(" + expression + " := _macro_" + expression + ")";
							} else if ("stop".equals(name)) {
								// 删除 #stop
								message = "";
							}
						}
					} else if (message.charAt(0) == '$') { // 插值
						if (message.charAt(1) >= 'a' && message.charAt(1) <= 'z'
								|| message.charAt(1) >= 'A' && message.charAt(1) <= 'Z') {
							// 将 $user.name 转成 ${user.name}
							message = "${" + message.substring(1) + "}";
						} else if (message.length() > 2 && message.charAt(1) == '!'
								&& (message.charAt(2) >= 'a' && message.charAt(2) <= 'z'
								|| message.charAt(2) >= 'A' && message.charAt(2) <= 'Z')) {
							// 将 $!user.name 转成 ${user.name}
							message = "${" + message.substring(1) + "}";
						} else if (message.length() > 3 && message.charAt(0) == '$' && message.charAt(1) == '!'
								&& message.charAt(2) == '{' && message.charAt(message.length() - 1) == '}') {
							// 将 $!{user.name} 转成 ${user.name}
							message = "$" + message.substring(2);
						}
					}
				}
				buf.append(message);
			}
			return buf.toString();
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}