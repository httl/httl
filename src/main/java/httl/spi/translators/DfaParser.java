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
package httl.spi.translators;

import httl.spi.Translator;
import httl.spi.sequences.StringSequence;
import httl.spi.translators.expressions.BinaryOperator;
import httl.spi.translators.expressions.Bracket;
import httl.spi.translators.expressions.Constant;
import httl.spi.translators.expressions.Node;
import httl.spi.translators.expressions.Operator;
import httl.spi.translators.expressions.UnaryOperator;
import httl.spi.translators.expressions.Variable;
import httl.internal.util.DfaScanner;
import httl.internal.util.LinkedStack;
import httl.internal.util.StringUtils;
import httl.internal.util.Token;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic Finite state Automata (DFA) Expression Parser (ThreadSafe)
 * 
 * @see httl.spi.translators.DefaultTranslator
 * 
 * @author Liang Fei (liangfei0201@gmail.com)
 */
public class DfaParser {

	//单字母命名, 保证状态机图简洁
	
	// BREAK，结束片段，包含当前字符
	private static final int B = DfaScanner.BREAK;

	// BACK_ONE，结束片段，退还当前字符
	private static final int B1 = DfaScanner.BREAK - 1;

	// BACK_TWO，结束片段，退还两个字符
	private static final int B2 = DfaScanner.BREAK - 2;

	// ERROR，解析出错
	private static final int E = DfaScanner.ERROR;

	// 表达式语法状态机图
	// 行表示状态
	// 行列交点表示, 在该状态时, 遇到某类型的字符时, 切换到的下一状态(数组行号)
	// E/B/T表示接收前面经过的字符为一个片断, R表示错误状态(这些状态均为负数)
	static final int states[][] = {
				  // 0.空格, 1.字母, 2.数字, 3.点号, 4.双引号, 5.单引号, 6.反单引号, 7.反斜线, 8.括号, 9.其它
		/* 0.起始  */ { 0, 1, 2, 5, 7, 9, 11, 4, 6, 4}, // 初始状态或上一片断刚接收完成状态
		/* 1.变量  */{ B1, 1, 1, B1, E, E, E, B1, B1, B1}, // 变量名识别
		/* 2.数字  */{ B1, 2, 2, 13, E, E, E, B1, B1, B1}, // 数字识别
		/* 3.小数  */{ B1, 3, 3, B1, E, E, E, B1, B1, B1}, // 小数点号识别
		/* 4.操作*/{ B1, B1, B1, 4, B1, B1, B1, 4, B1, 4}, // 操作符识别
		/* 5.点号  */{ B1, 1, 3, B, B1, B1, B1, B1, B1, 4}, // 属性点号
		/* 6.括号  */{ B1, B1, B1, B1, B1, B1, B1, B1, B1, B1}, // 括号
		/* 7.字符*/{ 7, 7, 7, 7, B, 7, 7, 8, 7, 7}, // 双引号字符串识别
		/* 8.转义  */{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7}, // 双引号字符串转义
		/* 9.字符*/{ 9, 9, 9, 9, 9, B, 9, 10, 9, 9}, // 单引号字符串识别
		/*10.转义  */{ 9, 9, 9, 9, 9, 9, 9, 9, 9, 9}, // 单引号字符串转义
		/*11.字符  */{ 11, 11, 11, 11, 11, 11, B, 11, 11, 11}, // 反单引号字符串识别
		/*12.转义  */{ 11, 11, 11, 11, 11, 11, 11, 11, 11, 11}, // 反单引号字符串转义
		/*13.数点  */{ B2, B2, 3, B2, B2, B2, B2, B2, B2, B2}, // 数字属性点号识别, 区分于小数点(如: 123.toString 或 11..15)
	};

	static int getCharType(char ch) {
		switch (ch) {
			case ' ': case '\t': case '\n': case '\r': case '\f': case '\b':
				return 0;
			case '_' :
			case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : 
			case 'h' : case 'i' : case 'j' : case 'k' : case 'l' : case 'm' : case 'n' : 
			case 'o' : case 'p' : case 'q' : case 'r' : case 's' : case 't' : 
			case 'u' : case 'v' : case 'w' : case 'x' : case 'y' : case 'z' :
			case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' : case 'G' : 
			case 'H' : case 'I' : case 'J' : case 'K' : case 'L' : case 'M' : case 'N' : 
			case 'O' : case 'P' : case 'Q' : case 'R' : case 'S' : case 'T' : 
			case 'U' : case 'V' : case 'W' : case 'X' : case 'Y' : case 'Z' :
				return 1;
			case '0' : case '1' : case '2' : case '3' : case '4' : 
			case '5' : case '6' : case '7' : case '8' : case '9' : 
				return 2;
			case '.' : 
				return 3;
			case '\"' : 
				return 4;
			case '\'' : 
				return 5;
			case '`' : 
				return 6;
			case '\\' : 
				return 7;
			case '(' : case ')' : case '[' : case ']' : 
				return 8;
			default:
				return 9;
		}
	}

