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
import httl.spi.Configurable;
import httl.spi.Translator;
import httl.spi.translators.expression.ExpressionImpl;
import httl.spi.translators.expression.Node;
import httl.util.StringUtils;

import java.text.ParseException;
import java.util.Collection;
import java.util.Map;


/**
 * DfaResolver. (SPI, Singleton, ThreadSafe)
 * 
 * Deterministic Finite state Automata (DFA)
 * 
 * @see httl.Engine#setTranslator(Translator)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DfaTranslator implements Translator, Configurable {

    private Engine engine;

    protected String[] importPackages;

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
    
    public void configure(Map<String, String> config) {
        String packages = config.get(IMPORT_PACKAGES);
        if (packages != null && packages.trim().length() > 0) {
            importPackages = packages.trim().split("\\s*\\,\\s*");
        }
    }
    
	public Expression translate(String source, Map<String, Class<?>> parameterTypes, int offset) throws ParseException {
	    source = StringUtils.unescapeHtml(source);
	    Collection<Class<?>> functions = engine.getFunctions().keySet();
	    Node node = new DfaParser(this, parameterTypes, functions, importPackages, offset).parse(source);
        return new ExpressionImpl(engine, source, parameterTypes, offset, node.getCode(), node.getReturnType(), importPackages);
	}

}
