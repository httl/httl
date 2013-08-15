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

import httl.Engine;
import httl.Node;
import httl.Template;
import httl.ast.BlockDirective;
import httl.ast.BreakDirective;
import httl.ast.Comment;
import httl.ast.Directive;
import httl.ast.ElseDirective;
import httl.ast.EndDirective;
import httl.ast.Expression;
import httl.ast.ForDirective;
import httl.ast.IfDirective;
import httl.ast.MacroDirective;
import httl.ast.RootDirective;
import httl.ast.SetDirective;
import httl.ast.Statement;
import httl.ast.Text;
import httl.ast.ValueDirective;
import httl.spi.Parser;
import httl.util.ClassUtils;
import httl.util.DfaScanner;
import httl.util.LinkedStack;
import httl.util.ParameterizedTypeImpl;
import httl.util.StringUtils;
import httl.util.Token;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TemplateParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setTemplateParser(Parser)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class TemplateParser implements Parser {

	// 单字母命名, 保证状态机图简洁

	// END，结束片段，包含当前字符
	private static final int E = DfaScanner.BREAK;

	// BREAK，结束片段，并退回一个字符，即不包含当前字符
	private static final int B = DfaScanner.BREAK - 1;

	// BACKSPACE，结束片段，退回当前字符，以及之前的所有空白
	private static final int S = DfaScanner.BACKSPACE - 1;

	// PUSH，压栈1，即指令小括号栈，并回到状态4，即指令参数
	private static final int P = DfaScanner.PUSH - 4;

	// POP，弹栈1，即指令小括号栈，并回到状态4，即指令参数
	private static final int O = DfaScanner.POP - 4;

	// PUSH，压栈2，即插值大括号栈，并回到状态7，即插值参数
	private static final int P2 = DfaScanner.PUSH - 7;

	// POP，弹栈2，即插值大括号栈，并回到状态7，即插值参数
	private static final int O2 = DfaScanner.POP - 7;

	// 插值语法状态机图
	// 行表示状态
	// 行列交点表示, 在该状态时, 遇到某类型的字符时, 切换到的下一状态(数组行号)
	// E/B/T表示接收前面经过的字符为一个片断, R表示错误状态(这些状态均为负数)
	static final int states[][] = {
				  // 0.\s, 1.a-z, 2.#, 3.$, 4.!, 5.*, 6.(, 7.), 8.[, 9.], 10.{, 11.}, 12.", 13.', 14.`, 15.\, 16.\r\n, 17.其它
		/* 0.起始 */ { 1, 1, 2, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, }, // 初始状态或上一片断刚接收完成状态
		/* 1.文本 */ { 1, 1, B, B, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, }, // 非指令文本内容
		
		/* 2.指令 */ { 1, 3, 9, B, 6, 10, 1, 1, 12, 1, P2, 1, 1, 1, 1, 1, 1, 1, }, // 指令提示符
		/* 3.指名 */ { 26, 3, B, B, B, B, P, B, B, B, B, B, B, B, B, B, B, B, }, // 指令名
		/* 4.指参 */ { 4, 4, 4, 4, 4, 4, P, O, 4, 4, 4, 4, 14, 16, 18, 4, 4, 4, }, // 指令参数
		
		/* 5.插值 */ { 1, 1, B, B, 6, 1, 1, 1, 1, 1, P2, 1, 1, 1, 1, 1, 1, 1, }, // 插值提示符
		/* 6.非滤 */ { 1, 1, B, B, 1, 1, 1, 1, 1, 1, P2, 1, 1, 1, 1, 1, 1, 1, }, // 非过滤插值
		/* 7.插参 */ { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, P2, O2, 20, 22, 24, 7, 7, 7, }, // 插值参数
		
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
		
		/* 26.指间空白 */ { 26, S, S, S, S, S, P, S, S, S, S, S, S, S, S, S, S, S, }, // 指令名和括号间的空白
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

	private boolean isDirective(String message) {
		if (message.length() > 1 && message.charAt(0) == '#'
				&& message.charAt(1) >= 'a' && message.charAt(1) <= 'z') {
			int i = message.indexOf('(');
			String name = (i > 0 ? message.substring(1, i) : message.substring(1));
 			return isDirectiveName(name);
		}
		return false;
	}

	private boolean isDirectiveName(String name) {
		return StringUtils.inArray(name, setDirective)
				|| StringUtils.inArray(name, ifDirective) || StringUtils.inArray(name, elseDirective) 
				|| StringUtils.inArray(name, forDirective) || StringUtils.inArray(name, breakDirective) 
				|| StringUtils.inArray(name, macroDirective) || StringUtils.inArray(name, endDirective);
	}
	
	private void defineVariableTypes(String value, int offset, List<Statement> directives) throws IOException, ParseException {
		int o = offset;
		for (String v : splitDefine(value)) {
			v = v.trim().replaceAll("\\s", " ");
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
			directives.add(new SetDirective(parseGenericType(type, o), var, null, false, false, offset));
			o += v.length() + 1;
		}
	}
	
	private boolean isNoLiteralText(Statement node) {
		return node instanceof Text && ! ((Text) node).isLiteral();
	}

	private List<Statement> clean(List<Statement> nodes) throws ParseException, IOException {
		List<Statement> result = null;
		for (int i = 0; i < nodes.size(); i ++) {
			if (i + 1 < nodes.size() && isNoLiteralText(nodes.get(i)) && isNoLiteralText(nodes.get(i + 1))) {
				if (result == null) {
					result = new ArrayList<Statement>();
					for (int j = 0; j < i; j ++) {
						result.add(nodes.get(j));
					}
				}
				int offset = nodes.get(i).getOffset();
				StringBuilder buf = new StringBuilder();
				buf.append(((Text) nodes.get(i)).getContent());
				while (i + 1 < nodes.size() && isNoLiteralText(nodes.get(i + 1))) {
					buf.append(((Text) nodes.get(i + 1)).getContent());
					i ++;
				}
				result.add(new Text(buf.toString(), false, offset));
			} else if (result != null) {
				result.add(nodes.get(i));
			}
		}
		if (result != null) {
			return result;
		}
		return nodes;
	}

	private List<Statement> scan(String source, int sourceOffset) throws ParseException, IOException {
		List<Statement> directives = new ArrayList<Statement>();
		List<Token> tokens = scanner.scan(source, sourceOffset);
		AtomicInteger seq = new AtomicInteger();
		for (int t = 0; t < tokens.size(); t ++) {
			Token token = tokens.get(t);
			String message = token.getMessage();
			int offset = token.getOffset();
			if (isDirective(message)) {
				int s = message.indexOf('(');
				String name;
				String value;
				int exprOffset;
				if (s > 0) {
					exprOffset = offset + s + 1;
					name = message.substring(1, s);
					if (! message.endsWith(")")) {
						throw new ParseException("The #" + name + " directive mismatch right parentheses.", exprOffset);
					}
					value = message.substring(s + 1, message.length() - 1);
				} else {
					exprOffset = token.getOffset() + message.length();
					name = message.substring(1);
					value = "";
					if (! StringUtils.inArray(name, elseDirective)
							&& ! StringUtils.inArray(name, endDirective)
							&& ! StringUtils.inArray(name, breakDirective)) {
						throw new ParseException("Not found parameter expression in the #" + name + " directive.", offset);
					}
				}
				if (StringUtils.inArray(name, setDirective)) {
					if (value.contains("=")) {
						int o = 0;
						for (String v : splitAssign(value)) {
							int i = v.indexOf('=');
							String var = v.substring(0, i).trim();
							String expr = v.substring(i + 1);
							int blank = 0;
							while (blank < expr.length()) {
								if (! Character.isWhitespace(expr.charAt(blank))) {
									break;
								}
								blank ++;
							}
							if (blank > 0) {
								expr = expr.substring(blank);
							}
							Expression expression = (Expression) expressionParser.parse(expr, exprOffset + i + 1 + blank + o);
							boolean export = false;
							boolean hide = false;
							if (var.endsWith(":")) {
								export = true;
								var = var.substring(0, var.length() - 1).trim();
							} else if (var.endsWith(".")) {
								hide = true;
								var = var.substring(0, var.length() - 1).trim();
							}
							int j = var.lastIndexOf(' ');
							String type = null;
							if (j > 0) {
								type = var.substring(0, j).trim();
								var = var.substring(j + 1).trim();
							}
							directives.add(new SetDirective(parseGenericType(type, exprOffset), var, expression, export, hide, offset));
							o += v.length() + 1;
						}
					} else {
						defineVariableTypes(value, offset, directives);
					}
				} else if (StringUtils.inArray(name, forDirective)) {
					if (StringUtils.isNumber(value.trim())) {
						value = "__for" + seq.incrementAndGet() + " : 1 .. " + value.trim();
					}
					int i = value.indexOf(" in ");
					int n = 4;
					if (i < 0) {
						i = value.indexOf(':');
						n = 1;
					}
					if (i < 0) {
						throw new ParseException("Miss colon \":\" in invalid directive #for(" + value + ")", offset);
					}
					String var = value.substring(0, i).trim();
					String expr = value.substring(i + n);
					int blank = 0;
					while (blank < expr.length()) {
						if (! Character.isWhitespace(expr.charAt(blank))) {
							break;
						}
						blank ++;
					}
					if (blank > 0) {
						expr = expr.substring(blank);
					}
					Expression expression = (Expression) expressionParser.parse(expr, exprOffset + i + n + blank);
					int j = var.lastIndexOf(' ');
					String type = null;
					if (j > 0) {
						type = var.substring(0, j).trim();
						var = var.substring(j + 1).trim();
					}
					directives.add(new ForDirective(parseGenericType(type, exprOffset), var, expression, offset));
				} else if (StringUtils.inArray(name, ifDirective)) {
					directives.add(new IfDirective((Expression) expressionParser.parse(value, exprOffset), offset));
				} else if (StringUtils.inArray(name, elseDirective)) {
					directives.add(new ElseDirective(StringUtils.isEmpty(value)
							? null : (Expression) expressionParser.parse(value, exprOffset), offset));
				} else if (StringUtils.inArray(name, breakDirective)) {
					directives.add(new BreakDirective(StringUtils.isBlank(value) ? null : (Expression) expressionParser.parse(value, exprOffset), offset));
				} else if (StringUtils.inArray(name, macroDirective)) {
					String macroName = value;
					String macroParams = null;
					String filter = null;
					int idx = macroName.indexOf("=>");
					if (idx > 0) {
						filter = macroName.substring(idx + 2).trim();
						macroName = macroName.substring(0, idx);
					}
					int i = value.indexOf('(');
					if (i > 0) {
						if (! message.endsWith(")")) {
							throw new ParseException("The #" + name + " directive mismatch right parentheses.", exprOffset);
						}
						macroName = value.substring(0, i);
						macroParams = value.substring(i + 1, value.length() - 1);
					}
					String set = null;
					boolean parent = false;
					boolean hide = false;
					i = macroName.indexOf('=');
					if (i > 0) {
						set = macroName.substring(0, i);
						if (set.endsWith(":")) {
							parent = true;
							set = set.substring(0, set.length() - 1);
						} else if (set.endsWith(".")) {
							hide = true;
							set = set.substring(0, set.length() - 1);
						}
						set = set.trim();
						macroName = macroName.substring(i + 1);
					}
					boolean out = false;
					if (macroName.startsWith("$")) {
						out = true;
						macroName = macroName.substring(macroName.startsWith("$!") ? 2 : 1);
					}
					String expr;
					if (StringUtils.isNotEmpty(filter)) {
						if (filter.contains("(")) {
							expr = filter;
						} else {
							expr = filter + "(" + macroName + ")";
						}
					} else {
						expr = macroName;
					}
					if (StringUtils.isNotEmpty(set)) {
						directives.add(new SetDirective(Template.class, set, (Expression) expressionParser.parse(expr, exprOffset), parent, hide, offset));
					}
					if (out) {
						directives.add(new ValueDirective((Expression) expressionParser.parse(expr, exprOffset), true, offset));
					}
					macroName = macroName.trim();
					directives.add(new MacroDirective(macroName, offset));
					if (StringUtils.isNotEmpty(macroParams)) {
						defineVariableTypes(macroParams, exprOffset, directives);
					}
				} else if (StringUtils.inArray(name, endDirective)) {
					directives.add(new EndDirective(offset));
				}
			} else if (message.endsWith("}") && (message.startsWith("${") || message.startsWith("$!{")
					|| message.startsWith("#{") || message.startsWith("#!{"))) {
				int i = message.indexOf('{');
				directives.add(new ValueDirective((Expression) expressionParser.parse(message.substring(i + 1, message.length() - 1), 
						offset + i + 1), message.startsWith("$!") || message.startsWith("#!"), offset));
			} else if (message.startsWith("##")) {
				directives.add(new Comment(message.substring(2), false, offset));
			} else if ((message.startsWith("#*") && message.endsWith("*#"))) {
				directives.add(new Comment(message.substring(2, message.length() - 2), true, offset));
			} else {
				boolean literal;
				if (message.startsWith("#[") && message.endsWith("]#")) {
					message = message.substring(2, message.length() - 2);
					literal = true;
				} else {
					message = filterEscape(message);
					literal = false;
				}
				directives.add(new Text(message, literal, offset));
			}
		}
		return directives;
	}

	private BlockDirective reduce(List<Statement> directives) throws ParseException {
		LinkedStack<BlockDirectiveEntry> directiveStack = new LinkedStack<BlockDirectiveEntry>();
		RootDirective rootDirective = new RootDirective();
		directiveStack.push(new BlockDirectiveEntry(rootDirective));
		for (int i = 0, n = directives.size(); i < n; i ++) {
			Statement directive = (Statement)directives.get(i);
			if (directive == null)
				continue;
			Class<?> directiveClass = directive.getClass();
			// 弹栈
			if (directiveClass == EndDirective.class
					|| directiveClass == ElseDirective.class) {
				if (directiveStack.isEmpty())
					throw new ParseException("Miss #end directive.", directive.getOffset());
				BlockDirective blockDirective = ((BlockDirectiveEntry) directiveStack.pop()).popDirective();
				if (blockDirective == rootDirective)
					throw new ParseException("Miss #end directive.", directive.getOffset());
				EndDirective endDirective;
				if (directiveClass == ElseDirective.class) {
					endDirective = new EndDirective(directive.getOffset());
				} else {
					endDirective = (EndDirective) directive;
				}
				blockDirective.setEnd(endDirective);
			}
			// 设置树
			if (directiveClass != EndDirective.class) { // 排除EndDirective
				if (directiveStack.isEmpty())
					throw new ParseException("Miss #end directive.", directive.getOffset());
				((BlockDirectiveEntry) directiveStack.peek()).appendInnerDirective(directive);
			}
			// 压栈
			if (directive instanceof BlockDirective)
				directiveStack.push(new BlockDirectiveEntry((BlockDirective) directive));
		}
		BlockDirective root = (BlockDirective) ((BlockDirectiveEntry) directiveStack.pop()).popDirective();
		if (! directiveStack.isEmpty()) { // 后验条件
			throw new ParseException("Miss #end directive." + root.getClass().getSimpleName(), root.getOffset());
		}
		return (BlockDirective) root;
	}

	// 指令归约辅助封装类
	private static final class BlockDirectiveEntry {

		private BlockDirective blockDirective;

		private List<Node> elements = new ArrayList<Node>();

		BlockDirectiveEntry(BlockDirective blockDirective) {
			this.blockDirective = blockDirective;
		}

		void appendInnerDirective(Statement innerDirective) {
			this.elements.add(innerDirective);
		}

		BlockDirective popDirective() throws ParseException {
			((BlockDirective)blockDirective).setChildren(elements);
			return blockDirective;
		}

	}
	
	private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\\\+[#$]");

	private String[] setDirective = new String[] { "var" };

	private String[] ifDirective = new String[] { "if" };

	private String[] elseDirective = new String[] { "else" };

	private String[] forDirective = new String[] { "for" };

	private String[] breakDirective = new String[] { "break" };

	private String[] macroDirective = new String[] { "macro" };

	private String[] endDirective = new String[] { "end" };

	private Engine engine;
	
	private Parser expressionParser;
	
	private String[] importMacros;
   
	private final Map<String, Template> importMacroTemplates = new ConcurrentHashMap<String, Template>();

	private String[] importPackages;

	private String[] importVariables;

	private Map<String, Class<?>> importTypes;

	private final Map<Class<?>, Object> functions = new ConcurrentHashMap<Class<?>, Object>();

	private Class<?> defaultVariableType;

	private boolean removeDirectiveBlankLine = true;

	/**
	 * httl.properties: remove.directive.blank.line=true
	 */
	public void setRemoveDirectiveBlankLine(boolean removeDirectiveBlankLine) {
		this.removeDirectiveBlankLine = removeDirectiveBlankLine;
	}

	/**
	 * httl.properties: set.directive=set
	 */
	public void setSetDirective(String[] setDirective) {
		this.setDirective = setDirective;
	}

	/**
	 * httl.properties: if.directive=if
	 */
	public void setIfDirective(String[] ifDirective) {
		this.ifDirective = ifDirective;
	}

	/**
	 * httl.properties: else.directive=else
	 */
	public void setElseDirective(String[] elseDirective) {
		this.elseDirective = elseDirective;
	}

	/**
	 * httl.properties: for.directive=for
	 */
	public void setForDirective(String[] forDirective) {
		this.forDirective = forDirective;
	}

	/**
	 * httl.properties: break.directive=break
	 */
	public void setBreakDirective(String[] breakDirective) {
		this.breakDirective = breakDirective;
	}

	/**
	 * httl.properties: macro.directive=macro
	 */
	public void setMacroDirective(String[] macroDirective) {
		this.macroDirective = macroDirective;
	}

	/**
	 * httl.properties: end.directive=end
	 */
	public void setEndDirective(String[] endDirective) {
		this.endDirective = endDirective;
	}

	/**
	 * httl.properties: default.variable.type=java.lang.String
	 */
	public void setDefaultVariableType(String defaultVariableType) {
		this.defaultVariableType = ClassUtils.forName(defaultVariableType);
	}

	/**
	 * httl.properties: import.macros=common.httl
	 */
	public void setImportMacros(String[] importMacros) {
		this.importMacros = importMacros;
	}

	/**
	 * httl.properties: engine=httl.spi.engines.DefaultEngine
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/**
	 * httl.properties: expression.parser=httl.spi.parsers.ExpressionParser
	 */
	public void setExpressionParser(Parser expressionParser) {
		this.expressionParser = expressionParser;
	}

	/**
	 * httl.properties: import.packages=java.util
	 */
	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
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

	public Node parse(String source, int offset) throws IOException, ParseException {
		return reduce(trim(clean(scan(source, offset))));
	}

	/**
	 * 如果指令所在的行没有其他内容，那么将两边的空白内容删除。
	 * 
	 * @author subchen@gmail.com
	 */
	private List<Statement> trim(List<Statement> nodes) throws ParseException, IOException {
		if (! removeDirectiveBlankLine) {
			return nodes;
		}
		
		// 待移除的空的 Text Node (主要是 Text Node 不允许为空的内容，只能在循环外把它delete了)
		List<Integer> empty_text_index_list = new ArrayList<Integer>();

		for (int i = 0; i < nodes.size(); i++) {
			Statement node = nodes.get(i);

			if (isTrimableDirective(node)) {
				if (i > 0) {
					int prev_index = i - 1;
					Statement prev = nodes.get(prev_index);

					if (isNoLiteralText(prev)
							&& !empty_text_index_list.contains(prev_index)) {
						// 删除上一个文本节点最后一个\n之后的所有空白符
						String text = ((Text) prev).getContent();
						int pos = text.lastIndexOf('\n');
						if (pos >= 0) {
							String tail = text.substring(pos + 1);
							if (tail.length() > 0 && tail.trim().length() == 0) {
								text = text.substring(0, pos + 1);
								if (text.length() == 0) {
									empty_text_index_list.add(prev_index); // 将会是空节点，加入到待删除队列
								} else {
									nodes.set(prev_index, new Text(text, false,
											prev.getOffset())); // 修改
								}
							}
						}
					}
				} // prev text node

				if (i + 1 < nodes.size()) {
					int next_index = i + 1;
					Statement next = nodes.get(next_index);

					if (isNoLiteralText(next)) {
						// 删除下一个文本节点地一个\n之前的所有空白符
						String text = ((Text) next).getContent();
						int pos = text.indexOf('\n');
						if (pos >= 0) {
							String head = text.substring(0, pos);
							if (head.trim().length() == 0) {
								text = text.substring(pos + 1);
								boolean isEmptyNode = false;
								if (text.length() == 0) {
									empty_text_index_list.add(next_index); // 将会是空节点，加入到待删除队列
									isEmptyNode = true;
								} else if (text.indexOf('\n') == -1
										&& text.trim().length() == 0) {
									// 看看下面是不是还是个指令，是不是可以全部丢掉
									if (next_index + 1 < nodes.size()) {
										Statement next_next = nodes
												.get(next_index + 1);
										if (isTrimableDirective(next_next)) {
											empty_text_index_list
													.add(next_index); // 将会是空节点，加入到待删除队列
											isEmptyNode = true;
										}
									}
								}
								if (!isEmptyNode) {
									nodes.set(next_index, new Text(text, false,
											next.getOffset())); // 修改
								}
							}
						}
						i++; // skip next
					}
				} // next text node
			} // not Directive
		}

		// 删除需要删掉的空节点。
		if (empty_text_index_list.size() > 0) {
			// 必须先删除后面的node
			for (int i = empty_text_index_list.size() - 1; i >= 0; i--) {
				int index = empty_text_index_list.get(i); // 必须转成小 int,
															// 否则就是删除对象，不是删除指定的index
				nodes.remove(index);
			}
		}
		return nodes;
	}

	private boolean isTrimableDirective(Statement node) {
	    if (node instanceof Directive) {
	        return !(node instanceof ValueDirective);
	    }
	    return false;
	}

	private String filterEscape(String source) {
		StringBuffer buf = new StringBuffer();
		Matcher matcher = ESCAPE_PATTERN.matcher(source + "#"); // 预加#号简化正则表达式
		while(matcher.find()) {
			String escape = matcher.group();
			String slash = escape.substring(0, escape.length() - 1);
			String symbol = escape.substring(escape.length() - 1);
			int length = slash.length();
			int half = (length - length % 2) / 2;
			matcher.appendReplacement(buf, Matcher.quoteReplacement(slash.substring(0, half) + symbol));
		}
		matcher.appendTail(buf);
		return buf.toString().substring(0, buf.length() - 1); // 减掉预加的#号
	}

	private Type parseGenericType(String type, int offset) throws IOException, ParseException {
		if (StringUtils.isBlank(type)) {
			return null;
		}
		int i = type.indexOf('<');
		if (i < 0) {
			try {
				return ClassUtils.forName(importPackages, type);
			} catch (Exception e) {
				throw new ParseException("No such class " + type + ", cause: " + ClassUtils.dumpException(e), offset);
			}
		}
		if (! type.endsWith(">")) {
			throw new ParseException("Illegal type: " + type, offset);
		}
		Class<?> raw;
		try {
			raw = ClassUtils.forName(importPackages, type.substring(0, i));
		} catch (Exception e) {
			throw new ParseException("No such class " + type.substring(0, i) + ", cause: " + ClassUtils.dumpException(e), offset);
		}
		String parameterType = type.substring(i + 1, type.length() - 1).trim();
		offset = offset + 1;
		List<String> genericTypes = new ArrayList<String>();
		List<Integer> genericOffsets = new ArrayList<Integer>();
		parseGenericTypeString(parameterType, offset, genericTypes, genericOffsets);
		if (genericTypes != null && genericTypes.size() > 0) {
			Type[] types = new Type[genericTypes.size()];
			for (int k = 0; k < genericTypes.size(); k ++) {
				types[k] = parseGenericType(genericTypes.get(k), genericOffsets.get(k));
			}
			return new ParameterizedTypeImpl(raw, types);
		}
		return raw;
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
	
	private static boolean isEndString(String value) {
		int sc = 0;
		int dc = 0;
		for (int i = 0; i < value.length(); i ++) {
			char ch = value.charAt(i);
			if (ch == '\'' && dc % 2 == 0 
					|| ch == '\"' && sc % 2 == 0) {
				int c = 0;
				for (int j = i - 1; j >= 0; j--) {
					if (value.charAt(j) == '\\') {
						c++;
					} else {
						break;
					}
				}
				if (c % 2 == 0) {
					if (ch == '\'') {
						sc ++;
					} else {
						dc ++;
					}
				}
			}
		}
		return sc % 2 == 0 && dc % 2 == 0;
	}

	static List<String> splitAssign(String value) {
		List<String> list = new ArrayList<String>();
		int i = value.indexOf('=');
		while ((i = value.indexOf('=', i + 1)) > 0) {
			if (i + 1 < value.length() && value.charAt(i + 1) == '=') {
				i ++;
			} else if (value.charAt(i - 1) != '>'
					&& value.charAt(i - 1) != '<'
					&& value.charAt(i - 1) != '!'
					&& isEndString(value.substring(0, i - 1))) {
				String sub = value.substring(0, i);
				int j = sub.lastIndexOf(',');
				int k = sub.lastIndexOf('>');
				if (j > 0 && j < k) {
					int g = 0;
					for (int n = k; n >= 0; n --) {
						char c = sub.charAt(n);
						if (c == '>') {
							g ++;
						} else if (c == '<') {
							g --;
						}
						if (g == 0) {
							sub = sub.substring(0, n);
							j = sub.lastIndexOf(',');
							break;
						}
					}
				}
				if (j > 0) {
					list.add(value.substring(0, j));
					value = value.substring(j + 1);
					i = i - j - 1;
				} else {
					break;
				}
			}
		}
		list.add(value);
		return list;
	}

	private static final Pattern DEFINE_PATTERN = Pattern.compile("([\\w>\\]]\\s+\\w+)\\s*[,]?");

	static List<String> splitDefine(String value) {
		List<String> vs = new ArrayList<String>();
		Matcher matcher = DEFINE_PATTERN.matcher(value);
		while (matcher.find()) {
			StringBuffer rep = new StringBuffer();
			matcher.appendReplacement(rep, "$1");
			String v = rep.toString();
			if (v.contains(",")) {
				if (! v.contains("<")) {
					vs.addAll(Arrays.asList(v.split(",")));
				} else if (v.indexOf(',') < v.indexOf('<')) {
					int j = v.indexOf('<');
					int i = v.substring(0, j).lastIndexOf(',');
					vs.addAll(Arrays.asList(v.substring(0, i).split(",")));
					vs.add(v.substring(i + 1));
				} else {
					vs.add(v);
				}
			} else {
				vs.add(v);
			}
		}
		if (vs.size() == 0) {
			vs = Arrays.asList(value.split(","));
		} else {
			StringBuffer tail = new StringBuffer();
			matcher.appendTail(tail);
			if (tail.toString().trim().length() > 0) {
				vs.add(tail.toString());
			}
		}
		return vs;
	}
	
}