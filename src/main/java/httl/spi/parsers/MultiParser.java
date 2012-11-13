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
package httl.spi.parsers;

import httl.Resource;
import httl.spi.Parser;
import httl.spi.Translator;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MultiParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setParser(Parser)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiParser extends AbstractParser {
    
    private AbstractParser[] parsers;

    public void setParsers(AbstractParser[] parsers) {
        this.parsers = parsers;
    }

    protected String doParse(Resource resource, boolean stream, String source, Translator translator, 
                             List<String> parameters, List<Class<?>> parameterTypes, 
                             Set<String> variables, Map<String, Class<?>> types, Map<String, Class<?>> returnTypes, Map<String, Class<?>> macros) throws IOException, ParseException {
        if (parsers == null || parsers.length == 0) {
        	throw new IllegalStateException("parsers == null");
        }
    	for (AbstractParser parser : parsers) {
            source = parser.doParse(resource, stream, source, translator, parameters, parameterTypes, variables, types, returnTypes, macros);
        }
        return source;
    }

}
