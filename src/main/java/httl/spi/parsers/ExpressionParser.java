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

import httl.Node;
import httl.ast.AddOperator;
import httl.ast.AndOperator;
import httl.ast.ArrayOperator;
import httl.ast.BinaryOperator;
import httl.ast.BitAndOperator;
import httl.ast.BitNotOperator;
import httl.ast.BitOrOperator;
import httl.ast.BitXorOperator;
import httl.ast.CastOperator;
import httl.ast.ConditionOperator;
import httl.ast.Constant;
import httl.ast.DivOperator;
import httl.ast.EntryOperator;
import httl.ast.EqualsOperator;
import httl.ast.Expression;
import httl.ast.GreaterEqualsOperator;
import httl.ast.GreaterOperator;
import httl.ast.IndexOperator;
import httl.ast.InstanceofOperator;
import httl.ast.LeftShiftOperator;
import httl.ast.LessEqualsOperator;
import httl.ast.LessOperator;
import httl.ast.ListOperator;
import httl.ast.MethodOperator;
import httl.ast.ModOperator;
import httl.ast.MulOperator;
import httl.ast.NegativeOperator;
import httl.ast.NewOperator;
import httl.ast.NotEqualsOperator;
import httl.ast.NotOperator;
import httl.ast.Operator;
import httl.ast.OrOperator;
import httl.ast.PositiveOperator;
import httl.ast.RightShiftOperator;
import httl.ast.SequenceOperator;
import httl.ast.StaticMethodOperator;
import httl.ast.SubOperator;
import httl.ast.UnaryOperator;
import httl.ast.UnsignShiftOperator;
import httl.ast.Variable;
import httl.internal.util.ClassUtils;
import httl.internal.util.DfaScanner;
import httl.internal.util.LinkedStack;
import httl.internal.util.StringUtils;
import httl.internal.util.Token;
import httl.spi.Filter;
import httl.spi.Parser;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExpressionParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.parsers.TemplateParser#setExpressionParser(Parser)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ExpressionParser implements Parser {

	private Filter expressionFilter;

	private String[] forbidMethods;

	private String[] importPackages;

	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	/**
	 * httl.properties: expression.filters=httl.spi.filters.UnescapeXmlFilter
	 */
	public void setExpressionFilter(Filter expressionFilter) {
		this.expressionFilter = expressionFilter;
	}

	/**
	 * httl.properties: import.getters=forbid.methods=add,put,save,insert,modify,update,delete,remove,clear
	 */
	public void setForbidMethods(String[] forbidMethods) {
		this.forbidMethods = forbidMethods;
	}

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
	
	private static final Set<String> BINARY_OPERATORS = new HashSet<String>(Arrays.asList(new String[]{"+", "-", "*", "/", "%", "==", "!=", ">", ">=", "<", "<=", "gt", "ge", "lt", "le", "&&", "||", "&", "|", "^", ">>", "<<", ">>>", ",", "?", ":", "instanceof", "is", "[", ".."}));
	
	private static final Set<String> UNARY_OPERATORS = new HashSet<String>(Arrays.asList(new String[]{"+", "-", "!", "~", "new", "["}));

	private static final Pattern BLANK_PATTERN = Pattern.compile("^(\\s+)");
	
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

	private UnaryOperator createUnaryOperator(String name, int priority, int offset) {
		if ("+".equals(name)) {
			return new PositiveOperator(name, priority, offset);
		} else if ("-".equals(name)) {
			return new NegativeOperator(name, priority, offset);
		} else if ("!".equals(name)) {
			return new NotOperator(name, priority, offset);
		} else if ("~".equals(name)) {
			return new BitNotOperator(name, priority, offset);
		} else if ("[".equals(name)) {
			return new ListOperator(name, priority, offset);
		} else if (name.startsWith("new ")) {
			return new NewOperator(name.substring(4), priority, offset);
		} else if (StringUtils.isFunction(name)) {
			return new StaticMethodOperator(name.substring(1), priority, offset);
		} else if (StringUtils.isTyped(name)) {
			return new CastOperator(name, priority, offset);
		} else {
			throw new UnsupportedOperationException("Unsupported unary operator " + name);
		}
	}

	private BinaryOperator createBinaryOperator(String name, int priority, int offset) {
		if ("+".equals(name)) {
			return new AddOperator(name, priority, offset);
		} else if ("-".equals(name)) {
			return new SubOperator(name, priority, offset);
		} else if ("*".equals(name)) {
			return new MulOperator(name, priority, offset);
		} else if ("/".equals(name)) {
			return new DivOperator(name, priority, offset);
		} else if ("%".equals(name)) {
			return new ModOperator(name, priority, offset);
		} else if ("==".equals(name)) {
			return new EqualsOperator(name, priority, offset);
		} else if ("!=".equals(name)) {
			return new NotEqualsOperator(name, priority, offset);
		} else if (">".equals(name)) {
			return new GreaterOperator(name, priority, offset);
		} else if (">=".equals(name)) {
			return new GreaterEqualsOperator(name, priority, offset);
		} else if ("<".equals(name)) {
			return new LessOperator(name, priority, offset);
		} else if ("<=".equals(name)) {
			return new LessEqualsOperator(name, priority, offset);
		} else if ("&&".equals(name)) {
			return new AndOperator(name, priority, offset);
		} else if ("||".equals(name)) {
			return new OrOperator(name, priority, offset);
		} else if ("&".equals(name)) {
			return new BitAndOperator(name, priority, offset);
		} else if ("|".equals(name)) {
			return new BitOrOperator(name, priority, offset);
		} else if ("^".equals(name)) {
			return new BitXorOperator(name, priority, offset);
		} else if (">>".equals(name)) {
			return new RightShiftOperator(name, priority, offset);
		} else if ("<<".equals(name)) {
			return new LeftShiftOperator(name, priority, offset);
		} else if (">>>".equals(name)) {
			return new UnsignShiftOperator(name, priority, offset);
		} else if (",".equals(name)) {
			return new ArrayOperator(name, priority, offset);
		} else if ("?".equals(name)) {
			return new ConditionOperator(name, priority, offset);
		} else if (":".equals(name)) {
			return new EntryOperator(name, priority, offset);
		} else if ("instanceof".equals(name)) {
			return new InstanceofOperator(name, priority, offset);
		} else if ("[".equals(name)) {
			return new IndexOperator(name, priority, offset);
		} else if ("..".equals(name)) {
			return new SequenceOperator(name, priority, offset);
		} else if (StringUtils.isFunction(name)) {
			return new MethodOperator(name.substring(1), priority, offset);
		} else {
			throw new UnsupportedOperationException("Unsupported binary operator " + name);
		}
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
	
	private boolean isPackageName(String msg) {
		return StringUtils.isNamed(msg) || StringUtils.isFunction(msg);
	}
	
	public Expression parse(String source, int offset) throws ParseException {
		if (expressionFilter != null) {
			source = expressionFilter.filter(source, source);
		}
		LinkedStack<Expression> parameterStack = new LinkedStack<Expression>();
		LinkedStack<Operator> operatorStack = new LinkedStack<Operator>();
		Map<Operator, Token> operatorTokens = new HashMap<Operator, Token>();
		List<Token> tokens = scanner.scan(source);
		boolean beforeOperator = true;
		for (int i = 0; i < tokens.size(); i ++) {
			Token token = tokens.get(i);
			String msg = token.getMessage().trim();
			if ("new".equals(msg)) {
				StringBuilder buf = new StringBuilder();
				while (i + 1 < tokens.size() && isPackageName(tokens.get(i + 1).getMessage().trim())) {
					buf.append(tokens.get(i + 1).getMessage().trim());
					i ++;
				} 
				try {
					msg = "new " + buf.toString();
				} catch (Exception e) {
					throw new ParseException(e.getMessage(), token.getOffset());
				}
			} else if ("@".equals(msg)) {
				StringBuilder buf = new StringBuilder();
				buf.append(msg);
				while (i + 2 < tokens.size() 
						&& isPackageName(tokens.get(i + 1).getMessage().trim())
						&& isPackageName(tokens.get(i + 2).getMessage().trim())) {
					buf.append(tokens.get(i + 1).getMessage().trim());
					i ++;
				}
				try {
					msg = buf.toString();
				} catch (Exception e) {
					throw new ParseException(e.getMessage(), token.getOffset());
				}
			} else if ("gt".equals(msg)) {
				msg = ">";
			} else if ("ge".equals(msg)) {
				msg = ">=";
			} else if ("lt".equals(msg)) {
				msg = ">";
			} else if ("le".equals(msg)) {
				msg = "<=";
			} else if ("is".equals(msg)) {
				msg = "instanceof";
			} else if (! "null".equals(msg) && ! "true".equals(msg) 
					&& ! "false".equals(msg) && StringUtils.isNamed(msg)) {
				if (i < tokens.size() - 1) {
					String next = tokens.get(i + 1).getMessage().trim();
					if ("(".equals(next)) {
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
							UnaryOperator operator = createUnaryOperator(msg, getPriority(msg, true), getTokenOffset(token) + offset);
							operatorTokens.put(operator, token);
							operatorStack.push(operator);
							beforeOperator = true;
							i ++;
							continue;
						}
					}
				}
				if (i > 0) {
					String pre = tokens.get(i - 1).getMessage().trim();
					if ("is".equals(pre) || "instanceof".equals(pre)) {
						StringBuilder buf = new StringBuilder();
						buf.append("@");
						buf.append(msg);
						while (i + 1 < tokens.size() && isPackageName(tokens.get(i + 1).getMessage().trim())) {
							buf.append(tokens.get(i + 1).getMessage().trim());
							i ++;
						}
						msg = buf.toString();
					}
				}
			}
			// ================
			if (msg.length() >= 2 
					&& (msg.startsWith("\"") && msg.endsWith("\"") 
					|| msg.startsWith("\'") && msg.endsWith("\'") 
					|| msg.startsWith("`") && msg.endsWith("`"))) {
				String value = StringUtils.unescapeString(msg.substring(1, msg.length() - 1));
				if (msg.startsWith("`") && value.length() == 1) {
					parameterStack.push(new Constant(value.charAt(0), false, token.getOffset()));
				} else if (msg.startsWith("`") && value.length() == 2 && value.charAt(0) == '\\') {
					parameterStack.push(new Constant(value.charAt(1), true, token.getOffset()));
				} else {
					parameterStack.push(new Constant(StringUtils.unescapeString(value), false, token.getOffset()));
				}
				beforeOperator = false;
			} else if (StringUtils.isNumber(msg)) {
				Object value;
				boolean boxed = false;
				if (msg.endsWith("b") || msg.endsWith("B")) {
					value = Byte.valueOf(msg.substring(0, msg.length() - 1));
					boxed = msg.endsWith("B");
				} else if (msg.endsWith("s") || msg.endsWith("S")) {
					value = Short.valueOf(msg.substring(0, msg.length() - 1));
					boxed = msg.endsWith("S");
				} else if (msg.endsWith("i") || msg.endsWith("I")) {
					value = Integer.valueOf(msg.substring(0, msg.length() - 1));
					boxed = msg.endsWith("I");
				} else if (msg.endsWith("l") || msg.endsWith("L")) {
					value = Long.valueOf(msg.substring(0, msg.length() - 1));
					boxed = msg.endsWith("L");
				} else if (msg.endsWith("f") || msg.endsWith("F")) {
					value = Float.valueOf(msg.substring(0, msg.length() - 1));
					boxed = msg.endsWith("F");
				} else if (msg.endsWith("d") || msg.endsWith("D")) {
					value = Double.valueOf(msg.substring(0, msg.length() - 1));
					boxed = msg.endsWith("D");
				} else if (msg.indexOf('.') >= 0) {
					value = Double.valueOf(msg);
				} else {
					value = Integer.valueOf(msg);
				}
				parameterStack.push(new Constant(value, boxed, token.getOffset()));
				beforeOperator = false;
			} else if ("null".equals(msg)) {
				parameterStack.push(new Constant(null, false, token.getOffset()));
				beforeOperator = false;
			} else if ("true".equals(msg) || "false".equals(msg)) {
				parameterStack.push(new Constant("true".equals(msg) ? Boolean.TRUE : Boolean.FALSE, false, token.getOffset()));
				beforeOperator = false;
			} else if ("TRUE".equals(msg) || "FALSE".equals(msg)) {
				parameterStack.push(new Constant("TRUE".equals(msg) ? Boolean.TRUE : Boolean.FALSE, true, token.getOffset()));
				beforeOperator = false;
			} else if (msg.length() > 1 && msg.startsWith("@")) {
				parameterStack.push(new Constant(ClassUtils.forName(importPackages, msg.substring(1).trim()), false, token.getOffset()));
				beforeOperator = false;
			} else if (StringUtils.isNamed(msg) && ! "instanceof".equals(msg)) {
				parameterStack.push(new Variable(msg, getTokenOffset(token) + offset));
				beforeOperator = false;
			} else if ("(".equals(msg)) {
				operatorStack.push(Bracket.ROUND);
				beforeOperator = true;
			} else if (")".equals(msg)) {
				while (popOperator(parameterStack, operatorStack, operatorTokens, offset) != Bracket.ROUND);
				beforeOperator = false;
			} else if ("]".equals(msg)) {
				while (popOperator(parameterStack, operatorStack, operatorTokens, offset) != Bracket.SQUARE);
				beforeOperator = false;
			} else {
				if (forbidMethods != null && StringUtils.isFunction(msg)) {
					String method = msg.substring(1);
					for (String forbid : forbidMethods) {
						if (method.startsWith(forbid)) {
							throw new ParseException("Forbid call method " + method + " by forbid.method=" + forbid + " config.", offset);
						}
					}
				}
				if (beforeOperator) {
					if (! msg.startsWith("new ") && ! StringUtils.isFunction(msg) && ! UNARY_OPERATORS.contains(msg)) {
						throw new ParseException("Unsupported binary operator " + msg, getTokenOffset(token) + offset);
					}
					UnaryOperator operator = createUnaryOperator(msg, getPriority(msg, true), getTokenOffset(token) + offset);
					operatorTokens.put(operator, token);
					operatorStack.push(operator);
				} else {
					if (! StringUtils.isFunction(msg) && ! BINARY_OPERATORS.contains(msg)) {
						throw new ParseException("Unsupported binary operator " + msg, getTokenOffset(token) + offset);
					}
					BinaryOperator operator = createBinaryOperator(msg, getPriority(msg, false), getTokenOffset(token) + offset);
					operatorTokens.put(operator, token);
					while (! operatorStack.isEmpty() && ! (operatorStack.peek() instanceof Bracket)
							&& operatorStack.peek().getPriority() >= operator.getPriority()) {
						popOperator(parameterStack, operatorStack, operatorTokens, offset);
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
						parameterStack.push(new Constant(null, true, token.getOffset()));
						beforeOperator = false;
					}
					if (empty) {
						i = i + 2;
					}
				}
			}
		}
		while (! operatorStack.isEmpty()) {
			Operator operator = popOperator(parameterStack, operatorStack, operatorTokens, offset);
			if (operator == Bracket.ROUND || operator == Bracket.SQUARE) {
				throw new ParseException("Miss right parenthesis", offset);
			}
		}
		Expression result = parameterStack.pop();
		if (! parameterStack.isEmpty()) {
			Expression parent = parameterStack.pop();
			throw new ParseException("Miss parameter in the operator " + parent, parent.getOffset());
		}
		return result;
	}

	private Operator popOperator(LinkedStack<Expression> parameterStack, LinkedStack<Operator> operatorStack, Map<Operator, Token> operatorTokens, int offset) throws ParseException {
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

	private static class Bracket extends Operator {

		public static final Bracket ROUND = new Bracket("(");
		
		public static final Bracket SQUARE = new Bracket("[");

		private Bracket(String name) {
			super(name, Integer.MAX_VALUE, 0);
		}

		@SuppressWarnings("unchecked")
		public List<Node> getChildren() {
			return Collections.EMPTY_LIST;
		}

	}

}