	private static DfaScanner scanner = new DfaScanner() {
		@Override
		public int next(int state, char ch) {
			return states[state][getCharType(ch)];
		}
		
	};
	
	private static final Set<String> BINARY_OPERATORS = new HashSet<String>(Arrays.asList(new String[]{"+", "-", "*", "/", "%", "==", "!=", ">", ">=", "<", "<=", "gt", "ge", "lt", "le", "&&", "||", "&", "|", "^", ">>", "<<", ">>>", ",", "?", ":", "instanceof", "[", ".."}));
	
	private static final Set<String> UNARY_OPERATORS = new HashSet<String>(Arrays.asList(new String[]{"+", "-", "!", "~", "new", "["}));
	
	private static final Pattern BLANK_PATTERN = Pattern.compile("^(\\s+)");
	
	private final Translator translator;

	private final Map<String, Class<?>> parameterTypes;

	private final Class<?> defaultType;

	private final Collection<Class<?>> functions;

	private final List<StringSequence> sequences;

	private final String[] getters;

	private final String[] packages;
	
	private final String[] sizers;

	private final int offset;
	
	private final LinkedStack<Node> parameterStack = new LinkedStack<Node>();

	private final LinkedStack<Operator> operatorStack = new LinkedStack<Operator>();
	
	private final Map<Operator, Token> operatorTokens = new HashMap<Operator, Token>();

	public DfaParser(Translator translator, Map<String, Class<?>> parameterTypes, Class<?> defaultType, Collection<Class<?>> functions, List<StringSequence> sequences, String[] getters, String[] sizers, String[] packages, int offset) {
		this.translator = translator;
		this.parameterTypes = parameterTypes;
		this.defaultType = defaultType;
		this.functions = functions;
		this.sequences = sequences;
		this.getters = getters;
		this.packages = packages;
		this.sizers = sizers;
		this.offset = offset;
	}
	
	private int getTokenOffset(Token token) {
		int offset = token.getOffset();
		String msg = token.getMessage();
		Matcher matcher = BLANK_PATTERN.matcher(msg);
		if (matcher.find()) {
			return offset + matcher.group(1).length();
		}
		if (offset < 0) {
			offset = 0;
		}
		return offset;
	}
	
	private int getPriority(String operator, boolean unary) {
		int priority = 1000;
		if (unary && operator.startsWith("new ")) {
			return priority;
		}
		priority --;
		if (StringUtils.isFunction(operator) || operator.equals("[")) {
			return priority;
		}
		priority --;
		if (unary) {
			return priority;
		}
		priority --;
		if ("*".equals(operator)
				|| "/".equals(operator)
				|| "%".equals(operator)) {
			return priority;
		}
		priority --;
		if ("+".equals(operator)
				|| "-".equals(operator)) {
			return priority;
		}
		priority --;
		if (">>".equals(operator)
				|| "<<".equals(operator)
				|| ">>>".equals(operator)) {
			return priority;
		}
		priority --;
		if ("..".equals(operator)) {
			return priority;
		}
		priority --;
		if (">".equals(operator)
				|| "<".equals(operator)
				|| ">=".equals(operator)
				|| "<=".equals(operator)
				|| "instanceof".equals(operator)) {
			return priority;
		}
		priority --;
		if ("==".equals(operator)
				|| "!=".equals(operator)) {
			return priority;
		}
		priority --;
		if ("&".equals(operator)) {
			return priority;
		}
		priority --;
		if ("^".equals(operator)) {
			return priority;
		}
		priority --;
		if ("|".equals(operator)) {
			return priority;
		}
		priority --;
		if ("&&".equals(operator)) {
			return priority;
		}
		priority --;
		if ("||".equals(operator)) {
			return priority;
		}
		priority --;
		if ("?".equals(operator)
				|| ":".equals(operator)) {
			return priority;
		}
		priority --;
		if (",".equals(operator)) {
			return priority;
		}
		return priority;
	}
	
