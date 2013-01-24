/*
 * Copyright 2011-2012 HTTL Team.
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

import httl.spi.Compiler;
import httl.spi.Converter;
import httl.util.MapSupport;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * BeanMapConverter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.DefaultTranslator#setMapConverter(Converter)
 * @see httl.spi.parsers.AbstractParser#setMapConverter(Converter)
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
	public Map<String, Object> convert(Object bean) throws IOException, ParseException {
		if (bean == null) {
			return null;
		}
		Class<?> beanClass = bean.getClass();
		Class<?> wrapperClass = BEAN_WRAPPERS.get(beanClass);
		if (wrapperClass == null) {
			try {
				wrapperClass = createWrapperClass(beanClass, compiler);
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

	private Class<?> createWrapperClass(Class<?> beanClass, Compiler compiler) throws ParseException {
		StringBuilder keys = new StringBuilder();
		StringBuilder gets = new StringBuilder();
		for (Method method : beanClass.getMethods()) {
			String name = method.getName();
			if ((name.length() > 3 && name.startsWith("get") 
					|| name.length() > 2 && name.startsWith("is"))
					&& Modifier.isPublic(method.getModifiers())
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
				gets.append("if (\"" + key + "\".equals(key)) return bean." + name + "();\n");
			}
		}
		StringBuilder code = new StringBuilder();
		String className = "BeanMap_" + beanClass.getCanonicalName().replace('.', '_');
		code.append("package " + BeanMapConverter.class.getPackage().getName() + ";\n");
		code.append("public class " + className + " extends " + MapSupport.class.getName() + " {\n");
		code.append("private " + beanClass.getCanonicalName() + " bean;\n");
		code.append("public " + className + "(" + beanClass.getCanonicalName() + " bean) {\n");
		code.append("super(new String[] {" + keys + "});\n");
		code.append("this.bean = bean;\n");
		code.append("}\n");
		code.append("public Object get(Object key) {\n");
		code.append(gets);
		code.append("return null;\n");
		code.append("}\n");
		code.append("}\n");
		return compiler.compile(code.toString());
	}

}
