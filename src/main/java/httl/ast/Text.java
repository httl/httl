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
package httl.ast;

import httl.internal.util.StringUtils;

import java.text.ParseException;

/**
 * Text. (SPI, Prototype, ThreadSafe)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Text extends Statement {

	private final String content;

	private final boolean literal;

	public Text(String content, boolean literal, int offset) throws ParseException {
		super(offset);
		if (StringUtils.isEmpty(content)) {
			throw new ParseException("The text content == null.", offset);
		}
		this.content = content;
		this.literal = literal;
	}

	public String getContent() {
		return content;
	}

	public boolean isLiteral() {
		return literal;
	}

	@Override
	public String toString() {
		return literal ? "#[" + content + "]#" : content;
	}

}