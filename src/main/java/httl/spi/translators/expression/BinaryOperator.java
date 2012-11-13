/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.translators.expression;

import httl.spi.Translator;
import httl.spi.methods.DefaultMethod;
import httl.spi.sequences.CharacterSequence;
import httl.spi.sequences.IntegerSequence;
import httl.spi.sequences.StringSequence;
import httl.util.ClassUtils;
import httl.util.CollectionUtils;
import httl.util.MapEntry;
import httl.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * BinaryOperator
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class BinaryOperator extends Operator {

	private static final long serialVersionUID = 1L;

    private Node leftParameter;
    
    private Node rightParameter;
    
    private List<StringSequence> sequences;
    
    public BinaryOperator(Translator translator, String source, int offset, Map<String, Class<?>> parameterTypes, 
                          Collection<Class<?>> functions, List<StringSequence> sequences,
                          String[] packages, String name, int priority){
        super(translator, source, offset, parameterTypes, functions, packages, name, priority);
        this.sequences = sequences;
    }

    public Node getLeftParameter() {
        return leftParameter;
    }

    public void setLeftParameter(Node leftParameter) {
        this.leftParameter = leftParameter;
    }

    public Node getRightParameter() {
        return rightParameter;
    }

    public void setRightParameter(Node rightParameter) {
        this.rightParameter = rightParameter;
    }

    private List<String> getSequence(String begin, String end) {
        for (StringSequence sequence : sequences) {
            if (sequence.containSequence(begin, end)) {
                return sequence.getSequence(begin, end);
            }
        }
        throw new IllegalStateException("No such sequence from \"" + begin + "\" to \"" + end + "\".");
    }

    public String getCode() throws ParseException {
        Class<?> leftType = leftParameter.getReturnType();
        String leftCode = leftParameter.getCode();
        if (leftParameter instanceof Operator
                && ((Operator) leftParameter).getPriority() < getPriority()) {
            leftCode = "(" + leftCode + ")";
        }
        String name = getName();
        Map<String, Class<?>> types = getParameterTypes();
        String rightCode = rightParameter.getCode();
        if (StringUtils.isFunction(name)) {
            name =  name.substring(1);
            if ("to".equals(name) 
                    && rightParameter instanceof Constant
                    && rightParameter.getReturnType() == String.class
                    && rightCode.length() > 2
                    && rightCode.startsWith("\"") && rightCode.endsWith("\"")) {
                return "((" + rightCode.substring(1, rightCode.length() - 1) + ")" + leftCode + ")";
            } else if ("class".equals(name)) {
                if (leftType.isPrimitive()) {
                    return leftType.getCanonicalName() + ".class";
                } else {
                    return leftCode + ".getClass()";
                }
            }
            if (Map.Entry.class.isAssignableFrom(leftType)
            		&& ("key".equals(name) || "value".equals(name))) {
            	String var = getGenericVariableName(leftParameter);
            	if (var != null) {
	            	Class<?> keyType = types.get(var + ":0"); // Map<K,V>第一个泛型
	            	Class<?> valueType = types.get(var + ":1"); // Map<K,V>第二个泛型
	                if ("key".equals(name) && keyType != null) {
	            		return "((" + keyType.getName() + ")" + leftCode + ".getKey(" + rightCode + "))";
	            	} else if ("value".equals(name) && valueType != null) {
	            		return "((" + valueType.getName() + ")" + leftCode + ".getValue(" + rightCode + "))";
	            	}
            	}
            }
            Class<?> rightType = rightParameter.getReturnType();
            if (Map.class.isAssignableFrom(leftType)
            		&& "get".equals(name)
            		&& String.class.equals(rightType)) {
            	String var = getGenericVariableName(leftParameter);
            	if (var != null) {
	                Class<?> type = types.get(var + ":1"); // Map<K,V>第二个泛型 
	                if (type != null) {
	                    return "((" + type.getName() + ")" + leftCode + ".get(" + rightCode + "))";
	                }
            	}
            }
            if (List.class.isAssignableFrom(leftType)
            		&& "get".equals(name)
            		&& int.class.equals(rightType)) {
            	String var = getGenericVariableName(leftParameter);
            	if (var != null) {
	                Class<?> type = types.get(var + ":0"); // List<T>第一个泛型
	                if (type != null) {
	                	return "((" + type.getName() + ")" + leftCode + ".get(" + rightCode + "))";
	                }
            	}
            }
            Class<?>[] rightTypes = rightParameter.getReturnTypes();
            Collection<Class<?>> functions = getFunctions();
            if (functions != null && functions.size() > 0) {
                Class<?>[] allTypes;
                String allCode;
                if (rightTypes == null || rightTypes.length == 0) {
                    allTypes = new Class<?>[] {leftType};
                    allCode = leftCode;
                } else {
                    allTypes = new Class<?>[rightTypes.length + 1];
                    allTypes[0] = leftType;
                    System.arraycopy(rightTypes, 0, allTypes, 1, rightTypes.length);
                    allCode = leftCode + ", " + rightCode;
                }
                for (Class<?> function : functions) {
                    try {
                        Method method = ClassUtils.searchMethod(function, name, allTypes);
                        if (! Object.class.equals(method.getDeclaringClass())) {
                        	if (Modifier.isStatic(method.getModifiers())) {
                            	return function.getName() + "." + method.getName() + "(" + allCode + ")";
                            }
                            return "_" + function.getName().replace('.', '_') + "." + method.getName() + "(" + allCode + ")";
                        }
                    } catch (NoSuchMethodException e) {
                    }
                }
            }
            Class<?> type = getReturnType();
            return "(" + leftCode + " == null ? (" + type.getCanonicalName() + ")" + ClassUtils.getInitCode(type) + " : " + getMethodName(leftType, name, rightTypes, leftCode, rightCode) + ")";
        } else if (name.equals("[")) {
            if (Map.class.isAssignableFrom(leftType)) {
                String var = getGenericVariableName(leftParameter);
                if (var != null) {
	                Class<?> t = types.get(var + ":1"); // Map<K,V>第二个泛型 
	                if (t != null) {
	                    return "((" + t.getCanonicalName() + ")" + leftCode + ".get(" + rightCode + "))";
	                }
                }
                return leftCode + ".get(" + rightCode + ")";
            }
            Class<?> rightType = rightParameter.getReturnType();
            if (List.class.isAssignableFrom(leftType)) {
                if (IntegerSequence.class.equals(rightType) || int[].class == rightType) {
                    return ClassUtils.class.getName() + ".subList(" + leftCode + ", " + rightCode + ")";
                } else if (int.class.equals(rightType)) {
                    if (leftParameter instanceof Variable) {
                        String var = ((Variable)leftParameter).getName();
                        Class<?> t = types.get(var + ":0"); // List<T>第一个泛型
                        if (t != null) {
                            return "((" + t.getCanonicalName() + ")" + leftCode + ".get(" + rightCode + "))";
                        }
                    }
                    return leftCode + ".get(" + rightCode + ")";
                } else {
                    throw new ParseException("The \"[]\" index type: " + rightType.getName() + " must be int!", getOffset());
                }
            } else if (leftType.isArray()) {
                if (IntegerSequence.class.equals(rightType) || int[].class == rightType) {
                    return ClassUtils.class.getName() + ".subArray(" + leftCode + ", " + rightCode + ")";
                } else if (int.class.equals(rightType)) {
                    return leftCode + "[" + rightCode + "]";
                } else {
                    throw new ParseException("The \"[]\" index type: " + rightType.getName() + " must be int!", getOffset());
                }
            }
            throw new ParseException("Unsuptorted \"[]\" for non-array type: " + leftType.getName(), getOffset());
        } else if (name.equals("..")) {
            if (leftType == int.class || leftType == Integer.class 
                    || leftType == short.class  || leftType == Short.class
                    || leftType == long.class || leftType == Long.class ) {
                return "new " + IntegerSequence.class.getName() + "(" + leftCode + ", " + rightCode + ")";
            } else if (leftType == char.class || leftType == Character.class) {
                return "new " + CharacterSequence.class.getName() + "(" + leftCode + ", " + rightCode + ")";
            } else if (leftType == String.class 
            			&& leftCode.length() >= 2 && leftCode.startsWith("\"") && leftCode.endsWith("\"")
            			&& rightCode.length() >= 2 && rightCode.startsWith("\"") && rightCode.endsWith("\"")) {
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
                return "new String[] {" + buf.toString() + "}";
            } else {
                throw new ParseException("The operator \"..\" unsupported parameter type " + leftType, getOffset());
            }
        } else if("==".equals(name) && ! "null".equals(leftCode) && ! "null".equals(rightCode)
                && ! leftType.isPrimitive() && ! rightParameter.getReturnType().isPrimitive()) {
            return leftCode + ".equals(" + rightCode + ")";
        } else if("!=".equals(name) && ! "null".equals(leftCode) && ! "null".equals(rightCode)
                && ! leftType.isPrimitive() && ! rightParameter.getReturnType().isPrimitive()) {
            return "(! " + leftCode + ".equals(" + rightCode + "))";
        } else if("&&".equals(name) || "||".equals(name)) {
            if (rightParameter instanceof Operator
                    && ((Operator) rightParameter).getPriority() < getPriority()) {
                rightCode = "(" + rightCode + ")";
            }
            leftCode = StringUtils.getConditionCode(leftType, leftCode);
            rightCode = StringUtils.getConditionCode(rightParameter.getReturnType(), rightCode);
            return leftCode + " " + name + " " + rightCode;
        } else if (name.equals("?")) {
            if (rightParameter instanceof Operator
                    && ((Operator) rightParameter).getPriority() < getPriority()) {
                rightCode = "(" + rightCode + ")";
            }
            leftCode = StringUtils.getConditionCode(leftType, leftCode);
            return leftCode + " " + name + " " + rightCode;
        } else if("|".equals(name) 
                && ! leftType.isPrimitive()
                && ! Number.class.isAssignableFrom(leftType)
                && ! Boolean.class.isAssignableFrom(leftType)) {
            return "(" + ClassUtils.class.getName() + ".isNotEmpty(" + leftParameter.getCode() + ") ? (" + leftCode + ") : (" + rightCode + "))";
        } else if (":".equals(name) 
                && ! (leftParameter instanceof BinaryOperator 
                        && "?".equals(((BinaryOperator)leftParameter).getName()))) {
            return "new " + MapEntry.class.getName() + "(" + leftCode + ", " + rightCode + ")";
        } else {
        	if ("lt".equals(name)) {
            	name = "<";
            } else if ("le".equals(name)) {
            	name = "<=";
            } else if ("gt".equals(name)) {
            	name = ">";
            } else if ("ge".equals(name)) {
            	name = ">=";
            }
            if (leftType != null && Comparable.class.isAssignableFrom(leftType)) {
                if (">".equals(name)) {
                    return leftCode + ".compareTo(" + rightCode + ") > 0";
                } else if (">=".equals(name)) {
                    return leftCode + ".compareTo(" + rightCode + ") >= 0";
                } else if ("<".equals(name)) {
                    return leftCode + ".compareTo(" + rightCode + ") < 0";
                } else if ("<=".equals(name)) {
                    return leftCode + ".compareTo(" + rightCode + ") <= 0";
                }
            }
            if ("+".equals(name)) {
            	Class<?> rightType = rightParameter.getReturnType();
            	if ((Collection.class.isAssignableFrom(leftType) || leftType.isArray()) 
		    					&& (Collection.class.isAssignableFrom(rightType) || rightType.isArray())
		    			|| Map.class.isAssignableFrom(leftType) && Map.class.isAssignableFrom(rightType)) {
		    		return CollectionUtils.class.getName() + ".merge(" + leftCode+ ", " + rightCode + ")";
		    	}
            	Class<?> type;
            	if (rightType.isPrimitive() && rightType != boolean.class) {
            		type = rightType;
            	} else if (leftType.isPrimitive() && leftType != boolean.class) {
            		type = leftType;
            	} else {
            		type = String.class;
            	}
            	if (type != String.class) {
	            	String typeName = type.getCanonicalName();
	        		typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
	            	if (! (leftType.isPrimitive() && leftType != boolean.class)) {
	            		leftCode = DefaultMethod.class.getName() + ".to" + typeName + "(" + leftCode + ")";
	            	}
	            	if (! (rightType.isPrimitive() && leftType != boolean.class)) {
	            		rightCode = DefaultMethod.class.getName() + ".to" + typeName + "(" + rightCode + ")";
	            	}
            	}
            } else if ("－".equals(name) || "＊".equals(name) || "／".equals(name) || "%".equals(name)) {
            	Class<?> rightType = rightParameter.getReturnType();
            	Class<?> type;
            	if (rightType.isPrimitive() && rightType != boolean.class) {
            		type = rightType;
            	} else if (leftType.isPrimitive() && leftType != boolean.class) {
            		type = leftType;
            	} else {
            		type = int.class;
            	}
            	String typeName = type.getCanonicalName();
        		typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
            	if (! (leftType.isPrimitive() && leftType != boolean.class)) {
            		leftCode = DefaultMethod.class.getName() + ".to" + typeName + "(" + leftCode + ")";
            	}
            	if (! (rightType.isPrimitive() && leftType != boolean.class)) {
            		rightCode = DefaultMethod.class.getName() + ".to" + typeName + "(" + rightCode + ")";
            	}
            }
            if (rightParameter instanceof Operator
                    && ((Operator) rightParameter).getPriority() < getPriority()) {
                rightCode = "(" + rightCode + ")";
            }
            return leftCode + " " + name + " " + rightCode;
        }
    }
    
    public Class<?> getReturnType() throws ParseException {
    	String name = getName();
        if (">".equals(name) || ">=".equals(name) 
                || "<".equals(name)|| "<=".equals(name)
                || "gt".equals(name) || "ge".equals(name)
                || "lt".equals(name) || "le".equals(name)
                || "==".equals(name)|| "!=".equals(name)
                || "&&".equals(name)|| "||".equals(name)) {
            return boolean.class;
        }
        Map<String, Class<?>> types = getParameterTypes();
    	Class<?> leftType = leftParameter.getReturnType();
        if (StringUtils.isFunction(name)) {
            name =  name.substring(1);
            if ("to".equals(name) 
                    && rightParameter instanceof Constant
                    && rightParameter.getReturnType() == String.class) {
                String rightCode = rightParameter.getCode();
                if(rightCode.length() > 2 && rightCode.startsWith("\"") && rightCode.endsWith("\"")) {
                    return ClassUtils.forName(getPackages(), rightCode.substring(1, rightCode.length() - 1));
                }
            } else if ("class".equals(name)) {
                return Class.class;
            }
            if (Map.Entry.class.isAssignableFrom(leftType)
            		&& ("key".equals(name) || "value".equals(name))) {
            	String var = getGenericVariableName(leftParameter);
            	if (var != null) {
	            	Class<?> keyType = types.get(var + ":0"); // Map<K,V>第一个泛型
	            	Class<?> valueType = types.get(var + ":1"); // Map<K,V>第二个泛型
	                if ("key".equals(name) && keyType != null) {
	            		return keyType;
	            	} else if ("value".equals(name) && valueType != null) {
	            		return valueType;
	            	}
            	}
            }
            Class<?> rightType = rightParameter.getReturnType();
            if (Map.class.isAssignableFrom(leftType)
            		&& "get".equals(name)
            		&& String.class.equals(rightType)) {
            	String var = getGenericVariableName(leftParameter);
            	if (var != null) {
	                Class<?> type = types.get(var + ":1"); // Map<K,V>第二个泛型 
	                if (type != null) {
	                    return type;
	                }
            	}
            }
            if (List.class.isAssignableFrom(leftType)
            		&& "get".equals(name)
            		&& int.class.equals(rightType)) {
            	String var = getGenericVariableName(leftParameter);
            	if (var != null) {
	                Class<?> type = types.get(var + ":0"); // List<T>第一个泛型
	                if (type != null) {
	                    return type;
	                }
            	}
            }
            Class<?>[] rightTypes = rightParameter.getReturnTypes();
            Collection<Class<?>> functions = getFunctions();
            if (functions != null && functions.size() > 0) {
                Class<?>[] allTypes;
                if (rightTypes == null || rightTypes.length == 0) {
                    if (leftType == null) {
                        allTypes = new Class<?>[0];
                    } else {
                        allTypes = new Class<?>[] {leftType};
                    }
                } else {
                    allTypes = new Class<?>[rightTypes.length + 1];
                    allTypes[0] = leftType;
                    System.arraycopy(rightTypes, 0, allTypes, 1, rightTypes.length);
                }
                for (Class<?> function : functions) {
                    try {
                        Method method = ClassUtils.searchMethod(function, name, allTypes);
                        if (Object.class.equals(method.getDeclaringClass())) {
                            break;
                        }
                        return method.getReturnType();
                    } catch (NoSuchMethodException e) {
                    }
                }
            }
            return getReturnType(leftType, name, rightTypes);
        } else if ("..".equals(name)) {
            if (leftType == int.class || leftType == Integer.class 
                    || leftType == short.class  || leftType == Short.class
                    || leftType == long.class || leftType == Long.class ) {
                return IntegerSequence.class;
            } else if (leftType == char.class || leftType == Character.class) {
                return CharacterSequence.class;
            } else if (leftType == String.class) {
                return StringSequence.class;
            } else {
                throw new ParseException("The operator \"..\" unsupported parameter type " + leftType, getOffset());
            }
        } else if ("[".equals(name)) {
            if (Map.class.isAssignableFrom(leftType)) {
            	String var = getGenericVariableName(leftParameter);
            	if (var != null) {
                    Class<?> t = types.get(var + ":1"); // Map<K,V>第二个泛型 
                    if (t != null) {
                        return t;
                    }
                }
                return Object.class;
            }
            Class<?> rightType = rightParameter.getReturnType();
            if (List.class.isAssignableFrom(leftType)) {
                if (IntegerSequence.class.equals(rightType) || int[].class == rightType) {
                    return List.class;
                } else if (int.class.equals(rightType)) {
                    String var = getGenericVariableName(leftParameter);
                    if (var != null) {
                        Class<?> t = types.get(var + ":0"); // List<T>第一个泛型
                        if (t != null) {
                            return t;
                        }
                    }
                    return Object.class;
                } else {
                    throw new ParseException("The \"[]\" index type: " + rightType.getName() + " must be int!", getOffset());
                }
            } else if (leftType.isArray()) {
                if (IntegerSequence.class.equals(rightType) || int[].class == rightType) {
                    return leftType;
                } else if (int.class.equals(rightType)) {
                    return leftType.getComponentType();
                } else {
                    throw new ParseException("The \"[]\" index type: " + rightType.getName() + " must be int!", getOffset());
                }
            }
            throw new ParseException("Unsuptorted \"[]\" for non-array type: " + leftType.getName(), getOffset());
        } else if ("?".equals(name)) {
            return rightParameter.getReturnType();
        } else if (":".equals(name) 
                && ! (leftParameter instanceof BinaryOperator 
                        && "?".equals(((BinaryOperator)leftParameter).getName()))) {
            return Map.Entry.class;
        } else if ("+".equals(name)) {
        	Class<?> rightType = rightParameter.getReturnType();
        	if ((Collection.class.isAssignableFrom(leftType) || leftType.isArray()) 
							&& (Collection.class.isAssignableFrom(rightType) || rightType.isArray())
					|| Map.class.isAssignableFrom(leftType) && Map.class.isAssignableFrom(rightType)) {
				return leftType;
			} else if (rightType.isPrimitive() && rightType != boolean.class) {
        		return rightType;
        	} else if (leftType.isPrimitive() && leftType != boolean.class) {
        		return leftType;
        	} else {
        		return String.class;
        	}
        } else if ("－".equals(name) || "＊".equals(name) || "／".equals(name) || "%".equals(name)) {
        	Class<?> rightType = rightParameter.getReturnType();
        	if (rightType.isPrimitive() && rightType != boolean.class) {
        		return rightType;
        	} else if (leftType.isPrimitive() && leftType != boolean.class) {
        		return leftType;
        	} else {
        		return int.class;
        	}
        } else {
            return leftType;
        }
    }

    public Class<?>[] getReturnTypes() throws ParseException {
        if (getName().equals(",")) {
            Class<?>[] leftTypes = leftParameter.getReturnTypes();
            Class<?>[] rightTypes = rightParameter.getReturnTypes();
            Class<?>[] types = new Class<?>[leftTypes.length + rightTypes.length];
            System.arraycopy(leftTypes, 0, types, 0, leftTypes.length);
            System.arraycopy(rightTypes, 0, types, leftTypes.length, rightTypes.length);
            return types;
        }
        return super.getReturnTypes();
    }

    private String getMethodName(Class<?> leftType, String name, Class<?>[] rightTypes, String leftCode, String rightCode) throws ParseException {
        if (leftType == null) {
            throw new ParseException("No such method " + name + "("
                                     + Arrays.toString(rightTypes) + ") in null class.", getOffset());
        }
        try {
            Method method = ClassUtils.searchMethod(leftType, name, rightTypes);
            return leftCode + "." + method.getName() + "(" + rightCode + ")";
        } catch (NoSuchMethodException e) {
            if (rightTypes != null && rightTypes.length > 0) {
                throw new ParseException("No such method " + name + "("
                        + Arrays.toString(rightTypes) + ") in class "
                        + leftType.getName(), getOffset());
            } else { // search property
                try {
                    String getter = "get" + name.substring(0, 1).toUpperCase()
                            + name.substring(1);
                    Method method = leftType.getMethod(getter,
                            new Class<?>[0]);
                    return leftCode + "." + method.getName() + "()";
                } catch (NoSuchMethodException e2) {
                    try {
                        String getter = "is"
                                + name.substring(0, 1).toUpperCase()
                                + name.substring(1);
                        Method method = leftType.getMethod(getter,
                                new Class<?>[0]);
                        return leftCode + "." + method.getName() + "()";
                    } catch (NoSuchMethodException e3) {
                        try {
                            Field field = leftType.getField(name);
                            return field.getName();
                        } catch (NoSuchFieldException e4) {
                            if (Map.class.isAssignableFrom(leftType)
                                    && (rightTypes == null || rightTypes.length == 0 
                                            || (rightTypes.length == 1 && rightTypes[0] == null))) {
                                Map<String, Class<?>> types = getParameterTypes();
                                String var = getGenericVariableName(leftParameter);
                            	if (var != null) {
                                    Class<?> t = types.get(var + ":1"); // Map<K,V>第二个泛型 
                                    if (t != null) {
                                        return "((" + t.getCanonicalName() + ")" + leftCode + ".get(\"" + name + "\"))";
                                    }
                                }
                                return leftCode + ".get(\"" + name + "\")";
                            }
                            throw new ParseException(
                                    "No such property "
                                            + name
                                            + " in class "
                                            + leftType.getName()
                                            + ", because no such method get"
                                            + name.substring(0, 1)
                                                    .toUpperCase()
                                            + name.substring(1)
                                            + "() or method is"
                                            + name.substring(0, 1)
                                                    .toUpperCase()
                                            + name.substring(1)
                                            + "() or method " + name
                                            + "() or filed " + name, getOffset());
                        }
                    }
                }
            }
        }
    }
    
    private Class<?> getReturnType(Class<?> leftType, String name, Class<?>[] rightTypes) throws ParseException {
        if (leftType == null) {
            return Void.class;
        }
        try {
            Method method = ClassUtils.searchMethod(leftType, name, rightTypes);
            return method.getReturnType();
        } catch (NoSuchMethodException e) {
            if (rightTypes != null && rightTypes.length > 0) {
                throw new ParseException("No such method " + name + "("
                        + Arrays.toString(rightTypes) + ") in class "
                        + leftType.getName(), getOffset());
            } else { // search property
                try {
                    String getter = "get" + name.substring(0, 1).toUpperCase()
                            + name.substring(1);
                    Method method = leftType.getMethod(getter,
                            new Class<?>[0]);
                    return method.getReturnType();
                } catch (NoSuchMethodException e2) {
                    try {
                        String getter = "is"
                                + name.substring(0, 1).toUpperCase()
                                + name.substring(1);
                        Method method = leftType.getMethod(getter,
                                new Class<?>[0]);
                        return method.getReturnType();
                    } catch (NoSuchMethodException e3) {
                        try {
                            Field field = leftType.getField(name);
                            return field.getType();
                        } catch (NoSuchFieldException e4) {
                            if (Map.class.isAssignableFrom(leftType)
                                    && (rightTypes == null || rightTypes.length == 0 
                                            || (rightTypes.length == 1 && rightTypes[0] == null))) {
                                Map<String, Class<?>> types = getParameterTypes();
                                String var = getGenericVariableName(leftParameter);
                            	if (var != null) {
                                    Class<?> t = types.get(var + ":1"); // Map<K,V>第二个泛型 
                                    if (t != null) {
                                        return t;
                                    }
                                }
                                return Object.class;
                            }
                            throw new ParseException(
                                    "No such property "
                                            + name
                                            + " in class "
                                            + leftType.getName()
                                            + ", because no such method get"
                                            + name.substring(0, 1)
                                                    .toUpperCase()
                                            + name.substring(1)
                                            + "() or method is"
                                            + name.substring(0, 1)
                                                    .toUpperCase()
                                            + name.substring(1)
                                            + "() or method " + name
                                            + "() or filed " + name, getOffset());
                        }
                    }
                }
            }
        }
    }

}
