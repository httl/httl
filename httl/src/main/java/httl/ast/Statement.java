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

import httl.Node;
import httl.Visitor;

import java.io.IOException;
import java.text.ParseException;

/**
 * Statement. (SPI, Prototype, ThreadSafe)
 * 
 * @author liangfei
 */
public abstract class Statement implements Node {

	private final int offset;

	private Node parent;

	public Statement(int offset) {
		this.offset = offset;
	}

	public void accept(Visitor visitor) throws IOException, ParseException {
		visitor.visit(this);
	}

	public int getOffset() {
		return offset;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) throws ParseException {
		if (this.parent != null)
			throw new ParseException("Can not modify parent.", getOffset());
		this.parent = parent;
	}

}