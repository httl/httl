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

import java.text.ParseException;


/**
 * Constant
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Constant extends AbstractExpression {

	private static final long serialVersionUID = 1L;

    public static final Constant NULL = new Constant(null, null, "null");

    public static final Constant EMPTY = new Constant(null, null, "");

    public static final Constant TRUE = new Constant(true, boolean.class, "true");

    public static final Constant FALSE = new Constant(false, boolean.class, "false");
    
    private final Object value;

    private final Class<?> type;
    
    private final String literal;

    public Constant(Object value, Class<?> type, String literal){
        super(null, null, 0, null);
        this.value = value;
        this.type = type;
        this.literal = literal;
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getReturnType() throws ParseException {
        return type;
    }

    public String getCode() throws ParseException {
        return literal;
    }

    @Override
    public String toString() {
        return literal;
    }

}
