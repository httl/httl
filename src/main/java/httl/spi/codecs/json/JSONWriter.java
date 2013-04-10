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
package httl.spi.codecs.json;

import httl.internal.util.Stack;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * JSON Writer.
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class JSONWriter {

	private static final byte UNKNOWN = 0, ARRAY = 1, OBJECT = 2,
			OBJECT_VALUE = 3;

	private static class State {
		private byte type;
		private int itemCount = 0;

		State(byte t) {
			type = t;
		}
	}

	private Writer writer;

	private State state = new State(UNKNOWN);

	private Stack<State> stack = new Stack<State>();

	public JSONWriter(Writer writer) {
		this.writer = writer;
	}

	public JSONWriter(OutputStream is, String charset)
			throws UnsupportedEncodingException {
		writer = new OutputStreamWriter(is, charset);
	}

	/**
	 * object begin.
	 * 
	 * @return this.
	 * @throws IOException.
	 */
	public JSONWriter objectBegin() throws IOException {
		beforeValue();

		writer.write(JSON.LBRACE);
		stack.push(state);
		state = new State(OBJECT);
		return this;
	}

	/**
	 * object end.
	 * 
	 * @return this.
	 * @throws IOException.
	 */
	public JSONWriter objectEnd() throws IOException {
		writer.write(JSON.RBRACE);
		state = stack.pop();
		return this;
	}

	/**
	 * object item.
	 * 
	 * @param name
	 *            name.
	 * @return this.
	 * @throws IOException.
	 */
	public JSONWriter objectItem(String name) throws IOException {
		beforeObjectItem();

		writer.write(JSON.QUOTE);
		writer.write(escape(name));
		writer.write(JSON.QUOTE);
		writer.write(JSON.COLON);
		return this;
	}

	/**
	 * array begin.
	 * 
	 * @return this.
	 * @throws IOException.
	 */
	public JSONWriter arrayBegin() throws IOException {
		beforeValue();

		writer.write(JSON.LSQUARE);
		stack.push(state);
		state = new State(ARRAY);
		return this;
	}

	/**
	 * array end, return array value.
	 * 
	 * @return this.
	 * @throws IOException.
	 */
	public JSONWriter arrayEnd() throws IOException {
		writer.write(JSON.RSQUARE);
		state = stack.pop();
		return this;
	}

	/**
	 * value.
	 * 
	 * @return this.
	 * @throws IOException.
	 */
	public JSONWriter valueNull() throws IOException {
		beforeValue();

		writer.write(JSON.NULL);
		return this;
	}

	/**
	 * value.
	 * 
	 * @param value
	 *            value.
	 * @return this.
	 * @throws IOException
	 */
	public JSONWriter valueString(String value) throws IOException {
		beforeValue();

		writer.write(JSON.QUOTE);
		writer.write(escape(value));
		writer.write(JSON.QUOTE);
		return this;
	}

	/**
	 * value.
	 * 
	 * @param value
	 *            value.
	 * @return this.
	 * @throws IOException
	 */
	public JSONWriter valueBoolean(boolean value) throws IOException {
		beforeValue();

		writer.write(value ? "true" : "false");
		return this;
	}

	/**
	 * value.
	 * 
	 * @param value
	 *            value.
	 * @return this.
	 * @throws IOException
	 */
	public JSONWriter valueInt(int value) throws IOException {
		beforeValue();

		writer.write(String.valueOf(value));
		return this;
	}

	/**
	 * value.
	 * 
	 * @param value
	 *            value.
	 * @return this.
	 * @throws IOException
	 */
	public JSONWriter valueLong(long value) throws IOException {
		beforeValue();

		writer.write(String.valueOf(value));
		return this;
	}

	/**
	 * value.
	 * 
	 * @param value
	 *            value.
	 * @return this.
	 * @throws IOException
	 */
	public JSONWriter valueFloat(float value) throws IOException {
		beforeValue();

		writer.write(String.valueOf(value));
		return this;
	}

	/**
	 * value.
	 * 
	 * @param value
	 *            value.
	 * @return this.
	 * @throws IOException
	 */
	public JSONWriter valueDouble(double value) throws IOException {
		beforeValue();

		writer.write(String.valueOf(value));
		return this;
	}

	private void beforeValue() throws IOException {
		switch (state.type) {
		case ARRAY:
			if (state.itemCount++ > 0)
				writer.write(JSON.COMMA);
			return;
		case OBJECT:
			throw new IOException("Must call objectItem first.");
		case OBJECT_VALUE:
			state.type = OBJECT;
			return;
		}
	}

	private void beforeObjectItem() throws IOException {
		switch (state.type) {
		case OBJECT_VALUE:
			writer.write(JSON.NULL);
		case OBJECT:
			state.type = OBJECT_VALUE;
			if (state.itemCount++ > 0)
				writer.write(JSON.COMMA);
			return;
		default:
			throw new IOException("Must call objectBegin first.");
		}
	}

	private static final String[] CONTROL_CHAR_MAP = new String[] { "\\u0000",
			"\\u0001", "\\u0002", "\\u0003", "\\u0004", "\\u0005", "\\u0006",
			"\\u0007", "\\b", "\\t", "\\n", "\\u000b", "\\f", "\\r", "\\u000e",
			"\\u000f", "\\u0010", "\\u0011", "\\u0012", "\\u0013", "\\u0014",
			"\\u0015", "\\u0016", "\\u0017", "\\u0018", "\\u0019", "\\u001a",
			"\\u001b", "\\u001c", "\\u001d", "\\u001e", "\\u001f" };

	private static String escape(String str) {
		if (str == null)
			return str;
		int len = str.length();
		if (len == 0)
			return str;

		char c;
		StringBuilder sb = null;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (c < ' ') // control char.
			{
				if (sb == null) {
					sb = new StringBuilder(len << 1);
					sb.append(str, 0, i);
				}
				sb.append(CONTROL_CHAR_MAP[c]);
			} else {
				switch (c) {
				case '\\':
				case '/':
				case '"':
					if (sb == null) {
						sb = new StringBuilder(len << 1);
						sb.append(str, 0, i);
					}
					sb.append('\\').append(c);
					break;
				default:
					if (sb != null)
						sb.append(c);
				}
			}
		}
		return sb == null ? str : sb.toString();
	}
}