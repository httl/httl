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
package httl.spi.translators.templates;

import httl.Context;
import httl.Node;
import httl.Template;
import httl.ast.AddOperator;
import httl.ast.AndOperator;
import httl.ast.ArrayOperator;
import httl.ast.AstVisitor;
import httl.ast.BinaryOperator;
import httl.ast.BitAndOperator;
import httl.ast.BitNotOperator;
import httl.ast.BitOrOperator;
import httl.ast.BitXorOperator;
import httl.ast.BreakDirective;
import httl.ast.CastOperator;
import httl.ast.ConditionOperator;
import httl.ast.Constant;
import httl.ast.DivOperator;
import httl.ast.ElseDirective;
import httl.ast.EntryOperator;
import httl.ast.EqualsOperator;
import httl.ast.ForDirective;
import httl.ast.GreaterEqualsOperator;
import httl.ast.GreaterOperator;
import httl.ast.IfDirective;
import httl.ast.IndexOperator;
import httl.ast.InstanceofOperator;
import httl.ast.LeftShiftOperator;
import httl.ast.LessEqualsOperator;
import httl.ast.LessOperator;
import httl.ast.ListOperator;
import httl.ast.MacroDirective;
import httl.ast.MethodOperator;
import httl.ast.ModOperator;
import httl.ast.MulOperator;
import httl.ast.NegativeOperator;
import httl.ast.NewOperator;
import httl.ast.NotEqualsOperator;
import httl.ast.NotOperator;
import httl.ast.OrOperator;
import httl.ast.PositiveOperator;
import httl.ast.RightShiftOperator;
import httl.ast.SequenceOperator;
import httl.ast.SetDirective;
import httl.ast.StaticMethodOperator;
import httl.ast.SubOperator;
import httl.ast.Text;
import httl.ast.UnsignShiftOperator;
import httl.ast.ValueDirective;
import httl.ast.Variable;
import httl.internal.util.ClassUtils;
import httl.internal.util.CollectionUtils;
import httl.internal.util.Condition;
import httl.internal.util.LinkedStack;
import httl.internal.util.MapEntry;
import httl.internal.util.Status;
import httl.internal.util.StringSequence;
import httl.internal.util.StringUtils;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Switcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class InterpretedVisitor extends AstVisitor {

	private Filter currentTextFilter;

	private Filter textFilter;

	private Filter valueFilter;

	private Formatter<Object> formatter;

	private Switcher<Filter> textFilterSwitcher;

	private Switcher<Filter> valueFilterSwitcher;

	private Switcher<Formatter<Object>> formatterSwitcher;

	private String filterVariable = "filter";

	private String formatterVariable = "formatter";

	private String[] forVariable = new String[] { "for" };

	private String ifVariable = "if";

	private String breakVariable = "break";

	private String outputEncoding = "UTF-8";

	private Map<Class<?>, Object> importMethods;

	private List<StringSequence> importSequences;

	private Map<String, Template> importMacros;

	private String[] importPackages;

	private Template template;

	private Object out;

	private String preText;

	private final LinkedStack<Object> parameterStack = new LinkedStack<Object>();

	public void setTextFilterSwitcher(Switcher<Filter> textFilterSwitcher) {
		this.textFilterSwitcher = textFilterSwitcher;
	}

	public void setValueFilterSwitcher(Switcher<Filter> valueFilterSwitcher) {
		this.valueFilterSwitcher = valueFilterSwitcher;
	}

	public void setFormatterSwitcher(Switcher<Formatter<Object>> formatterSwitcher) {
		this.formatterSwitcher = formatterSwitcher;
	}

	public void setFilterVariable(String filterVariable) {
		this.filterVariable = filterVariable;
	}

	public void setFormatterVariable(String formatterVariable) {
		this.formatterVariable = formatterVariable;
	}

	public void setImportMacros(Map<String, Template> importMacros) {
		this.importMacros = importMacros;
	}

	public void setImportMethods(Map<Class<?>, Object> importMethods) {
		this.importMethods = importMethods;
	}

	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	public void setImportSequences(List<StringSequence> importSequences) {
		this.importSequences = importSequences;
	}

	private List<String> getSequence(String begin, String end) {
		if (importSequences != null) {
			for (StringSequence sequence : importSequences) {
				if (sequence.containSequence(begin, end)) {
					return sequence.getSequence(begin, end);
				}
			}
		}
		throw new IllegalStateException("No such sequence from \"" + begin + "\" to \"" + end + "\".");
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

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
		this.currentTextFilter = textFilter;
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

	private Object popExpressionResult(int offset) throws IOException, ParseException {
		Object result = parameterStack.pop();
		if (! parameterStack.isEmpty()) {
			throw new ParseException("The directive expression error.", offset);
		}
		return result;
	}

	@Override
	public void visit(Text node) throws IOException, ParseException {
		try {
			String text = node.getContent();
			if (textFilterSwitcher != null || valueFilterSwitcher != null || formatterSwitcher != null) {
				Set<String> locations = new HashSet<String>();
				List<String> textLocations = textFilterSwitcher == null ? null : textFilterSwitcher.locations();
				if (textLocations != null) {
					locations.addAll(textLocations);
				}
				List<String> valueLocations = valueFilterSwitcher == null ? null : valueFilterSwitcher.locations();
				if (valueLocations != null) {
					locations.addAll(valueLocations);
				}
				List<String> formatterLocations = formatterSwitcher == null ? null : formatterSwitcher.locations();
				if (formatterLocations != null) {
					locations.addAll(formatterLocations);
				}
				if (locations != null && locations.size() > 0) {
					Map<Integer, Set<String>> switchesd = new TreeMap<Integer, Set<String>>();
					for (String location : locations) {
						int i = -1;
						while ((i = text.indexOf(location, i + 1)) >= 0) {
							Integer key = Integer.valueOf(i);
							Set<String> values = switchesd.get(key);
							if (values == null) {
								values = new HashSet<String>();
								switchesd.put(key, values);
							}
							values.add(location);
						}
					}
					if (switchesd.size() > 0) {
						int begin = 0;
						for (Map.Entry<Integer, Set<String>> entry : switchesd.entrySet()) {
							int end = entry.getKey();
							String part = text.substring(begin, end);
							if (StringUtils.isNotEmpty(part)) {
								part = currentTextFilter == null ? part : currentTextFilter.filter(preText, part);
								preText = part;
								if (out instanceof Writer) {
									((Writer) out).write(part);
								} else if (out instanceof OutputStream) {
									((OutputStream) out).write(part.getBytes(outputEncoding));
								}
							}
							begin = end;
							for (String location : entry.getValue()) {
								if (textLocations != null && textLocations.contains(location)) {
									currentTextFilter = textFilterSwitcher.switchover(location, textFilter);
								}
								if (valueLocations != null && valueLocations.contains(location)) {
									Context.getContext().put(filterVariable, valueFilterSwitcher.switchover(location, valueFilter));
								}
								if (formatterLocations != null && formatterLocations.contains(location)) {
									Context.getContext().put(formatterVariable, formatterSwitcher.switchover(location, formatter));
								}
							}
						}
						if (begin > 0) {
							text = text.substring(begin);
						}
					}
				}
			}
			if (StringUtils.isNotEmpty(text)) {
				text = currentTextFilter == null ? text : currentTextFilter.filter(preText, text);
				preText = text;
				if (out instanceof Writer) {
					((Writer) out).write(text);
				} else if (out instanceof OutputStream) {
					((OutputStream) out).write(text.getBytes(outputEncoding));
				}
			}
		} catch (IOException e) {
			throw new ParseException(e.getMessage(), node.getOffset());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(ValueDirective node) throws IOException, ParseException {
		Object result = popExpressionResult(node.getOffset());
		if (result instanceof Template) {
			((Template) result).render(out);
		} else {
			Formatter<Object> format = (Formatter<Object>) Context.getContext().get(formatterVariable, formatter);
			String text = format == null ? StringUtils.toString(result) : format.toString(null, result);
			Filter filter = (Filter) Context.getContext().get(filterVariable, valueFilter);
			if (! node.isNoFilter() && filter != null) {
				text =  filter.filter(node.getExpression().toString(), text);
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
				throw new ParseException(e.getMessage(), node.getOffset());
			}
		}
	}

	@Override
	public void visit(SetDirective node) throws IOException, ParseException {
		if (node.getExpression() != null) {
			Object result = popExpressionResult(node.getOffset());
			if (node.isExport() && Context.getContext().getParent() != null) {
				Context.getContext().getParent().put(node.getName(), result);
			} else {
				Context.getContext().put(node.getName(), result);
			}
		}
	}

	@Override
	public void visit(BreakDirective node) throws IOException, ParseException {
		boolean result = true;
		if (node.getExpression() != null) {
			result = ClassUtils.isTrue(popExpressionResult(node.getOffset()));
		}
		if (result) {
			Context.getContext().put(breakVariable, true);
		}
	}

	@Override
	public boolean visit(IfDirective node) throws IOException, ParseException {
		boolean result = ClassUtils.isTrue(popExpressionResult(node.getOffset()));
		Context.getContext().put(ifVariable, result);
		return result;
	}

	@Override
	public boolean visit(ElseDirective node) throws IOException, ParseException {
		boolean result = true;
		if (node.getExpression() != null) {
			result = ClassUtils.isTrue(popExpressionResult(node.getOffset()));
		}
		result = result && ! ClassUtils.isTrue(Context.getContext().get(ifVariable));
		if (result) {
			Context.getContext().put(ifVariable, true);
		}
		return result;
	}

	@Override
	public boolean visit(ForDirective node) throws IOException, ParseException {
		Object data = popExpressionResult(node.getOffset());
		boolean result = ClassUtils.isTrue(data);
		Context.getContext().put(ifVariable, result);
		Iterator<?> iterator = CollectionUtils.toIterator(data);
		Status status = new Status((Status) Context.getContext().get(forVariable[0]), data);
		for (String var : forVariable) {
			Context.getContext().put(var, status);
		}
		loop: while (iterator.hasNext()) {
			Object item = iterator.next();
			Context.getContext().put(node.getName(), item);
			for (Node child : node.getChildren()) {
				child.accept(this);
				if (ClassUtils.isTrue(Context.getContext().get(breakVariable))) {
					Context.getContext().remove(breakVariable);
					break loop;
				}
			}
			status.increment();
		}
		for (String var : forVariable) {
			Context.getContext().put(var, status.getParent());
		}
		return false;
	}

	@Override
	public boolean visit(MacroDirective node) throws IOException, ParseException {
		return false;
	}

	@Override
	public boolean visit(Template node) throws IOException, ParseException {
		for (Node child : node.getChildren()) {
			child.accept(this);
			if (ClassUtils.isTrue(Context.getContext().get(breakVariable))) {
				Context.getContext().remove(breakVariable);
				break;
			}
		}
		return false;
	}

	@Override
	public void visit(Constant node) throws IOException, ParseException {
		parameterStack.push(node.getValue());
	}

	@Override
	public void visit(Variable node) throws IOException, ParseException {
		parameterStack.push(Context.getContext().get(node.getName()));
	}

	public void visit(CastOperator node) throws IOException, ParseException {
		Object parameter = parameterStack.pop();
		Object result = parameter;
		parameterStack.push(result);
	}

	@Override
	public void visit(PositiveOperator node) throws IOException, ParseException {
		Object parameter = parameterStack.pop();
		Object result = parameter;;
		parameterStack.push(result);
	}

	@Override
	public void visit(NegativeOperator node) throws IOException, ParseException {
		Object parameter = parameterStack.pop();
		Object result = null;
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
			throw new ParseException("The unary operator \"" + node.getName() +  "\" unsupported parameter type " + parameter.getClass(), node.getOffset());
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(NotOperator node) throws IOException, ParseException {
		Object parameter = parameterStack.pop();
		Object result = ! ClassUtils.isTrue(parameter);
		parameterStack.push(result);
	}

	@Override
	public void visit(BitNotOperator node) throws IOException, ParseException {
		Object parameter = parameterStack.pop();
		Object result = null;
		if (parameter instanceof Integer) {
			result = ~ ((Integer) parameter).intValue();
		} else if (parameter instanceof Long) {
			result = ~ ((Long) parameter).longValue();
		} else if (parameter instanceof Short) {
			result = ~ ((Short) parameter).shortValue();
		} else if (parameter instanceof Byte) {
			result = ~ ((Byte) parameter).byteValue();
		} else {
			throw new ParseException("The unary operator \"" + node.getName() +  "\" unsupported parameter type " + parameter.getClass(), node.getOffset());
		}
		parameterStack.push(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(ListOperator node) throws IOException, ParseException {
		Object parameter = parameterStack.pop();
		Object result = null;
		if (parameter instanceof Object[]) {
			Object[] array = (Object[]) parameter;
			Class<?> cls = null;
			for (Object obj : array) {
				if (obj != null) {
					if (cls == null) {
						cls = obj.getClass();
					} else if (cls != obj.getClass()) {
						cls = Object.class;
						break;
					}
				}
			}
			if (Map.Entry.class.isAssignableFrom(cls)) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (Object obj : array) {
					if (obj != null) {
						Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) obj;
						map.put(entry.getKey(), entry.getValue());
					}
				}
				result = map;
			} else if (cls != null && cls != Object.class) {
				cls = ClassUtils.getUnboxedClass(cls);
				Object newArray = Array.newInstance(cls, array.length);
				for (int i = 0; i < array.length; i ++) {
					Object obj = array[i];
					if (obj != null) {
						Array.set(newArray, i, obj);
					}
				}
				result = newArray;
			} else {
				result = array;
			}
		} else if (parameter instanceof Map.Entry) {
			Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) parameter;
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put(entry.getKey(), entry.getValue());
			result = map;
		} else {
			result = new Object[] { parameter };
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(NewOperator node) throws IOException, ParseException {
		Object parameter = parameterStack.pop();
		Object result = null;
		String name = node.getName();
		Class<?> cls = ClassUtils.forName(importPackages, name);
		Object[] args;
		if (parameter == null 
				&& node.getParameter() instanceof Constant
				&& ((Constant) node.getParameter()).isBoxed()) {
			args = new Object[0];
		} else if (parameter instanceof Object[]) {
			args = (Object[]) parameter;
		} else {
			args = new Object[] { parameter };
		}
		Class<?>[] types = new Class<?>[args.length];
		for (int i = 0; i < args.length; i ++) {
			types[i] = args[i] == null ? null : ClassUtils.getUnboxedClass(args[i].getClass());
		}
		try {
			result = cls.getConstructor(types).newInstance(args);
		} catch (NoSuchMethodException e) {
			throw new ParseException("No such constructor " + ClassUtils.getMethodFullName(name, types) + ", cause: " + e.getMessage(), node.getOffset());
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				e = ((InvocationTargetException) e).getTargetException();
			}
			throw new ParseException("Failed to create " + ClassUtils.getMethodFullName(name, types) + ", cause: " + e.getMessage(), node.getOffset());
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(StaticMethodOperator node) throws IOException, ParseException {
		Object parameter = parameterStack.pop();
		Object result = null;
		String name = node.getName();
		String filteredName = ClassUtils.filterJavaKeyword(name);
		Object[] args;
		if (parameter == null 
				&& node.getParameter() instanceof Constant
				&& ((Constant) node.getParameter()).isBoxed()) {
			args = new Object[0];
		} else if (parameter instanceof Object[]) {
			args = (Object[]) parameter;
		} else {
			args = new Object[] { parameter };
		}
		Class<?>[] types = new Class<?>[args.length];
		for (int i = 0; i < args.length; i ++) {
			types[i] = args[i] == null ? null : args[i].getClass();
		}
		boolean found = false;
		if (importMethods != null && importMethods.size() > 0) {
			for (Map.Entry<Class<?>, Object> entry : importMethods.entrySet()) {
				Class<?> function = entry.getKey();
				try {
					Method method = ClassUtils.searchMethod(function, filteredName, types, true);
					if (! Object.class.equals(method.getDeclaringClass())) {
						Class<?> type = method.getReturnType();
						if (type == void.class) {
							throw new ParseException("Can not call void method " + method.getName() + " in class " + function.getName(), node.getOffset());
						}
						if (Modifier.isStatic(method.getModifiers())) {
							result = method.invoke(null, args);
						} else {
							result = method.invoke(entry.getValue(), args);
						}
						found = true;
						break;
					}
				} catch (NoSuchMethodException e) {
				} catch (Exception e) {
					throw new ParseException("Failed to invoke method " + ClassUtils.getMethodFullName(filteredName, types) + " in class " + function.getCanonicalName() + ", cause: " + ClassUtils.dumpException(e), node.getOffset());
				}
			}
		}
		if (! found) {
			Template macro;
			Object value = Context.getContext().get(name);
			if (value instanceof Template) {
				macro = (Template) value;
			} else {
				macro = template.getMacros().get(name);
				if (macro == null && importMacros != null) {
					macro = importMacros.get(name);
				}
			}
			if (macro != null) {
				result = macro.evaluate(args);
			} else {
				throw new ParseException("No such macro \"" + filteredName + "\" or import method " + ClassUtils.getMethodFullName(filteredName, types) + ".", node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(AddOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
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
			} else if (leftParameter instanceof Map && rightParameter instanceof Map) {
				result = CollectionUtils.merge((Map<Object, Object>) leftParameter, (Map<Object, Object>) rightParameter);
			} else if (leftParameter instanceof Collection) {
				if (rightParameter instanceof Collection) {
					result = CollectionUtils.merge((Collection<Object>) leftParameter, (Collection<Object>) rightParameter);
				} else if (rightParameter instanceof Object[]) {
					result = CollectionUtils.merge((Collection<Object>) leftParameter, (Object[]) rightParameter);
				} else {
					result = CollectionUtils.merge((Collection<Object>) leftParameter, Arrays.asList(rightParameter));
				}
			} else if (leftParameter instanceof Object[]) {
				if (rightParameter instanceof Collection) {
					result = CollectionUtils.merge((Object[]) leftParameter, (Collection<Object>) rightParameter);
				} else if (rightParameter instanceof Object[]) {
					result = CollectionUtils.merge((Object[]) leftParameter, (Object[]) rightParameter);
				} else {
					result = CollectionUtils.merge((Object[]) leftParameter, Arrays.asList(rightParameter));
				}
			} else {
				result = String.valueOf(leftParameter) + String.valueOf(rightParameter);
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(SubOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
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
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(MulOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
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
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(DivOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
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
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(ModOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
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
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(EqualsOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			result = leftParameter.equals(rightParameter);
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(NotEqualsOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			result = ! leftParameter.equals(rightParameter);
		}
		parameterStack.push(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(GreaterOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Comparable<?>) {
				result = ((Comparable<Object>) leftParameter).compareTo(rightParameter) > 0;
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(GreaterEqualsOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Comparable<?>) {
				result = ((Comparable<Object>) leftParameter).compareTo(rightParameter) >= 0;
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(LessOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Comparable<?>) {
				result = ((Comparable<Object>) leftParameter).compareTo(rightParameter) < 0;
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(LessEqualsOperator node) throws IOException,
			ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Comparable<?>) {
				result = ((Comparable<Object>) leftParameter).compareTo(rightParameter) <= 0;
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(AndOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			result = ClassUtils.isTrue(leftParameter) && ClassUtils.isTrue(rightParameter);
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(OrOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (ClassUtils.isTrue(leftParameter)) {
				result = leftParameter;
			} else {
				result = rightParameter;
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(BitAndOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Integer && rightParameter instanceof Number) {
				result = ((Number) leftParameter).intValue() & ((Number) rightParameter).intValue();
			} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
				result = ((Number) leftParameter).longValue() & ((Number) rightParameter).longValue();
			} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
				result = ((Number) leftParameter).shortValue() & ((Number) rightParameter).shortValue();
			} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
				result = ((Number) leftParameter).byteValue() & ((Number) rightParameter).byteValue();
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(BitOrOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Integer && rightParameter instanceof Number) {
				result = ((Number) leftParameter).intValue() | ((Number) rightParameter).intValue();
			} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
				result = ((Number) leftParameter).longValue() | ((Number) rightParameter).longValue();
			}  else if (leftParameter instanceof Short && rightParameter instanceof Number) {
				result = ((Number) leftParameter).shortValue() | ((Number) rightParameter).shortValue();
			} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
				result = ((Number) leftParameter).byteValue() | ((Number) rightParameter).byteValue();
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(BitXorOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Integer && rightParameter instanceof Number) {
				result = ((Number) leftParameter).intValue() ^ ((Number) rightParameter).intValue();
			} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
				result = ((Number) leftParameter).longValue() ^ ((Number) rightParameter).longValue();
			} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
				result = ((Number) leftParameter).shortValue() ^ ((Number) rightParameter).shortValue();
			} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
				result = ((Number) leftParameter).byteValue() ^ ((Number) rightParameter).byteValue();
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(RightShiftOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Integer && rightParameter instanceof Number) {
				result = ((Number) leftParameter).intValue() >> ((Number) rightParameter).intValue();
			} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
				result = ((Number) leftParameter).longValue() >> ((Number) rightParameter).longValue();
			} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
				result = ((Number) leftParameter).shortValue() >> ((Number) rightParameter).shortValue();
			} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
				result = ((Number) leftParameter).byteValue() >> ((Number) rightParameter).byteValue();
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(LeftShiftOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Integer && rightParameter instanceof Number) {
				result = ((Number) leftParameter).intValue() << ((Number) rightParameter).intValue();
			} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
				result = ((Number) leftParameter).longValue() << ((Number) rightParameter).longValue();
			} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
				result = ((Number) leftParameter).shortValue() << ((Number) rightParameter).shortValue();
			} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
				result = ((Number) leftParameter).byteValue() << ((Number) rightParameter).byteValue();
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(UnsignShiftOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Integer && rightParameter instanceof Number) {
				result = ((Number) leftParameter).intValue() >>> ((Number) rightParameter).intValue();
			} else if (leftParameter instanceof Long && rightParameter instanceof Number) {
				result = ((Number) leftParameter).longValue() >>> ((Number) rightParameter).longValue();
			} else if (leftParameter instanceof Short && rightParameter instanceof Number) {
				result = ((Number) leftParameter).shortValue() >>> ((Number) rightParameter).shortValue();
			} else if (leftParameter instanceof Byte && rightParameter instanceof Number) {
				result = ((Number) leftParameter).byteValue() >>> ((Number) rightParameter).byteValue();
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(ArrayOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Object[]
					&& node.getLeftParameter() instanceof BinaryOperator
					&& ",".equals(((BinaryOperator) node.getLeftParameter()).getName())) {
				Object[] leftArray = (Object[]) leftParameter;
				Object[] array = new Object[leftArray.length + 1];
				System.arraycopy(leftArray, 0, array, 0, leftArray.length);
				array[leftArray.length] = rightParameter;
				result = array;
			} else {
				Object[] array = new Object[2];
				array[0] = leftParameter;
				array[1] = rightParameter;
				result = array;
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(ConditionOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			result = new Condition(ClassUtils.isTrue(leftParameter), rightParameter);
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(EntryOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Condition) {
				Condition condition = (Condition) leftParameter;
				result = condition.isStatus() ? condition.getValue() : rightParameter;
			} else {
				result = new MapEntry<Object, Object>(leftParameter, rightParameter);
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(InstanceofOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = false;
		if (leftParameter != null) {
			if (rightParameter instanceof Class<?>) {
				result = ((Class<?>) rightParameter).isInstance(leftParameter);
			} else if (rightParameter instanceof String) {
				result = ClassUtils.forName(((String) rightParameter)).isInstance(leftParameter);
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(IndexOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (rightParameter instanceof Number) {
				if (leftParameter instanceof List) {
					result = ((List<Object>) leftParameter).get(((Number) rightParameter).intValue());
				} else if (leftParameter instanceof Object[]) {
					result = ((Object[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof int[]) {
					result = ((int[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof long[]) {
					result = ((long[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof float[]) {
					result = ((float[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof double[]) {
					result = ((double[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof short[]) {
					result = ((short[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof byte[]) {
					result = ((byte[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof char[]) {
					result = ((char[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof boolean[]) {
					result = ((boolean[])leftParameter)[((Number) rightParameter).intValue()];
				} else if (leftParameter instanceof char[]) {
					result = ((char[])leftParameter)[((Number) rightParameter).intValue()];
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
				}
			} else if (rightParameter instanceof int[] || (rightParameter instanceof Object[]
					&& CollectionUtils.isIntegerArray((Object[]) rightParameter))) {
				int[] indexs = rightParameter instanceof int[] ? (int[]) rightParameter : CollectionUtils.toIntegerArray((Object[]) rightParameter);
				if (leftParameter instanceof List) {
					result = CollectionUtils.subList((List<Object>) leftParameter, indexs);
				} else if (leftParameter instanceof Object[]) {
					result = CollectionUtils.subArray((Object[]) leftParameter, indexs);
				} else if (leftParameter instanceof int[]) {
					result = CollectionUtils.subArray((int[]) leftParameter, indexs);
				} else if (leftParameter instanceof long[]) {
					result = CollectionUtils.subArray((long[]) leftParameter, indexs);
				} else if (leftParameter instanceof float[]) {
					result = CollectionUtils.subArray((float[]) leftParameter, indexs);
				} else if (leftParameter instanceof double[]) {
					result = CollectionUtils.subArray((double[]) leftParameter, indexs);
				} else if (leftParameter instanceof short[]) {
					result = CollectionUtils.subArray((short[]) leftParameter, indexs);
				} else if (leftParameter instanceof byte[]) {
					result = CollectionUtils.subArray((byte[]) leftParameter, indexs);
				} else if (leftParameter instanceof char[]) {
					result = CollectionUtils.subArray((char[]) leftParameter, indexs);
				} else if (leftParameter instanceof boolean[]) {
					result = CollectionUtils.subArray((boolean[]) leftParameter, indexs);
				} else if (leftParameter instanceof char[]) {
					result = CollectionUtils.subArray((char[]) leftParameter, indexs);
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
				}
			} else if (rightParameter instanceof String) {
				if (leftParameter instanceof Map) {
					result = ((Map<Object, Object>) leftParameter).get((String) rightParameter);
				} else if (StringUtils.isNamed((String) rightParameter)) {
					try {
						result = ClassUtils.searchProperty(leftParameter, (String) rightParameter);
					} catch (Exception e) {
						throw new ParseException(e.getMessage(), node.getOffset());
					}
				} else {
					throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
				}
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(SequenceOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			if (leftParameter instanceof Integer && rightParameter instanceof Integer) {
				result = CollectionUtils.createSequence(((Integer) leftParameter).intValue(), ((Integer) rightParameter).intValue());
			} else if (leftParameter instanceof Long && rightParameter instanceof Long) {
				result = CollectionUtils.createSequence(((Long) leftParameter).longValue(), ((Long) rightParameter).longValue());
			} else if (leftParameter instanceof Float && rightParameter instanceof Float) {
				result = CollectionUtils.createSequence(((Float) leftParameter).floatValue(), ((Float) rightParameter).floatValue());
			} else if (leftParameter instanceof Double && rightParameter instanceof Double) {
				result = CollectionUtils.createSequence(((Double) leftParameter).doubleValue(), ((Double) rightParameter).doubleValue());
			} else if (leftParameter instanceof Short && rightParameter instanceof Short) {
				result = CollectionUtils.createSequence(((Short) leftParameter).shortValue(), ((Short) rightParameter).shortValue());
			} else if (leftParameter instanceof Byte && rightParameter instanceof Byte) {
				result = CollectionUtils.createSequence(((Byte) leftParameter).byteValue(), ((Byte) rightParameter).byteValue());
			} else if (leftParameter instanceof Character && rightParameter instanceof Character) {
				result = CollectionUtils.createSequence(((Character) leftParameter).charValue(), ((Character) rightParameter).charValue());
			} else if (leftParameter instanceof String && rightParameter instanceof String) {
				result = getSequence((String) leftParameter, (String) rightParameter);
			} else {
				throw new ParseException("The binary operator \"" + node.getName() +  "\" unsupported parameter type " + leftParameter.getClass().getName() + ", " + rightParameter.getClass().getName(), node.getOffset());
			}
		}
		parameterStack.push(result);
	}

	@Override
	public void visit(MethodOperator node) throws IOException, ParseException {
		Object rightParameter = parameterStack.pop();
		Object leftParameter = parameterStack.pop();
		Object result = null;
		if (leftParameter != null) {
			String name = node.getName();
			Class<?> leftClass = leftParameter.getClass();
			if ("to".equals(name) && rightParameter instanceof String) {
				result = leftParameter;
			} else if ("class".equals(name)) {
				if (node.getLeftParameter() instanceof Constant
						&& ! ((Constant) node.getLeftParameter()).isBoxed()) {
					result = ClassUtils.getUnboxedClass(leftClass);
				} else {
					result = leftClass;
				}
			} else {
				name = ClassUtils.filterJavaKeyword(name);
				Object[] args;
				if (rightParameter == null) {
					args = new Object[0];
				} else if (rightParameter instanceof Object[]) {
					args = (Object[]) rightParameter;
				} else {
					args = new Object[] { rightParameter };
				}
				result = null;
				boolean found = false;
				if (importMethods != null && importMethods.size() > 0) {
					Object[] staticArgs = new Object[args.length + 1];
					staticArgs[0] = leftParameter;
					System.arraycopy(args, 0, staticArgs, 1, args.length);
					Class<?>[] types = new Class<?>[staticArgs.length];
					for (int i = 0; i < staticArgs.length; i ++) {
						types[i] = staticArgs[i] == null ? null : staticArgs[i].getClass();
					}
					for (Map.Entry<Class<?>, Object> entry : importMethods.entrySet()) {
						Class<?> function = entry.getKey();
						try {
							Method method = ClassUtils.searchMethod(function, name, types, true);
							if (Object.class.equals(method.getDeclaringClass())) {
								break;
							}
							Class<?> type = method.getReturnType();
							if (type == void.class) {
								throw new ParseException("Can not call void method " + method.getName() + " in class " + function.getName(), node.getOffset());
							}
							if (Modifier.isStatic(method.getModifiers())) {
								result = method.invoke(null, staticArgs);
							} else {
								result = method.invoke(entry.getValue(), staticArgs);
							}
							found = true;
							break;
						} catch (NoSuchMethodException e) {
						} catch (Exception e) {
							throw new ParseException("Failed to invoke method " + ClassUtils.getMethodFullName(name, types) + " in class " + function.getCanonicalName() + ", cause: " + e.getMessage(), node.getOffset());
						}
					}
				}
				if (! found) {
					try {
						Class<?>[] types = new Class<?>[args.length];
						for (int i = 0; i < args.length; i ++) {
							types[i] = args[i] == null ? null : args[i].getClass();
						}
						try {
							Method method = ClassUtils.searchMethod(leftClass, name, types, true);
							if (! method.isAccessible()) {
								method.setAccessible(true);
							}
							result = method.invoke(leftParameter, args);
							found = true;
						} catch (NoSuchMethodException e) {
							if (args.length == 0) {
								try {
									result = ClassUtils.searchProperty(leftParameter, name);
									found = true;
								} catch (NoSuchFieldException e2) {
								}
							}
							if (! found) {
								if (leftParameter instanceof Template) {
									Template macro = ((Template) leftParameter).getMacros().get(name);
									if (macro != null) {
										result = macro.evaluate(args);
									} else {
										throw new ParseException("No such macro or method " + name + " in " + leftParameter.getClass().getCanonicalName(), node.getOffset());
									}
								} else if (leftParameter instanceof Class) {
									Class<?> function = (Class<?>) leftParameter;
									Method method = ClassUtils.searchMethod(function, name, types, true);
									Class<?> type = method.getReturnType();
									if (type == void.class) {
										throw new ParseException("Can not call void method " + method.getName() + " in class " + function.getName(), node.getOffset());
									}
									if (! Modifier.isStatic(method.getModifiers())) {
										throw new ParseException("Can not call non-static method " + method.getName() + " in class " + function.getName(), node.getOffset());
									}
									result = method.invoke(null, args);
								} else {
									throw new ParseException("No such method " + name + " in " + leftParameter.getClass().getCanonicalName(), node.getOffset());
								}
							}
						}
					} catch (ParseException e) {
						throw e;
					} catch (Exception e) {
						throw new ParseException(e.getMessage(), node.getOffset());
					}
				}
			}
		}
		parameterStack.push(result);
	}

}