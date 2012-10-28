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
package httl.spi;

import httl.Expression;

import java.text.ParseException;
import java.util.Map;


/**
 * Expression Translator. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setTranslator(translator)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Translator {

    /**
     * Translate the template expression to java expression.
     * 
     * @param source - Template expression source
     * @param parameterTypes Expression parameter types
     * @param offset - Template expression offset
     * @return Java expression
     */
    Expression translate(String source, Map<String, Class<?>> parameterTypes, int offset) throws ParseException;

}
