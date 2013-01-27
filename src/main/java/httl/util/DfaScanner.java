package httl.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public abstract class DfaScanner {

	// BREAK，结束片段，包含当前字符
	public static final int BREAK = -1;

	// PUSH，压栈
	public static final int PUSH = -1000000;

	// POP，弹栈
	public static final int POP = -2000000;

	// ERROR，解析出错
	public static final int ERROR = -3000000;

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
				throw new ParseException("DFAScanner.state.error, error code: " + state, offset - buffer.length());
			}
			if (state <= POP) {
				p --;
				if (p < 0) {
					throw new ParseException("DFAScanner.mismatch.stack", offset - buffer.length());
				}
				if (p == 0) {
					state = BREAK;
				} else {
					state = POP - state;
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
