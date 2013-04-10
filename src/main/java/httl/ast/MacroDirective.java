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
import httl.internal.util.StringUtils;

import java.text.ParseException;

/**
 * MacroDirective. (SPI, Prototype, ThreadSafe)
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MacroDirective extends BlockDirective {

	private final String name;

	public MacroDirective(String name, int offset) throws ParseException {
		super(offset);
		if (! StringUtils.isNamed(name)) {
			throw new ParseException("Illegal macro name " + name + ", Can not contains any symbol.", offset);
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setParent(Node parent) throws ParseException {
		if (parent.getClass() !=  MacroDirective.class && parent.getClass() !=  RootDirective.class)
			throw new ParseException("Can not define macro inside the #" + parent.getClass().getSimpleName().toLowerCase() + " directive.", getOffset());
		super.setParent(parent);
	}

	@Override
	public String toString() {
		return "#macro(" + name + ")";
	}

}