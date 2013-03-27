package httl.spi.translators.visitors;

import httl.ast.AstVisitor;
import httl.ast.Var;
import httl.ast.Variable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class VariableVisitor extends AstVisitor {

	private Class<?> defaultVariableType = Object.class;

	private final Map<String, Class<?>> variables = new HashMap<String, Class<?>>();

	public void setDefaultVariableType(Class<?> defaultVariableType) {
		this.defaultVariableType = defaultVariableType;
	}

	public Map<String, Class<?>> getVariables() {
		return variables;
	}

	@Override
	public void visit(Var node) throws ParseException {
		if (node.getExpression() == null) {
			Type type = node.getType();
			Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
			variables.put(node.getName(), clazz);
		}
	}

	@Override
	public void visit(Variable node) throws ParseException {
		if (! variables.containsKey(node.getName())) {
			variables.put(node.getName(), defaultVariableType);
		}
	}

}
