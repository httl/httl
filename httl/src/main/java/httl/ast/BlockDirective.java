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
import java.util.List;

/**
 * BlockDirective. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class BlockDirective extends Directive {

	private List<Node> children;

	private EndDirective end;

	public BlockDirective(int offset) {
		super(offset);
	}

	public void accept(Visitor visitor) throws IOException, ParseException {
		Expression expression = getExpression();
		if (expression != null) {
			expression.accept(visitor);
		}
		if (visitor.visit(this)) {
			if (children != null) {
				for (Node node : children) {
					node.accept(visitor);
				}
			}
			if (end != null) {
				end.accept(visitor);
			}
		}
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) throws ParseException {
		if (this.children != null)
			throw new ParseException("Can not modify children.", getOffset());
		this.children = children;
		for (Node node : children) {
			((Statement) node).setParent(this);
		}
	}

	public EndDirective getEnd() {
		return end;
	}

	public void setEnd(EndDirective end) throws ParseException {
		if (this.end != null)
			throw new ParseException("Can not modify end.", this.end.getOffset());
		this.end = end;
		end.setStart(this);
	}

}