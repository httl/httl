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
 * Bracket. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Bracket extends Operator {

	private static final long serialVersionUID = 1L;

    public static final Bracket ROUND = new Bracket("(");
    
    public static final Bracket SQUARE = new Bracket("[");

    private Bracket(String name) {
        super(null, null, 0, null, null, null, name, Integer.MAX_VALUE);
    }

    public Class<?> getReturnType() throws ParseException {
        return null;
    }

    public String getCode() throws ParseException {
        return getName();
    }
    
}