	public Node parse(String source, Set<String> variables) throws ParseException {
		List<Token> tokens = scanner.scan(source);
		boolean beforeOperator = true;
		for (int i = 0; i < tokens.size(); i ++) {
			Token token = tokens.get(i);
			String msg = token.getMessage().trim();
			if ("new".equals(msg)) {
				i ++;
				token = tokens.get(i);
				msg = token.getMessage().trim();
				try {
					msg = "new " + msg;
				} catch (Exception e) {
					throw new ParseException(e.getMessage(), token.getOffset());
				}
			} else if (! "null".equals(msg) && ! "true".equals(msg) && ! "false".equals(msg)
					&& StringUtils.isNamed(msg) && i < tokens.size() - 1) {
				String next = tokens.get(i + 1).getMessage().trim();
				if ("(".equals(next)) {
					if (parameterTypes.containsKey(msg)) {
						variables.add(msg);
					}
					msg = "." + msg;
				} else if (")".equals(next) && i > 0
						&& i < tokens.size() - 2) {
					String prev = tokens.get(i - 1).getMessage().trim();
					String after = tokens.get(i + 2).getMessage().trim();
					if ("(".equals(prev) && ("(".equals(after) || StringUtils.isNamed(after))) {
						Operator left = operatorStack.pop();
						if (left != Bracket.ROUND) {
							throw new ParseException("Miss left parenthesis", token.getOffset());
						}
						UnaryOperator operator = new UnaryOperator(translator, msg, getTokenOffset(token) + offset, parameterTypes, functions, sizers, packages, msg, getPriority(msg, true));
						operatorTokens.put(operator, token);
						operatorStack.push(operator);
						beforeOperator = true;
						i ++;
						continue;
					}
				}
			}
			// ================
			if (msg.length() >= 2 
					&& (msg.startsWith("\"") && msg.endsWith("\"") 
					|| msg.startsWith("\'") && msg.endsWith("\'") 
					|| msg.startsWith("`") && msg.endsWith("`"))) {
				if (msg.length() == 3 && msg.startsWith("`")) {
					char value = msg.charAt(1);
					parameterStack.push(new Constant(value, char.class, "\'" + value + "\'"));
				} else {
					String value = msg.substring(1, msg.length() - 1);
					parameterStack.push(new Constant(value, String.class, "\"" + value + "\""));
				}
				beforeOperator = false;
			} else if (StringUtils.isNumber(msg)) {
				Object value;
				Class<?> type;
				String literal;
				if (msg.endsWith("b") || msg.endsWith("B")) {
					value = Byte.valueOf(msg.substring(0, msg.length() - 1));
					type = byte.class;
					literal = String.valueOf(value);
				} else if (msg.endsWith("s") || msg.endsWith("S")) {
					value = Short.valueOf(msg.substring(0, msg.length() - 1));
					type = short.class;
					literal = String.valueOf(value);
				} else if (msg.endsWith("i") || msg.endsWith("I")) {
					value = Integer.valueOf(msg.substring(0, msg.length() - 1));
					type = int.class;
					literal = String.valueOf(value);
				} else if (msg.endsWith("l") || msg.endsWith("L")) {
					value = Long.valueOf(msg.substring(0, msg.length() - 1));
					type = long.class;
					literal = String.valueOf(value) + "l";
				} else if (msg.endsWith("f") || msg.endsWith("F")) {
					value = Float.valueOf(msg.substring(0, msg.length() - 1));
					type = float.class;
					literal = String.valueOf(value) + "f";
				} else if (msg.endsWith("d") || msg.endsWith("D")) {
					value = Double.valueOf(msg.substring(0, msg.length() - 1));
					type = double.class;
					literal = String.valueOf(value) + "d";
				} else if (msg.indexOf('.') >= 0) {
					value = Double.valueOf(msg);
					type = double.class;
					literal = String.valueOf(value);
				} else {
					value = Integer.valueOf(msg);
					type = int.class;
					literal = String.valueOf(value);
				}
				parameterStack.push(new Constant(value, type, literal));
				beforeOperator = false;
			} else if ("null".equals(msg)) {
				parameterStack.push(Constant.NULL);
				beforeOperator = false;
			} else if ("true".equals(msg) || "false".equals(msg)) {
				parameterStack.push(Boolean.parseBoolean(msg) ? Constant.TRUE : Constant.FALSE);
				beforeOperator = false;
			} else if (StringUtils.isNamed(msg)
					&& ! "gt".equals(msg) && ! "ge".equals(msg) 
					&& ! "lt".equals(msg) && ! "le".equals(msg)) {
				if (defaultType == null && ! parameterTypes.containsKey(msg)) {
					throw new ParseException("Undefined variable \"" + msg + "\". \nPlease add variable type definition #set(Xxx " + msg + ") in your template.", getTokenOffset(token) + offset);
				}
				variables.add(msg);
				parameterStack.push(new Variable(translator, msg, getTokenOffset(token) + offset, parameterTypes, defaultType));
				beforeOperator = false;
			} else if ("(".equals(msg)) {
				operatorStack.push(Bracket.ROUND);
				beforeOperator = true;
			} else if (")".equals(msg)) {
				while (popOperator() != Bracket.ROUND);
				beforeOperator = false;
			} else if ("]".equals(msg)) {
				while (popOperator() != Bracket.SQUARE);
				beforeOperator = false;
			} else {
				if (beforeOperator) {
					if (! msg.startsWith("new ") && ! StringUtils.isFunction(msg) && ! UNARY_OPERATORS.contains(msg)) {
						throw new ParseException("Unsupported binary operator " + msg, getTokenOffset(token) + offset);
					}
					UnaryOperator operator = new UnaryOperator(translator, msg, getTokenOffset(token) + offset, parameterTypes, functions, sizers, packages, msg, getPriority(msg, true));
					operatorTokens.put(operator, token);
					operatorStack.push(operator);
				} else {
					if (! StringUtils.isFunction(msg) && ! BINARY_OPERATORS.contains(msg)) {
						throw new ParseException("Unsupported binary operator " + msg, getTokenOffset(token) + offset);
					}
					BinaryOperator operator = new BinaryOperator(translator, msg, getTokenOffset(token) + offset, parameterTypes, functions, sequences, getters, sizers, packages, msg, getPriority(msg, false));
					operatorTokens.put(operator, token);
					while (! operatorStack.isEmpty() && ! (operatorStack.peek() instanceof Bracket)
							&& operatorStack.peek().getPriority() >= operator.getPriority()) {
						popOperator();
					}
					operatorStack.push(operator);
				}
				if ("[".equals(msg)) {
					operatorStack.push(Bracket.SQUARE);
				}
				beforeOperator = true;
				// 给无参函数自动补上null参数
				if (msg.startsWith("new ") || StringUtils.isFunction(msg)) {
					boolean miss = i == tokens.size() - 1 || ! "(".equals(tokens.get(i + 1).getMessage().trim());
					boolean empty = i < tokens.size() - 2 && "(".equals(tokens.get(i + 1).getMessage().trim()) && ")".equals(tokens.get(i + 2).getMessage().trim());
					if (miss || empty) {
						parameterStack.push(Constant.EMPTY);
						beforeOperator = false;
					}
					if (empty) {
						i = i + 2;
					}
				}
			}
		}
		while (! operatorStack.isEmpty()) {
			Operator operator = popOperator();
			if (operator == Bracket.ROUND || operator == Bracket.SQUARE) {
				throw new ParseException("Miss right parenthesis", offset);
			}
		}
		Node result = parameterStack.pop();
		if (! parameterStack.isEmpty())
			throw new ParseException("Operator miss parameter", offset);
		return result;
	}

