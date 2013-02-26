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
package httl.spi.parsers;

import httl.ast.AbstractVisitor;
import httl.ast.BreakIf;
import httl.ast.Else;
import httl.ast.ElseIf;
import httl.ast.End;
import httl.ast.Foreach;
import httl.ast.If;
import httl.ast.Macro;
import httl.ast.Set;
import httl.ast.Text;
import httl.ast.Value;

/**
 * CompileVisitor
 * 
 * @author @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CompileVisitor extends AbstractVisitor {
	
	private StringBuilder builder = new StringBuilder();

	@Override
	public void visit(Text node) {
		builder.append("	$output.write(");
		builder.append(node.getContent());
		builder.append(");\n");
	}

	@Override
	public void visit(Value node) {
		builder.append("	$output.write(");
		builder.append(node.getExpression().getCode());
		builder.append(");\n");
	}

	@Override
	public void visit(Set node) {
		builder.append("	");
		builder.append(node.getType().getCanonicalName());
		builder.append(" ");
		builder.append(node.getName());
		builder.append(" = ");
		builder.append(node.getExpression().getCode());
		builder.append(");\n");
	}

	@Override
	public void visit(If node) {
		builder.append("	if(");
		builder.append(node.getExpression().getCode());
		builder.append(") {\n");
	}

	@Override
	public void visit(ElseIf node) {
		builder.append("	elseif(");
		builder.append(node.getExpression().getCode());
		builder.append(") {\n");
	}

	@Override
	public void visit(Else node) {
		builder.append("	else {\n");
	}

	@Override
	public void visit(Foreach node) {
		builder.append("	for(");
		builder.append(node.getExpression().getCode());
		builder.append(") {\n");
	}

	@Override
	public void visit(BreakIf node) {
		builder.append("	if(");
		builder.append(node.getExpression().getCode());
		builder.append(") break;\n");
	}

	@Override
	public void visit(Macro node) {
	}

	@Override
	public void visit(End node) {
		builder.append("	}\n");
	}

}
