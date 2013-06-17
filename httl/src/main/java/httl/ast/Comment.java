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

/**
 * Comment. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Comment extends Statement {

	private final String content;

	private final boolean block;

	public Comment(String content, boolean block, int offset) {
		super(offset);
		this.content = content;
		this.block = block;
	}

	public String getContent() {
		return content;
	}

	public boolean isBlock() {
		return block;
	}

	@Override
	public String toString() {
		return block ? "#[" + content + "]#" : "##" + content + "\n";
	}
}