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
package httl.internal.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public abstract class DfaScanner {

	// BREAK，结束片段，并回到起始状态，最多可回退100个字符，退回的字符将重新读取
	// state = BREAK - 退回字符数
	// state = BREAK - 1 // 结束并退回1个字符，即不包含当前字符
	public static final int BREAK = -1;

	// PUSH，压栈，并回到指定状态，最多900个状态
	// state = PUSH - 压栈后回到状态数
	// state = PUSH - 4 // 压入第2个栈，压栈后回到状态4
	public static final int PUSH = -100;

	// POP，弹栈，并回到指定状态，栈空回到起始状态0，表示结束片段
	// state = POP - 弹栈后回到状态数 - EMPTY * 栈空回到状态数
	// state = POP - 4 - EMPTY * 5 // 弹栈后回到状态4，栈空回到状态5
	public static final int POP = -1000000;
	public static final int EMPTY = -POP;

	// ERROR，解析出错，抛出异常
	// state = ERROR - 错误码
	// state = ERROR - 1 // 出错，并返回错误码为1的异常信息。
	public static final int ERROR = -100000000;

	public List<Token> scan(String charStream) throws ParseException {
		List<Token> tokens = new ArrayList<Token>();
		// 解析时状态 ----
		StringBuilder buffer = new StringBuilder(); // 缓存字符
		StringBuilder remain = new StringBuilder(); // 残存字符
		int pre = 0; // 上一状态
		int state = 0; // 当前状态
		char ch; // 当前字符
		int offset = 0;

		// 逐字解析 ----
		int i = 0;
		int p = 0;
		for(;;) {
			if (remain.length() > 0) { // 先处理残存字符
				ch = remain.charAt(0);
				remain.deleteCharAt(0);
			} else { // 没有残存字符则读取字符流
				if (i >= charStream.length()) {
					break;
				}
				ch = charStream.charAt(i ++);
				offset ++;
			}

			buffer.append(ch); // 将字符加入缓存
			state = next(state, ch); // 从状态机图中取下一状态
			if (state <= ERROR) {
				throw new ParseException("DFAScanner.state.error, error code: " + (ERROR - state), offset - buffer.length());
			}
			if (state <= POP) {
				int n = - (state % POP);
				int e = (state - n) / POP - 1;
				if (p <= 0) {
					throw new ParseException("DFAScanner.mismatch.stack", offset - buffer.length());
				}
				p --;
				if (p == 0) {
					state = e;
					if (state == 0) {
						state = BREAK;
					}
				} else {
					state = n;
					continue;
				}
			} else if (state <= PUSH) {
				p ++;
				state = PUSH - state;
				continue;
			}
			if (state <= BREAK) { // 负数表示接收状态
				int acceptLength = buffer.length() + state - BREAK;
				if (acceptLength < 0 || acceptLength > buffer.length())
					throw new ParseException("DFAScanner.accepter.error", offset - buffer.length());
				if (acceptLength != 0) {
					String message = buffer.substring(0, acceptLength);
					Token token = new Token(message, offset - buffer.length(), pre);
					tokens.add(token);// 完成接收
				}
				if (acceptLength != buffer.length())
					remain.insert(0, buffer.substring(acceptLength)); // 将未接收的缓存记入残存
				buffer.setLength(0); // 清空缓存
				state = 0; // 回归到初始状态
			}
			pre = state;
		}
		// 接收最后缓存中的内容
		if (buffer.length() > 0) {
			String message = buffer.toString();
			tokens.add(new Token(message, offset - message.length(), pre));
		}
		return tokens;
	}

	public abstract int next(int state, char ch);

}