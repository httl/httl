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
package httl.spi.converters;

import httl.internal.util.ClassUtils;
import httl.internal.util.MapSupport;
import httl.internal.util.StringUtils;
import httl.spi.Compiler;
import httl.spi.Converter;
import httl.spi.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * BeanMapConverter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setMapConverter(Converter)
 * @see httl.spi.translators.InterpretedTranslator#setMapConverter(Converter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class BeanMapConverter implements Converter<Object, Map<String, Object>> {

	private static final ConcurrentMap<Class<?>, Class<?>> BEAN_WRAPPERS = new ConcurrentHashMap<Class<?>, Class<?>>();

	private Compiler compiler;

	/**
	 * httl.properties: compiler=httl.spi.compilers.JdkCompiler
	 */
	public void setCompiler(Compiler compiler) {
		this.compiler = compiler;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> convert(Object bean, Map<String, Class<?>> types) throws IOException, ParseException {
		if (bean == null) {
			return null;
		}
		Class<?> beanClass = bean.getClass();
		Class<?> wrapperClass = BEAN_WRAPPERS.get(beanClass);
		if (wrapperClass == null) {
			try {
				wrapperClass = getMapClass(beanClass, compiler);
				Class<?> old = BEAN_WRAPPERS.putIfAbsent(beanClass, wrapperClass);
				if (old != null) {
					wrapperClass = old;
				}
			} catch (ParseException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		try {
			return (Map<String, Object>) wrapperClass.getConstructor(beanClass).newInstance(bean);
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static Class<?> getBeanClass(String className, Map<String, Class<?>> properties, Compiler compiler, Logger logger) throws ParseException {
		StringBuilder fields = new StringBuilder();
		StringBuilder gets = new StringBuilder();
		StringBuilder sets = new StringBuilder();
		Set<String> added = new HashSet<String>();
		for (Map.Entry<String, Class<?>> entry : properties.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue().getCanonicalName();
			String method = name.substring(0, 1).toUpperCase() + name.substring(1);
			String fname = ClassUtils.filterJavaKeyword(name);
			if (! added.contains(fname)) {
				added.add(fname);
				fields.append("private " + type + " " + fname + ";\n");
				gets.append("public " + type + " get" + method + "() {\n");
				gets.append("	return " + fname + ";\n");
				gets.append("}\n");
				sets.append("public void set" + method + "(" + type + " " + fname + ") {\n");
				sets.append("	this." + fname + " = " + fname + ";\n");
				sets.append("}\n");
			}
		}
		StringBuilder code = new StringBuilder();
		className = "MapBean_" + StringUtils.getVaildName(className);
		code.append("package " + BeanMapConverter.class.getPackage().getName() + ";\n");
		code.append("public class " + className + " {\n");
		code.append(fields);
		code.append(gets);
		code.append(sets);
		code.append("}\n");
		if (logger != null && logger.isDebugEnabled()) {
			logger.debug(code.toString());
		}
		return compiler.compile(code.toString());
	}

	public static Class<?> getMapClass(Class<?> beanClass, Compiler compiler) throws ParseException {
		StringBuilder keys = new StringBuilder();
		StringBuilder gets = new StringBuilder();
		StringBuilder clss = new StringBuilder();
		StringBuilder puts = new StringBuilder();
		for (Method method : beanClass.getMethods()) {
			String name = method.getName();
			if ((name.length() > 3 && name.startsWith("get") 
					|| name.length() > 2 && name.startsWith("is"))
					&& Modifier.isPublic(method.getModifiers())
					&& method.getReturnType() != void.class
					&& method.getParameterTypes().length == 0
					&& method.getDeclaringClass() != Object.class) {
				int i = name.startsWith("get") ? 3 : 2;
				String key = name.substring(i, i + 1).toLowerCase() + name.substring(i + 1);
				if (keys.length() > 0) {
					keys.append(", ");
				}
				keys.append("\"" + key + "\"");
				if (gets.length() > 0) {
					gets.append("else ");
				}
				String call = "bean." + name + "()";
				if (method.getReturnType().isPrimitive()) {
					call = ClassUtils.class.getName() + ".boxed(" + call + ")";
				}
				gets.append("if (\"" + key + "\".equals(key)) return " + call + ";\n");
				clss.append("else if (\"" + key + ".class\".equals(key)) return " + method.getReturnType().getCanonicalName() + ".class;\n");
			} else if (name.length() > 3 && name.startsWith("set")
					&& Modifier.isPublic(method.getModifiers())
					&& method.getParameterTypes().length == 1
					&& method.getDeclaringClass() != Object.class) {
				String key = name.substring(3, 3 + 1).toLowerCase() + name.substring(3 + 1);
				if (puts.length() > 0) {
					puts.append("else ");
				}
				String var;
				if (method.getParameterTypes()[0].isPrimitive()) {
					var = ClassUtils.class.getName() + ".unboxed((" + ClassUtils.getBoxedClass(method.getParameterTypes()[0]).getCanonicalName() + ") value)";
				} else {
					var = "(" + method.getParameterTypes()[0].getCanonicalName() + ") value";
				}
				puts.append("if (\"" + key + "\".equals(key)) bean." + name + "(" + var + ");\n");
			}
		}
		StringBuilder code = new StringBuilder();
		String className = "BeanMap_" + beanClass.getCanonicalName().replace('.', '_');
		code.append("package " + BeanMapConverter.class.getPackage().getName() + ";\n");
		code.append("public class " + className + " extends " + MapSupport.class.getName() + " {\n");
		code.append("private " + beanClass.getCanonicalName() + " bean;\n");
		code.append("public " + className + "(" + beanClass.getCanonicalName() + " bean) {\n");
		if (keys.length() > 0) {
			code.append("super(new String[] {" + keys + "});\n");
		}
		code.append("this.bean = bean;\n");
		code.append("}\n");
		code.append("public Object get(Object key) {\n");
		code.append(gets);
		code.append(clss);
		code.append("return null;\n");
		code.append("}\n");
		code.append("public Object put(Object key, Object value) {\n");
		code.append("Object old = get(key);\n");
		code.append(puts);
		code.append("return old;\n");
		code.append("}\n");
		code.append("}\n");
		try {
			return compiler.compile(code.toString());
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage() + "\n====\n" + code.toString() + "\n====\n", e);
		}
	}

}