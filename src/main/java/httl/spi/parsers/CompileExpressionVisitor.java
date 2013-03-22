package httl.spi.parsers;

import httl.Node;
import httl.Template;
import httl.ast.BinaryOperator;
import httl.ast.Constant;
import httl.ast.Expression;
import httl.ast.ExpressionVisitor;
import httl.ast.Operator;
import httl.ast.UnaryOperator;
import httl.ast.Variable;
import httl.internal.util.ClassUtils;
import httl.internal.util.CollectionUtils;
import httl.internal.util.LinkedStack;
import httl.internal.util.MapEntry;
import httl.internal.util.ParameterizedTypeImpl;
import httl.internal.util.StringSequence;
import httl.internal.util.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileExpressionVisitor extends ExpressionVisitor {

	private LinkedStack<Expression> nodeStack = new LinkedStack<Expression>();

	private LinkedStack<Type> typeStack = new LinkedStack<Type>();

	private LinkedStack<String> codeStack = new LinkedStack<String>();

	private Map<String, Class<?>> variableTypes = new HashMap<String, Class<?>>();

	private Map<String, Class<?>> types;
	
	private Class<?> defaultVariableType;

	private Collection<Class<?>> functions;

	private String[] sizers;

	private String[] packages;

	private String[] getters;

	private List<StringSequence> sequences;
	
	public String getCode() {
		return codeStack.peek();
	}

	public Class<?> getReturnType() {
		Type type = (Class<?>) typeStack.peek();
		return (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
	}

	public Map<String, Class<?>> getVariableTypes() {
		return variableTypes;
	}

	public void setImportGetters(String[] getters) {
		this.getters = getters;
	}

	public void setDefaultVariableType(Class<?> defaultVariableType) {
		this.defaultVariableType = defaultVariableType;
	}

	public void setTypes(Map<String, Class<?>> types) {
		this.types = types;
	}

	public void setImportMethods(Collection<Class<?>> functions) {
		this.functions = functions;
	}

	public void setImportSizers(String[] sizers) {
		this.sizers = sizers;
	}

	public void setImportPackages(String[] packages) {
		this.packages = packages;
	}

	public void setImportSequences(List<StringSequence> sequences) {
		this.sequences = sequences;
	}

	public void visit(Constant node) throws ParseException {
		Object value = node.getValue();
		Class<?> type;
		String code;
		if (node == Constant.NULL) {
			type = null;
			code = "null";
		} else if (node == Constant.EMPTY) {
			type = void.class;
			code = "";
		} else if (node == Constant.TRUE) {
			type = boolean.class;
			code = "true";
		} else if (node == Constant.FALSE) {
			type = boolean.class;
			code = "false";
		} else if (value instanceof String) {
			type = String.class;
			code = "\"" + value + "\"";
		} else if (value instanceof Character) {
			type = char.class;
			code = "'" + value + "'";
		} else if (value instanceof Double) {
			type = double.class;
			code = value + "D";
		} else if (value instanceof Float) {
			type = float.class;
			code = value + "F";
		} else if (value instanceof Long) {
			type = long.class;
			code = value + "L";
		} else if (value instanceof Short) {
			type = short.class;
			code = "((short)" + value + ")";
		} else if (value instanceof Byte) {
			type = byte.class;
			code = "((byte)" + value + ")";
		} else if (value instanceof Integer) {
			type = int.class;
			code = String.valueOf(value);
		} else {
			throw new ParseException("Unsupported constant " + value, node.getOffset());
		}
		nodeStack.push(node);
		typeStack.push(type);
		codeStack.push(code);
	}

	public void visit(Variable node) throws ParseException {
		String name = node.getName();
		Class<?> type = types.get(name);
		if (type == null) {
			type = defaultVariableType;
			if (type == null) {
				type = Object.class;
			}
		}
		String code = ClassUtils.filterJavaKeyword(name);
		nodeStack.push(node);
		typeStack.push(type);
		codeStack.push(code);
		variableTypes.put(name, type);
	}

	public void visit(UnaryOperator node) throws ParseException {
		String name = node.getName();
		Expression parameterNode = nodeStack.pop();
		Type parameterType = typeStack.pop();
		String parameterCode = codeStack.pop();
		Class<?> parameterClass = (Class<?>) (parameterType instanceof ParameterizedType ? ((ParameterizedType)parameterType).getRawType() : parameterType);
		Type type;
		String code;
		if (name.startsWith("new ")) {
			type = ClassUtils.forName(this.packages, name.substring(4));
			code = name + "(" + parameterCode + ")";
		} else if (StringUtils.isTyped(name)) {
			type = ClassUtils.forName(this.packages, name);
			code = "(" + name + ")(" + parameterCode + ")";
		} else if (StringUtils.isFunction(name)) {
			name = name.substring(1);
			Class<?>[] parameterTypes;
			if (parameterType instanceof ParameterizedType) {
				parameterTypes = (Class<?>[]) ((ParameterizedType) parameterType).getActualTypeArguments();
			} else if (parameterClass == void.class) {
				parameterTypes = new Class<?>[0];
			} else {
				parameterTypes = new Class<?>[] { parameterClass };
			}
			Class<?> t = types.get(name);
			if (t != null && Template.class.isAssignableFrom(t)) {
				variableTypes.put(name, Template.class);
				type = Object.class;
				code = "(" + name + " == null ? null : " + name + ".evaluate(" + CollectionUtils.class.getName() + ".toMap(" + name + ".getVariableTypes().keySet(), new Object" + (parameterCode.length() == 0 ? "[0]" : "[] { " + parameterCode + " }") + " )))";
			} else {
				name = ClassUtils.filterJavaKeyword(name);
				type = null;
				code = null;
				if (functions != null && functions.size() > 0) {
					for (Class<?> function : functions) {
						try {
							Method method = ClassUtils.searchMethod(function, name, parameterTypes);
							if (Object.class.equals(method.getDeclaringClass())) {
								break;
							}
							type = method.getReturnType();
							if (type == void.class) {
								throw new ParseException("Can not call void method " + method.getName() + " in class " + function.getName(), node.getOffset());
							}
							if (Modifier.isStatic(method.getModifiers())) {
								code = function.getName() + "." + method.getName() + "(" + parameterCode + ")";
							} else {
								code = "$" + function.getName().replace('.', '_') + "." + method.getName() + "(" + parameterCode + ")";
							}
							break;
						} catch (NoSuchMethodException e) {
						}
					}
				}
				if (code == null) {
					throw new ParseException("No such macro \"" + name + "\" or import method \"" + name + "\" with parameters " + Arrays.toString(parameterTypes), node.getOffset());
				}
			}
		} else if (name.equals("[")) {
			if (parameterType instanceof ParameterizedType) {
				parameterClass = (Class<?>)((ParameterizedType) parameterType).getActualTypeArguments()[0];
			}
			if (Map.Entry.class.isAssignableFrom(parameterClass)) {
				type = Map.class;
				code = CollectionUtils.class.getName() + ".toMap(new " + Map.Entry.class.getCanonicalName() + "[] {" + parameterCode + "})";
			} else {
				type = Array.newInstance(parameterClass, 0).getClass();
				code = "new " + parameterClass.getCanonicalName() + "[] {" + parameterCode + "}";
			}
		} else if (name.equals("!") && ! boolean.class.equals(parameterType)) {
			type = boolean.class;
			code = "! (" + StringUtils.getConditionCode(parameterClass, parameterCode, sizers) + ")";
		} else {
			type = parameterClass;
			if (parameterNode instanceof Operator
					&& ((Operator) parameterNode).getPriority() < node.getPriority()) {
				code = name + " (" + parameterCode + ")";
			} else {
				code = name + " " + parameterCode;
			}
		}
		nodeStack.push(node);
		typeStack.push(type);
		codeStack.push(code);
	}

	public void visit(BinaryOperator node) throws ParseException {
		if (nodeStack.isEmpty()) {
			throw new ParseException("Failed to visit binary operator " + node.getName(), node.getOffset());
		}

		Expression rightParameter = nodeStack.pop();
		Type rightType = typeStack.pop();
		String rightCode = codeStack.pop();
		
		Expression leftParameter = nodeStack.pop();
		Type leftType = typeStack.pop();
		String leftCode = codeStack.pop();
		
		if (leftParameter instanceof Operator
				&& ((Operator) leftParameter).getPriority() < node.getPriority()) {
			leftCode = "(" + leftCode + ")";
		}
		
		Class<?> leftClass = (Class<?>) (leftType instanceof ParameterizedType ? ((ParameterizedType)leftType).getRawType() : leftType);
		Class<?> rightClass = (Class<?>) (rightType instanceof ParameterizedType ? ((ParameterizedType)rightType).getRawType() : rightType);
		
		String name = node.getName();
		Type type;
		String code;
		if (StringUtils.isFunction(name)) {
			name =  name.substring(1);
			type = null;
			code = null;
			if ("to".equals(name) 
					&& rightParameter instanceof Constant
					&& rightType == String.class
					&& rightCode.length() > 2
					&& rightCode.startsWith("\"") && rightCode.endsWith("\"")) {
				code = "((" + rightCode.substring(1, rightCode.length() - 1) + ")" + leftCode + ")";
				type = ClassUtils.forName(this.packages, rightCode.substring(1, rightCode.length() - 1));
			} else if ("class".equals(name)) {
				type = Class.class;
				if (leftClass.isPrimitive()) {
					code = leftClass.getCanonicalName() + ".class";
				} else {
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".getClass()");
				}
			} else if (Map.Entry.class.isAssignableFrom(leftClass)
					&& ("key".equals(name) || "value".equals(name))) {
				String var = getGenericVariableName(leftParameter);
				if (var != null) {
					Class<?> keyType = types.get(var + ":0"); // Map<K,V>第一个泛型
					Class<?> valueType = types.get(var + ":1"); // Map<K,V>第二个泛型
					if ("key".equals(name) && keyType != null) {
						type = keyType;
						code = getNotNullCode(leftParameter, type, leftCode, "((" + keyType.getCanonicalName() + ")" + leftCode + ".getKey(" + rightCode + "))");
					} else if ("value".equals(name) && valueType != null) {
						type = valueType;
						code = getNotNullCode(leftParameter, type, leftCode, "((" + valueType.getCanonicalName() + ")" + leftCode + ".getValue(" + rightCode + "))");
					}
				}
			} else if (Map.class.isAssignableFrom(leftClass)
					&& "get".equals(name)
					&& String.class.equals(rightType)) {
				String var = getGenericVariableName(leftParameter);
				if (var != null) {
					Class<?> varType = types.get(var + ":1"); // Map<K,V>第二个泛型 
					if (varType != null) {
						type = varType;
						code = getNotNullCode(leftParameter, type, leftCode, "((" + varType.getCanonicalName() + ")" + leftCode + ".get(" + rightCode + "))");
					}
				}
			} else if (List.class.isAssignableFrom(leftClass)
					&& "get".equals(name)
					&& int.class.equals(rightType)) {
				String var = getGenericVariableName(leftParameter);
				if (var != null) {
					Class<?> varType = types.get(var + ":0"); // List<T>第一个泛型
					if (varType != null) {
						type = varType;
						code = getNotNullCode(leftParameter, type, leftCode, "((" + varType.getCanonicalName() + ")" + leftCode + ".get(" + rightCode + "))");
					}
				}
			}
			if (code == null) {
				Class<?>[] rightTypes;
				if (rightType instanceof ParameterizedType) {
					rightTypes = (Class<?>[]) ((ParameterizedType) rightType).getActualTypeArguments();
				} else if (rightClass == void.class) {
					rightTypes = new Class<?>[0];
				} else {
					rightTypes = new Class<?>[] { rightClass };
				}
				if (Template.class.isAssignableFrom(leftClass)
						&& ! hasMethod(Template.class, name, rightTypes)) {
					type = Object.class;
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".getMacro(\"" + name + "\").evaluate(" + CollectionUtils.class.getName() + ".toMap(" + leftCode + ".getMacro(\"" + name + "\").getVariableTypes().keySet(), new Object" + (rightCode.length() == 0 ? "[0]" : "[] { " + rightCode + " }") + " ))");
				} else if (Map.class.isAssignableFrom(leftClass)
						&& rightTypes.length == 0
						&& ! hasMethod(Map.class, name, rightTypes)) {
					type = Object.class;
					code = leftCode + ".get(\"" + name + "\")";
					String var = getGenericVariableName(leftParameter);
					if (var != null) {
						Class<?> t = types.get(var + ":1"); // Map<K,V>第二个泛型 
						if (t != null) {
							type = t;
							code = "((" + t.getCanonicalName() + ")" + leftCode + ".get(\"" + name + "\"))";
						}
					}
				} else if (getters != null && getters.length > 0
						&& rightTypes.length == 0 
						&& ! hasMethod(leftClass, name, rightTypes)) {
					for (String getter : getters) {
						if (hasMethod(leftClass, getter, new Class<?>[] { String.class })
								|| hasMethod(leftClass, getter, new Class<?>[] { Object.class })) {
							type = Object.class;
							code = getNotNullCode(leftParameter, type, leftCode, leftCode + "." + getter + "(\"" + name + "\")");
							break;
						}
					}
				}
				name = ClassUtils.filterJavaKeyword(name);
				if (functions != null && functions.size() > 0) {
					Class<?>[] allTypes;
					String allCode;
					if (rightTypes == null || rightTypes.length == 0) {
						allTypes = new Class<?>[] {leftClass};
						allCode = leftCode;
					} else {
						allTypes = new Class<?>[rightTypes.length + 1];
						allTypes[0] = leftClass;
						System.arraycopy(rightTypes, 0, allTypes, 1, rightTypes.length);
						allCode = leftCode + ", " + rightCode;
					}
					for (Class<?> function : functions) {
						try {
							Method method = ClassUtils.searchMethod(function, name, allTypes);
							if (! Object.class.equals(method.getDeclaringClass())) {
								type = method.getReturnType();
								if (type == void.class) {
									throw new ParseException("Can not call void method " + method.getName() + " in class " + function.getName(), node.getOffset());
								}
								if (Modifier.isStatic(method.getModifiers())) {
									code = function.getName() + "." + method.getName() + "(" + allCode + ")";
								} else {
									code = "$" + function.getName().replace('.', '_') + "." + method.getName() + "(" + allCode + ")";
								}
								break;
							}
						} catch (NoSuchMethodException e) {
						}
					}
				}
				if (code == null) {
					type = getReturnType(leftParameter, leftClass, name, rightTypes, node.getOffset());
					code = getNotNullCode(leftParameter, type, leftCode, getMethodName(leftParameter, leftClass, name, rightTypes, leftCode, rightCode, node.getOffset()));
				}
			}
		} else if (name.equals(",")) {
			List<Class<?>> ts = new ArrayList<Class<?>>();
			if (leftType instanceof ParameterizedType) {
				for (Type t : ((ParameterizedType) leftType).getActualTypeArguments()) {
					ts.add((Class<?>) t);
				}
			} else if (leftType != void.class) {
				ts.add((Class<?>) leftType);
			}
			if (rightType instanceof ParameterizedType) {
				for (Type t : ((ParameterizedType) rightType).getActualTypeArguments()) {
					ts.add((Class<?>) t);
				}
			} else if (rightType != void.class)  {
				ts.add((Class<?>) rightType);
			}
			type = new ParameterizedTypeImpl(Object[].class, ts.toArray(new Class<?>[ts.size()]));
			code = leftCode + ", " + rightCode;
		} else if (name.equals("..")) {
			if (leftClass == int.class || leftClass == Integer.class 
					|| leftClass == short.class  || leftClass == Short.class
					|| leftClass == long.class || leftClass == Long.class
					|| leftClass == char.class || leftClass == Character.class) {
				type = Array.newInstance(leftClass, 0).getClass();
				code = CollectionUtils.class.getName() + ".createSequence(" + leftCode + ", " + rightCode + ")";
			} else if (leftClass == String.class 
						&& leftCode.length() >= 2 && rightCode.length() >= 2 
						&& (leftCode.startsWith("\"") || leftCode.startsWith("\'"))
						&& (leftCode.endsWith("\"") || leftCode.endsWith("\'"))
						&& (rightCode.startsWith("\"") || rightCode.startsWith("\'"))
						&& (rightCode.endsWith("\"") || rightCode.endsWith("\'"))) {
				type = String[].class;
				StringBuilder buf = new StringBuilder();
				for (String s : getSequence(leftCode.substring(1, leftCode.length() - 1), 
						rightCode.substring(1, rightCode.length() - 1))) {
					if (buf.length() > 0) {
						buf.append(",");
					}
					buf.append("\"");
					buf.append(s);
					buf.append("\"");
				}
				code = "new String[] {" + buf.toString() + "}";
			} else {
				throw new ParseException("The operator \"..\" unsupported parameter type " + leftClass, node.getOffset());
			}
		} else if (name.equals("[")) {
			if (Map.class.isAssignableFrom(leftClass)) {
				String var = getGenericVariableName(leftParameter);
				if (var != null && types.containsKey(var + ":1")) {
					Class<?> varType = types.get(var + ":1"); // Map<K,V>第二个泛型 
					type = varType;
					code = getNotNullCode(leftParameter, type, leftCode, "((" + varType.getCanonicalName() + ")" + leftCode + ".get(" + rightCode + "))");
				} else {
					type = Object.class;
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".get(" + rightCode + ")");
				}
			} else if (List.class.isAssignableFrom(leftClass)) {
				if (int[].class == rightType) {
					type = List.class;;
					code = CollectionUtils.class.getName() + ".subList(" + leftCode + ", " + rightCode + ")";
				} else if (rightType instanceof ParameterizedType
						&& ((ParameterizedType) rightType).getActualTypeArguments()[0] == int.class) {
					type = List.class;;
					code = CollectionUtils.class.getName() + ".subList(" + leftCode + ", new int[] {" + rightCode + "})";
				} else if (int.class.equals(rightType)) {
					type = Object.class;
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".get(" + rightCode + ")");
					if (leftParameter instanceof Variable) {
						String var = ((Variable)leftParameter).getName();
						Class<?> varType = types.get(var + ":0"); // List<T>第一个泛型
						if (varType != null) {
							type = varType;
							code = getNotNullCode(leftParameter, type, leftCode, "((" + varType.getCanonicalName() + ")" + leftCode + ".get(" + rightCode + "))");
						}
					}
				} else {
					throw new ParseException("The \"[]\" index type: " + rightType + " must be int!", node.getOffset());
				}
			} else if (leftClass.isArray()) {
				if (int[].class == rightType) {
					type = leftClass;
					code = CollectionUtils.class.getName() + ".subArray(" + leftCode + ", " + rightCode + ")";
				} else if (rightType instanceof ParameterizedType
						&& ((ParameterizedType) rightType).getActualTypeArguments()[0] == int.class) {
					type = leftClass;
					code = CollectionUtils.class.getName() + ".subArray(" + leftCode + ", new int[] {" + rightCode + "})";
				} else if (int.class.equals(rightType)) {
					type = leftClass.getComponentType();
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + "[" + rightCode + "]");
				} else {
					throw new ParseException("The \"[]\" index type: " + rightType + " must be int!", node.getOffset());
				}
			} else {
				throw new ParseException("Unsuptorted \"[]\" for non-array type: " + leftClass, node.getOffset());
			}
		} else if("==".equals(name) && ! "null".equals(leftCode) && ! "null".equals(rightCode)
				&& ! leftClass.isPrimitive() && ! rightClass.isPrimitive()) {
			type = boolean.class;
			code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".equals(" + rightCode + ")");
		} else if("!=".equals(name) && ! "null".equals(leftCode) && ! "null".equals(rightCode)
				&& ! leftClass.isPrimitive() && ! rightClass.isPrimitive()) {
			type = boolean.class;
			code = getNotNullCode(leftParameter, type, leftCode, "(! " + leftCode + ".equals(" + rightCode + "))");
		} else if("||".equals(name) && ! boolean.class.equals(leftClass)) {
			type = leftClass;
			code = "(" + StringUtils.getConditionCode(leftClass, leftCode, sizers) + " ? (" + leftCode + ") : (" + rightCode + "))";
		} else if("&&".equals(name) && ! boolean.class.equals(leftClass)) {
			type = boolean.class;
			if (rightParameter instanceof Operator
					&& ((Operator) rightParameter).getPriority() < node.getPriority()) {
				rightCode = "(" + rightCode + ")";
			}
			leftCode = StringUtils.getConditionCode(leftClass, leftCode, sizers);
			rightCode = StringUtils.getConditionCode(rightClass, rightCode, sizers);
			code = leftCode + " " + name + " " + rightCode;
		} else if (name.equals("?")) {
			type = rightClass;
			if (rightParameter instanceof Operator
					&& ((Operator) rightParameter).getPriority() < node.getPriority()) {
				rightCode = "(" + rightCode + ")";
			}
			leftCode = StringUtils.getConditionCode(leftClass, leftCode, sizers);
			code = leftCode + " " + name + " " + rightCode;
		} else if (":".equals(name) 
				&& ! (leftParameter instanceof BinaryOperator 
						&& "?".equals(((BinaryOperator)leftParameter).getName()))) {
			type = Map.Entry.class;
			code = "new " + MapEntry.class.getName() + "(" + leftCode + ", " + rightCode + ")";
		} else {
			type = leftClass;
			code = null;
			if ("lt".equals(name)) {
				name = "<";
			} else if ("le".equals(name)) {
				name = "<=";
			} else if ("gt".equals(name)) {
				name = ">";
			} else if ("ge".equals(name)) {
				name = ">=";
			}
			if (leftClass != null && Comparable.class.isAssignableFrom(leftClass)) {
				if (">".equals(name)) {
					type = boolean.class;
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".compareTo(" + rightCode + ") > 0");
				} else if (">=".equals(name)) {
					type = boolean.class;
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".compareTo(" + rightCode + ") >= 0");
				} else if ("<".equals(name)) {
					type = boolean.class;
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".compareTo(" + rightCode + ") < 0");
				} else if ("<=".equals(name)) {
					type = boolean.class;
					code = getNotNullCode(leftParameter, type, leftCode, leftCode + ".compareTo(" + rightCode + ") <= 0");
				}
			}
			if ("+".equals(name)) {
				if ((Collection.class.isAssignableFrom(leftClass) || leftClass.isArray()) 
								&& (Collection.class.isAssignableFrom(rightClass) || rightClass.isArray())
						|| Map.class.isAssignableFrom(leftClass) && Map.class.isAssignableFrom(rightClass)) {
					code = CollectionUtils.class.getName() + ".merge(" + leftCode+ ", " + rightCode + ")";
					type = leftClass;
				} else if (rightClass.isPrimitive() && rightType != boolean.class) {
					type = rightClass;
				} else if (leftClass.isPrimitive() && leftClass != boolean.class) {
					type = leftClass;
				} else {
					type = String.class;
				}
				if (type != String.class) {
					Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
					String typeName = clazz.getCanonicalName();
					typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
					if (! (leftClass.isPrimitive() && leftClass != boolean.class)) {
						leftCode = ClassUtils.class.getName() + ".to" + typeName + "(" + leftCode + ")";
					}
					if (! (rightClass.isPrimitive() && leftClass != boolean.class)) {
						rightCode = ClassUtils.class.getName() + ".to" + typeName + "(" + rightCode + ")";
					}
				}
			} else if ("－".equals(name) || "＊".equals(name) || "／".equals(name) || "%".equals(name)) {
				if (rightClass.isPrimitive() && rightType != boolean.class) {
					type = rightClass;
				} else if (leftClass.isPrimitive() && leftClass != boolean.class) {
					type = leftClass;
				} else {
					type = int.class;
				}
				Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
				String typeName = clazz.getCanonicalName();
				typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
				if (! (leftClass.isPrimitive() && leftClass != boolean.class)) {
					leftCode = ClassUtils.class.getName() + ".to" + typeName + "(" + leftCode + ")";
				}
				if (! (rightClass.isPrimitive() && leftClass != boolean.class)) {
					rightCode = ClassUtils.class.getName() + ".to" + typeName + "(" + rightCode + ")";
				}
			}
			if (rightParameter instanceof Operator
					&& ((Operator) rightParameter).getPriority() < node.getPriority()) {
				rightCode = "(" + rightCode + ")";
			}
			if (code == null) {
				code = leftCode + " " + name + " " + rightCode;
			}
		}
		nodeStack.push(node);
		typeStack.push(type);
		codeStack.push(code);
	}

	public static String getGenericVariableName(Expression node) {
		if (node instanceof Variable) {
			return ((Variable)node).getName();
		}
		while (node instanceof BinaryOperator) {
			String name = ((BinaryOperator)node).getName();
			if ("+".equals(name) || "||".equals(name)
					 || "&&".equals(name)
					 || ".entrySet".equals(name)) {
				node = ((BinaryOperator)node).getLeftParameter();
				if (node instanceof Variable) {
					return ((Variable)node).getName();
				}
			} else {
				return null;
			}
		}
		return null;
	}

	private List<String> getSequence(String begin, String end) {
		if (sequences != null) {
			for (StringSequence sequence : sequences) {
				if (sequence.containSequence(begin, end)) {
					return sequence.getSequence(begin, end);
				}
			}
		}
		throw new IllegalStateException("No such sequence from \"" + begin + "\" to \"" + end + "\".");
	}
	
	public static Template getMacro(Template template, String name) {
		Template macro = template.getMacros().get(name);
		if (macro == null) {
			throw new IllegalStateException("No such macro " + name + "in template " + template.getName());
		}
		return macro;
	}
	
	private String getNotNullCode(Node leftParameter, Type type, String leftCode, String code) throws ParseException {
		if (leftParameter instanceof Constant) {
			return code;
		}
		Class<?> clazz = (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType() : type);
		return "(" + leftCode + " == null ? (" + clazz.getCanonicalName() + ")" + ClassUtils.getInitCode(clazz) + " : " + code + ")";
	}

	private boolean hasMethod(Class<?> leftClass, String name, Class<?>[] rightTypes) {
		if (leftClass == null) {
			return false;
		}
		if (leftClass.isArray() && "length".equals(name)) {
			return true;
		}
		try {
			Method method = ClassUtils.searchMethod(leftClass, name, rightTypes);
			return method != null;
		} catch (NoSuchMethodException e) {
			if (rightTypes != null && rightTypes.length > 0) {
				return false;
			} else { // search property
				try {
					String getter = "get" + name.substring(0, 1).toUpperCase()
							+ name.substring(1);
					Method method = leftClass.getMethod(getter,
							new Class<?>[0]);
					return method != null;
				} catch (NoSuchMethodException e2) {
					try {
						String getter = "is"
								+ name.substring(0, 1).toUpperCase()
								+ name.substring(1);
						Method method = leftClass.getMethod(getter,
								new Class<?>[0]);
						return method != null;
					} catch (NoSuchMethodException e3) {
						try {
							Field field = leftClass.getField(name);
							return field != null;
						} catch (NoSuchFieldException e4) {
							return false;
						}
					}
				}
			}
		}
	}

	private String getMethodName(Expression leftParameter, Class<?> leftClass, String name, Class<?>[] rightTypes, String leftCode, String rightCode, int offset) throws ParseException {
		if (leftClass == null) {
			throw new ParseException("No such method " + name + "("
									 + Arrays.toString(rightTypes) + ") in null class.", offset);
		}
		if (leftClass.isArray() && "length".equals(name)) {
			return leftCode + ".length";
		}
		try {
			Method method = ClassUtils.searchMethod(leftClass, name, rightTypes);
			return leftCode + "." + method.getName() + "(" + rightCode + ")";
		} catch (NoSuchMethodException e) {
			if (rightTypes != null && rightTypes.length > 0) {
				throw new ParseException("No such method " + name + "("
						+ Arrays.toString(rightTypes) + ") in class "
						+ leftClass.getName(), offset);
			} else { // search property
				try {
					String getter = "get" + name.substring(0, 1).toUpperCase()
							+ name.substring(1);
					Method method = leftClass.getMethod(getter,
							new Class<?>[0]);
					return leftCode + "." + method.getName() + "()";
				} catch (NoSuchMethodException e2) {
					try {
						String getter = "is"
								+ name.substring(0, 1).toUpperCase()
								+ name.substring(1);
						Method method = leftClass.getMethod(getter,
								new Class<?>[0]);
						return leftCode + "." + method.getName() + "()";
					} catch (NoSuchMethodException e3) {
						try {
							Field field = leftClass.getField(name);
							return field.getName();
						} catch (NoSuchFieldException e4) {
							throw new ParseException(
									"No such property "
											+ name
											+ " in class "
											+ leftClass.getName()
											+ ", because no such method get"
											+ name.substring(0, 1)
													.toUpperCase()
											+ name.substring(1)
											+ "() or method is"
											+ name.substring(0, 1)
													.toUpperCase()
											+ name.substring(1)
											+ "() or method " + name
											+ "() or filed " + name, offset);
						}
					}
				}
			}
		}
	}

	private Class<?> getReturnType(Expression leftParameter, Class<?> leftClass, String name, Class<?>[] rightTypes, int offset) throws ParseException {
		if (leftClass == null) {
			return null;
		}
		if (leftClass.isArray() && "length".equals(name)) {
			return int.class;
		}
		try {
			Method method = ClassUtils.searchMethod(leftClass, name, rightTypes);
			return method.getReturnType();
		} catch (NoSuchMethodException e) {
			if (rightTypes != null && rightTypes.length > 0) {
				throw new ParseException("No such method " + ClassUtils.getMethodFullName(name, rightTypes) + " in class "
						+ leftClass.getName(), offset);
			} else { // search property
				try {
					String getter = "get" + name.substring(0, 1).toUpperCase()
							+ name.substring(1);
					Method method = leftClass.getMethod(getter,
							new Class<?>[0]);
					return method.getReturnType();
				} catch (NoSuchMethodException e2) {
					try {
						String getter = "is"
								+ name.substring(0, 1).toUpperCase()
								+ name.substring(1);
						Method method = leftClass.getMethod(getter,
								new Class<?>[0]);
						return method.getReturnType();
					} catch (NoSuchMethodException e3) {
						try {
							Field field = leftClass.getField(name);
							return field.getType();
						} catch (NoSuchFieldException e4) {
							throw new ParseException(
									"No such property "
											+ name
											+ " in class "
											+ leftClass.getName()
											+ ", because no such method get"
											+ name.substring(0, 1)
													.toUpperCase()
											+ name.substring(1)
											+ "() or method is"
											+ name.substring(0, 1)
													.toUpperCase()
											+ name.substring(1)
											+ "() or method " + name
											+ "() or filed " + name, offset);
						}
					}
				}
			}
		}
	}

}
