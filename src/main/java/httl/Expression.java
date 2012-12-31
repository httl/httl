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
package httl;

import java.util.Map;

/**
 * Expression. (API, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getExpression(String)
 * @see httl.Engine#getExpression(String, Map)
 * @see httl.spi.Translator#translate(String, java.util.Map, int)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Expression {

    /**
     * Evaluate the expression.
     * 
     * @param parameters - evaluate parameters
     * @return evaluate result
     */
    Object evaluate();

    /**
     * Evaluate the expression.
     * 
     * @param parameters - evaluate parameters
     * @return evaluate result
     */
    Object evaluate(Map<String, Object> parameters);

    /**
     * Get the expression parameter types. (Ordered)
     * 
     * @return parameter types
     */
    Map<String, Class<?>> getParameterTypes();

    /**
     * Get the expression return type.
     * 
     * @return return type
     */
    Class<?> getReturnType();

    /**
     * Get the expression code.
     * 
     * @return code
     */
    String getCode();

    /**
     * Get the expression source.
     * 
     * @return source
     */
    String getSource();

    /**
     * Get the expression offset.
     * 
     * @return offset
     */
    int getOffset();

    /**
     * Get the expression engine.
     * 
     * @return engine
     */
    Engine getEngine();

}
