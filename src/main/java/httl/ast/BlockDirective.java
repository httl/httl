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

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * BlockDirective
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class BlockDirective extends Directive {

	private End end;

	private List<Directive> children;

	public BlockDirective() {
		this(0);
	}

	public BlockDirective(int offset) {
		super(offset);
		end = new End();
		end.setStart(this);
		end.setOffset(offset);
	}

	public void render(Map<String, Object> context, Object out) throws IOException,
			ParseException {
		if (children != null) {
			for (Directive directive : children) {
				directive.render(context, out);
			}
		}
	}

	public void accept(Visitor visitor) throws ParseException {
		if (visitor.visit(this)) {
			if (children != null) {
				for (Directive directive : children) {
					directive.accept(visitor);
				}
			}
			if (end != null) {
				end.accept(visitor);
			}
		}
	}

	public List<Directive> getChildren() {
		return children;
	}

	public void setChildren(List<Directive> children) {
		this.children = children;
		for (Directive node : children) {
			node.setParent(this);
		}
	}

	public End getEnd() {
		return end;
	}

	public void setEnd(End end) {
		end.setStart(this);
		this.end = end;
	}

}
