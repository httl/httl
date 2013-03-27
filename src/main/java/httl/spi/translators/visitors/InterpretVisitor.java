package httl.spi.translators.visitors;

import httl.Context;
import httl.Node;
import httl.ast.AstVisitor;
import httl.ast.BinaryOperator;
import httl.ast.Break;
import httl.ast.Constant;
import httl.ast.Else;
import httl.ast.For;
import httl.ast.If;
import httl.ast.Macro;
import httl.ast.Text;
import httl.ast.UnaryOperator;
import httl.ast.Value;
import httl.ast.Var;
import httl.ast.Variable;
import httl.internal.util.ClassUtils;
import httl.internal.util.CollectionUtils;
import httl.internal.util.LinkedStack;
import httl.internal.util.Status;
import httl.internal.util.StringUtils;
import httl.spi.Filter;
import httl.spi.Formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Iterator;

public class InterpretVisitor extends AstVisitor {

	private Formatter<Object> formatter;

	private Filter textFilter;

	private Filter valueFilter;

	private String[] forVariable = new String[] { "for" };

	private String ifVariable = "if";

	private String outputEncoding = "UTF-8";

	private Object out;

	private final LinkedStack<Object> parameterStack = new LinkedStack<Object>();

	public void setOut(Object out) {
		this.out = out;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void setFormatter(Formatter<Object> formatter) {
		this.formatter = formatter;
	}

	public void setTextFilter(Filter textFilter) {
		this.textFilter = textFilter;
	}

	public void setValueFilter(Filter valueFilter) {
		this.valueFilter = valueFilter;
	}

	public void setIfVariable(String ifVariable) {
		this.ifVariable = ifVariable;
	}

	public void setForVariable(String[] forVariable) {
		this.forVariable = forVariable;
	}

	private Object popExpressionResult(int offset) throws ParseException {
		Object result = parameterStack.pop();
		if (! parameterStack.isEmpty()) {
			throw new ParseException("", offset);
		}
		return result;
	}

	@Override
	public void visit(Text node) throws ParseException {
		try {
			String text = node.getContent();
			if (textFilter != null) {
				text = textFilter.filter(text, text);
			}
			if (text != null) {
				if (out instanceof Writer) {
					((Writer) out).write(text);
				} else if (out instanceof OutputStream) {
					((OutputStream) out).write(text.getBytes(outputEncoding));
				}
			}
		} catch (IOException e) {
			new ParseException(e.getMessage(), node.getOffset());
		}
	}

	@Override
	public void visit(Value node) throws ParseException {
		Object result = popExpressionResult(node.getOffset());
		String text = formatter == null ? String.valueOf(result) : formatter.toString(null, result);
		if (valueFilter != null) {
			text =  valueFilter.filter(text, text);
		}
		try {
			if (text != null) {
				if (out instanceof Writer) {
					((Writer) out).write(text);
				} else if (out instanceof OutputStream) {
					((OutputStream) out).write(text.getBytes(outputEncoding));
				}
			}
		} catch (IOException e) {
			new ParseException(e.getMessage(), node.getOffset());
		}
	}

	@Override
	public void visit(Var node) throws ParseException {
		if (node.getExpression() != null) {
			Object result = popExpressionResult(node.getOffset());
			Context.getContext().put(node.getName(), result);
		}
	}

	@Override
	public void visit(Break node) throws ParseException {
		if (node.getExpression() == null
				|| ClassUtils.isTrue(popExpressionResult(node.getOffset()))) {
			throw new BreakException();
		}
	}

	@Override
	public boolean visit(If node) throws ParseException {
		boolean result = ClassUtils.isTrue(popExpressionResult(node.getOffset()));
		Context.getContext().put(ifVariable, result);
		return result;
	}

	@Override
	public boolean visit(Else node) throws ParseException {
		boolean result = (! ClassUtils.isTrue(Context.getContext().get(ifVariable))
				&& (node.getExpression() == null
				|| ClassUtils.isTrue(popExpressionResult(node.getOffset()))));
		Context.getContext().put(ifVariable, result);
		return result;
	}

	@Override
	public boolean visit(For node) throws ParseException {
		Object data = popExpressionResult(node.getOffset());
		Iterator<?> iterator = CollectionUtils.toIterator(data);
		Status status = new Status((Status) Context.getContext().get(forVariable[0]), data);
		for (String var : forVariable) {
			Context.getContext().put(var, status);
		}
		while (iterator.hasNext()) {
			Object item = iterator.next();
			Context.getContext().put(node.getName(), item);
			for (Node child : node.getChildren()) {
				child.accept(this);
			}
			status.increment();
		}
		for (String var : forVariable) {
			Context.getContext().put(var, status.getParent());
		}
		return false;
	}

	@Override
	public boolean visit(Macro node) throws ParseException {
		return false;
	}

	@Override
	public void visit(Constant node) throws ParseException {
		parameterStack.push(node.getValue());
	}

	@Override
	public void visit(Variable node) throws ParseException {
		parameterStack.push(Context.getContext().get(node.getName()));
	}

	@Override
	public void visit(UnaryOperator node) throws ParseException {
		Object parameter = parameterStack.pop();
		Object result = parameter;
		if (parameter != null) {
			if ("+".equals(node.getName())) {
				result = parameter;
			} else if ("-".equals(node.getName())) {
				if (parameter instanceof Integer) {
					result = - ((Integer) parameter).intValue();
				} else if (parameter instanceof Long) {
					result = - ((Long) parameter).longValue();
				} else if (parameter instanceof Float) {
					result = - ((Float) parameter).floatValue();
				} else if (parameter instanceof Double) {
					result = - ((Double) parameter).doubleValue();
				} else if (parameter instanceof Short) {
					result = - ((Short) parameter).shortValue();
				} else if (parameter instanceof Byte) {
					result = - ((Byte) parameter).byteValue();
				} else {
					throw new ParseException("The unary operator \"" + node.getName() +  "\" unsupported type " + parameter.getClass(), node.getOffset());
				}
			} else if ("!".equals(node.getName())) {
				result = ! ClassUtils.isTrue(parameter);
			} else if ("~".equals(node.getName())) {
				if (parameter instanceof Integer) {
					result = ~ ((Integer) parameter).intValue();
				} else if (parameter instanceof Long) {
					result = ~ ((Long) parameter).longValue();
				} else if (parameter instanceof Short) {
					result = ~ ((Short) parameter).shortValue();
				} else if (parameter instanceof Byte) {
					result = ~ ((Byte) parameter).byteValue();
				} else {
					throw new ParseException("The unary operator \"" + node.getName() +  "\" unsupported type " + parameter.getClass(), node.getOffset());
				}
			} else if ("[".equals(node.getName())) {
			} else if ("new".equals(node.getName())) {
			} else {
				throw new ParseException("The unary operator \"" + node.getName() +  "\" unsupported type " + parameter.getClass(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(BinaryOperator node) throws ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result;
		if (leftParameter == null) {
			result = null;
		} else {
			if ("&&".equals(node.getName())) {
				result = ClassUtils.isTrue(leftParameter) && ClassUtils.isTrue(rightParameter);
			} else if ("||".equals(node.getName())) {
				if (ClassUtils.isTrue(leftParameter)) {
					result = leftParameter;
				} else {
					result = rightParameter;
				}
			} else if ("==".equals(node.getName())) {
				if (leftParameter instanceof Comparable<?>) {
					result = leftParameter.equals(rightParameter);
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("!=".equals(node.getName())) {
				if (leftParameter instanceof Comparable<?>) {
					result = ! leftParameter.equals(rightParameter);
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if (">".equals(node.getName())) {
				if (leftParameter instanceof Comparable<?>) {
					result = ((Comparable<Object>) leftParameter).compareTo(rightParameter) > 0;
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if (">=".equals(node.getName())) {
				if (leftParameter instanceof Comparable<?>) {
					result = ((Comparable<Object>) leftParameter).compareTo(rightParameter) >= 0;
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("<".equals(node.getName())) {
				if (leftParameter instanceof Comparable<?>) {
					result = ((Comparable<Object>) leftParameter).compareTo(rightParameter) < 0;
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("<=".equals(node.getName())) {
				if (leftParameter instanceof Comparable<?>) {
					result = ((Comparable<Object>) leftParameter).compareTo(rightParameter) <= 0;
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("+".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() + ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() + ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Float && rightParameter instanceof Number) {
					result = ((Number) leftParameter).floatValue() + ((Number) rightParameter).floatValue();
				} else if (leftParameter instanceof Double && rightParameter instanceof Number) {
					result = ((Number) leftParameter).doubleValue() + ((Number) rightParameter).doubleValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() + ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() + ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("-".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() - ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() - ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Float && rightParameter instanceof Number) {
					result = ((Number) leftParameter).floatValue() - ((Number) rightParameter).floatValue();
				} else if (leftParameter instanceof Double && rightParameter instanceof Number) {
					result = ((Number) leftParameter).doubleValue() - ((Number) rightParameter).doubleValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() - ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() - ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("*".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() * ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() * ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Float && rightParameter instanceof Number) {
					result = ((Number) leftParameter).floatValue() * ((Number) rightParameter).floatValue();
				} else if (leftParameter instanceof Double && rightParameter instanceof Number) {
					result = ((Number) leftParameter).doubleValue() * ((Number) rightParameter).doubleValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() * ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() * ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("/".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() / ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() / ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Float && rightParameter instanceof Number) {
					result = ((Number) leftParameter).floatValue() / ((Number) rightParameter).floatValue();
				} else if (leftParameter instanceof Double && rightParameter instanceof Number) {
					result = ((Number) leftParameter).doubleValue() / ((Number) rightParameter).doubleValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() / ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() / ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("%".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() % ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() % ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Float && rightParameter instanceof Number) {
					result = ((Number) leftParameter).floatValue() % ((Number) rightParameter).floatValue();
				} else if (leftParameter instanceof Double && rightParameter instanceof Number) {
					result = ((Number) leftParameter).doubleValue() % ((Number) rightParameter).doubleValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() % ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() % ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("&".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() & ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() & ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() & ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() & ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("|".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() | ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() | ((Number) rightParameter).longValue();
				}  else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() | ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() | ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("^".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() ^ ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() ^ ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() ^ ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() ^ ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if ("<<".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() << ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() << ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() << ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() << ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if (">>".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() >> ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() >> ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() >> ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() >> ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if (">>>".equals(node.getName())) {
				if (leftParameter instanceof Integer && rightParameter instanceof Number) {
					result = ((Number) leftParameter).intValue() >>> ((Number) rightParameter).intValue();
				} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
					result = ((Number) leftParameter).longValue() >>> ((Number) rightParameter).longValue();
				} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
					result = ((Number) leftParameter).shortValue() >>> ((Number) rightParameter).shortValue();
				} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
					result = ((Number) leftParameter).byteValue() >>> ((Number) rightParameter).byteValue();
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
				}
			} else if (StringUtils.isFunction(node.getName())) {
				String name = node.getName().substring(1);
				Class<?> leftClass = leftParameter.getClass();
				Object[] args;
				if (rightParameter == null) {
					args = new Object[0];
				} else if (rightParameter instanceof Object[]) {
					args = (Object[]) rightParameter;
				} else {
					args = new Object[] { rightParameter };
				}
				if (leftParameter.getClass().isArray() && "length".equals(name)) {
					result = Array.getLength(leftParameter);
				} else {
					try {
						try {
							Class<?>[] types = new Class<?>[args.length];
							for (int i = 0; i < args.length; i ++) {
								types[i] = args[i] == null ? null : args[i].getClass();
							}
							Method method = ClassUtils.searchMethod(leftClass, name, types);
							result = method.invoke(leftParameter, args);
						} catch (NoSuchMethodException e) {
							if (args.length > 0) {
								throw new ParseException(e.getMessage(), node.getOffset());
							} else { // search property
								try {
									String getter = "get" + name.substring(0, 1).toUpperCase()
											+ name.substring(1);
									Method method = leftClass.getMethod(getter,
											new Class<?>[0]);
									result = method.invoke(leftParameter, args);
								} catch (NoSuchMethodException e2) {
									try {
										String getter = "is"
												+ name.substring(0, 1).toUpperCase()
												+ name.substring(1);
										Method method = leftClass.getMethod(getter,
												new Class<?>[0]);
										result = method.invoke(leftParameter, args);
									} catch (NoSuchMethodException e3) {
										try {
											Field field = leftClass.getField(name);
											result = field.get(leftParameter);
										} catch (NoSuchFieldException e4) {
											throw new ParseException(e.getMessage(), node.getOffset());
										}
									}
								}
							}
						}
					} catch (ParseException e) {
						throw e;
					} catch (Exception e) {
						throw new ParseException(e.getMessage(), node.getOffset());
					}
				}
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported type " + leftParameter.getClass() + rightParameter.getClass(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

}
