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

import java.util.List;

/**
 * Operator. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class Operator extends Expression {

	private final String name;
	
	private final int priority;

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

	public abstract List<Node> getChildren();

	@Override
	public String toString() {
		return getName();
	}

}