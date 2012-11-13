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
package httl.spi.translators;

import httl.Engine;
import httl.Expression;
import httl.spi.Compiler;
import httl.spi.Translator;
import httl.spi.sequences.StringSequence;
import httl.spi.translators.expression.ExpressionImpl;
import httl.spi.translators.expression.Node;
import httl.util.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DfaTranslator. (SPI, Singleton, ThreadSafe)
 * 
 * Deterministic Finite state Automata (DFA)
 * 
 * @see httl.spi.parsers.AbstractParser#setTranslator(translator)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DfaTranslator implements Translator {

    private Engine engine;

    private Compiler compiler;
    
    protected String[] importPackages;

    private final Map<Class<?>, Object> functions = new ConcurrentHashMap<Class<?>, Object>();

    private final List<StringSequence> sequences = new CopyOnWriteArrayList<StringSequence>();

    /**
     * httl.properties: engine=httl.spi.engines.DefaultEngine
     */
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    /**
     * httl.properties: compiler=httl.spi.compilers.JdkCompiler
     */
    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    /**
     * httl.properties: import.packages=java.util
     */
    public void setImportPackages(String[] importPackages) {
    	this.importPackages = importPackages;
    }

    public void setImportMethods(Object[] importMethods) {
    	for (Object function : importMethods) {
    		this.functions.put(function.getClass(), function);
    	}
    }

    /**
     * httl.properties: sequences=Mon Tue Wed Thu Fri Sat Sun Mon
     */
    public void setSequences(String[] sequences) {
    	for (String s : sequences) {
            s = s.trim();
            if (s.length() > 0) {
                String[] ts = s.split("\\s+");
                List<String> sequence = new ArrayList<String>();
                for (String t : ts) {
                    t = t.trim();
                    if (t.length() > 0) {
                        sequence.add(t);
                    }
                }
                this.sequences.add(new StringSequence(sequence));
            }
        }
    }

	public Expression translate(String source, Map<String, Class<?>> parameterTypes, int offset) throws ParseException {
	    source = StringUtils.unescapeHtml(source);
	    Node node = new DfaParser(this, parameterTypes, functions.keySet(), sequences, importPackages, offset).parse(source);
        return new ExpressionImpl(source, parameterTypes, offset, node.getCode(), node.getReturnType(), engine, compiler, importPackages, functions);
	}

}