	private Operator popOperator() throws ParseException {
		if (operatorStack.isEmpty())
			throw new ParseException("Miss left parenthesis", offset);
		Operator operator = operatorStack.pop(); // 将优先级高于及等于当前操作符的弹出
		if (operator instanceof BinaryOperator) {
			Token token = operatorTokens.get(operator);
			BinaryOperator binaryOperator = (BinaryOperator) operator;
			if (parameterStack.isEmpty())
				throw new ParseException("Binary operator " + binaryOperator.getName() + " miss parameter", token == null ? offset : getTokenOffset(token) + offset);
			binaryOperator.setRightParameter(parameterStack.pop()); // right first
			if (parameterStack.isEmpty())
				throw new ParseException("Binary operator " + binaryOperator.getName() + " miss parameter", token == null ? offset : getTokenOffset(token) + offset);
			binaryOperator.setLeftParameter(parameterStack.pop());
			parameterStack.push(operator);
		} else if (operator instanceof UnaryOperator) {
			Token token = operatorTokens.get(operator);
			UnaryOperator unaryOperator = (UnaryOperator) operator;
			if (parameterStack.isEmpty())
				throw new ParseException("Unary operator " + unaryOperator.getName() + "miss parameter", token == null ? offset : getTokenOffset(token) + offset);
			unaryOperator.setParameter(parameterStack.pop());
			parameterStack.push(operator);
		}
		return operator;
	}

}