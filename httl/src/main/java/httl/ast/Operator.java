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

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

/**
 * Operator. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Operator extends Expression {

	private final String name;

	private final int priority;

	private List<Node> children;

	public Operator(String name, int priority, int offset){
		super(offset);
		this.name = name;
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Node> getChildren() {
		return children == null ? Collections.EMPTY_LIST : children;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setChildren(List<Expression> children) throws ParseException {
		if (this.children != null)
			throw new ParseException("Can not modify operator parameters.", getOffset());
		for (Expression node : children) {
			node.setParent(this);
		}
		this.children = (List) Collections.unmodifiableList(children);
	}

	@Override
	public String toString() {
		return getName();
	}

}