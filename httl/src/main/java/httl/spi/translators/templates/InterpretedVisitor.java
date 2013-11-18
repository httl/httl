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
import httl.ast.AstVisitor;
import httl.ast.BinaryOperator;
import httl.ast.BreakDirective;
import httl.ast.Constant;
import httl.ast.ElseDirective;
import httl.ast.ForDirective;
import httl.ast.IfDirective;
import httl.ast.MacroDirective;
import httl.ast.SetDirective;
import httl.ast.Text;
import httl.ast.UnaryOperator;
import httl.ast.ValueDirective;
import httl.ast.Variable;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Switcher;
import httl.util.ClassUtils;
import httl.util.CollectionUtils;
import httl.util.LinkedStack;
import httl.util.Status;
import httl.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
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

	private String filterVariable;

	private String formatterVariable;

	private String[] forVariable;

	private String ifVariable;

	private String breakVariable;

	private String outputEncoding;

	private Map<Class<?>, Object> importMethods;

	private Map<String, Template> importMacros;

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

	@Override
	public void visit(UnaryOperator node) throws IOException, ParseException {
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

	@Override
	public void visit(BinaryOperator node) throws IOException, ParseException {
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
				} else if (rightParameter.getClass().equals(Object[].class)) {
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
						throw new ParseException(ClassUtils.toString(e), node.getOffset());
					}
				}
			}
		}
		parameterStack.push(result);
	}

}