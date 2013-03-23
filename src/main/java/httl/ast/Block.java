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

import httl.Visitor;

import java.text.ParseException;
import java.util.List;

/**
 * Block
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Block extends Statement {

	private List<Statement> children;

	private End end;

	public Block(int offset) {
		super(offset);
	}

	public void accept(Visitor visitor) throws ParseException {
		Expression expression = getExpression();
		if (expression != null) {
			expression.accept(visitor);
		}
		if (visitor.visit(this)) {
			if (children != null) {
				for (Statement directive : children) {
					directive.accept(visitor);
				}
			}
			if (end != null) {
				end.accept(visitor);
			}
		}
	}

	public List<Statement> getChildren() {
		return children;
	}

	public void setChildren(List<Statement> children) {
		if (this.children != null)
			throw new IllegalStateException("Can not modify children.");
		this.children = children;
		for (Statement node : children) {
			node.setParent(this);
		}
	}

	public End getEnd() {
		return end;
	}

	public void setEnd(End end) {
		if (this.end != null)
			throw new IllegalStateException("Can not modify end.");
		this.end = end;
		end.setStart(this);
	}

}
