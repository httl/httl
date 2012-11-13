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

import httl.Engine;
import httl.Expression;
import httl.spi.Compiler;
import httl.util.ClassUtils;
import httl.util.MD5;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ExpressionImpl. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ExpressionImpl implements Expression, Serializable {

	private static final long serialVersionUID = 1L;

    private final Engine engine;

    private final Compiler compiler;
    
    private final String source;
    
    private final int offset;
    
    private final Node node;

	private final String code;

    private final Map<String, Class<?>> parameterTypes;

    private final Class<?> returnType;
    
    private final String[] importPackages;
    
    private final Set<String> importPackageSet;

    private final Map<Class<?>, Object> functions;

    private volatile String md5;
    
    private volatile Evaluator evaluator;
    
    public ExpressionImpl(String source, Map<String, Class<?>> parameterTypes, int offset, Node node, String code, Class<?> returnType, Engine engine, Compiler compiler, String[] importPackages, Map<Class<?>, Object> functions){
        this.engine = engine;
        this.compiler = compiler;
        this.source = source;
        this.offset = offset;
        this.node = node;
        this.code = code;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.importPackages = importPackages;
        this.importPackageSet = new HashSet<String>(Arrays.asList(importPackages));
        this.functions = functions;
    }

    public Node getNode() {
		return node;
	}

    public Object evaluate(Map<String, Object> parameters) {
    	if (evaluator == null) {
    		synchronized (this) {
    			if (evaluator == null) { // double check
    				evaluator = newEvaluator(); // lazy compile
    			}
			}
    	}
        return evaluator.evaluate(parameters);
    }
    
    private Class<?> newEvaluatorClass(String className) {
    	StringBuilder imports = new StringBuilder();
        String[] packages = importPackages;
        if (packages != null && packages.length > 0) {
            for (String pkg : packages) {
                imports.append("import ");
                imports.append(pkg);
                imports.append(".*;\n");
            }
        }
        StringBuilder declare = new StringBuilder();
        for (Map.Entry<String, Class<?>> entry : parameterTypes.entrySet()) {
        	String var = entry.getKey();
            Class<?> type = entry.getValue();
            String pkgName = type.getPackage() == null ? null : type.getPackage().getName();
            String typeName;
            if (pkgName != null && ("java.lang".equals(pkgName) 
                    || (importPackageSet != null && importPackageSet.contains(pkgName)))) {
                typeName = type.getSimpleName();
            } else {
                typeName = type.getCanonicalName();
            }
            declare.append(typeName + " " + var + " = " + ClassUtils.getInitCode(type) + ";\n");
        }
        StringBuilder funtionFileds = new StringBuilder();
        StringBuilder functionInits = new StringBuilder();
        for (Map.Entry<Class<?>, Object> function : functions.entrySet()) {
        	Class<?> functionType = function.getKey();
        	if (function.getValue() instanceof Class) {
        		continue;
        	}
        	String pkgName = functionType.getPackage() == null ? null : functionType.getPackage().getName();
            String typeName;
            if (pkgName != null && ("java.lang".equals(pkgName) 
                    || (importPackageSet != null && importPackageSet.contains(pkgName)))) {
                typeName = functionType.getSimpleName();
            } else {
                typeName = functionType.getCanonicalName();
            }
            funtionFileds.append("private final ");
        	funtionFileds.append(typeName);
        	funtionFileds.append(" _");
        	funtionFileds.append(functionType.getName().replace('.','_'));
        	funtionFileds.append(";\n");
        	
        	functionInits.append("this._");
        	functionInits.append(functionType.getName().replace('.','_'));
        	functionInits.append(" = (");
        	functionInits.append(typeName);
        	functionInits.append(") functions.get(");
        	functionInits.append(typeName);
        	functionInits.append(".class);\n");
        }
        String sourceCode = "package " + Evaluator.class.getPackage().getName() + ";\n" 
                + imports.toString()
                + "public class " + className + " implements " + Evaluator.class.getName() + " {\n" 
                + funtionFileds
                + "public " + className + "(Map functions) {\n"
                + functionInits
                + "}\n"
                + "public " + Object.class.getSimpleName() + " evaluate(" + Map.class.getName() + " parameters) {\n"
                + declare.toString()
                + "return " + ClassUtils.class.getName() + ".boxed(" + getCode() + ");\n"
                + "}\n"
                + "}\n";
        try {
        	return compiler.compile(sourceCode);
        } catch (ParseException e) {
        	throw new IllegalStateException("Failed to create expression class. class: " + className + ", code: \n" + sourceCode + ", offset: " + getOffset() + ", cause:" + ClassUtils.toString(e));
        }
    }

    private Evaluator newEvaluator() {
    	if (md5 == null) {
    		md5 = MD5.getMD5(source);
    	}
    	String className = (Evaluator.class.getSimpleName() + "_" + md5);
    	Class<?> cls;
    	try {
    		cls = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
    	} catch (ClassNotFoundException e) {
    		cls = newEvaluatorClass(className);
    	}
        try {
            return (Evaluator) cls.getConstructor(Map.class).newInstance(functions);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create expression instance. class: " + className + ", offset: " + getOffset() + ", cause:" + ClassUtils.toString(e));
        }
    }

    public String getSource() {
        return source;
    }
    
    public String getCode() {
        return code;
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public Engine getEngine() {
        return engine;
    }
    
}
