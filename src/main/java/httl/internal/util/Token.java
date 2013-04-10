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


/**
 * Token. (Tool, Prototype, NotThreadsafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Token {

	public static final int UNKNOWN_TYPE = -1;

	private final String message;

	private final int offset;

	private final int type;

	/**
	 * Create Token.
	 *
	 * @param message message
	 * @param offset offset
	 */
	public Token(String message, int offset) {
		this(message, offset, UNKNOWN_TYPE);
	}

	/**
	 * Create Token with type.
	 *
	 * @param message message
	 * @param offset offset
	 * @param type type
	 */
	public Token(String message, int offset, int type) {
		if (StringUtils.isEmpty(message))
			throw new IllegalArgumentException("message == null");
		this.message = message;
		this.offset = offset;
		this.type = type;
	}

	/**
	 * Get message.
	 *
	 * @return message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get offset.
	 * 
	 * @return offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Get type.
	 * 
	 * @return type.
	 */
	public int getType() {
		return type;
	}

	@Override
	public String toString() {
		return message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + offset;
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Token other = (Token) obj;
		if (message == null) {
			if (other.message != null) return false;
		} else if (!message.equals(other.message)) return false;
		if (offset != other.offset) return false;
		if (type != other.type) return false;
		return true;
	}

}