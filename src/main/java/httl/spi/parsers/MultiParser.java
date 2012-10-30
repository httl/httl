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

import httl.Engine;
import httl.Resource;
import httl.spi.Parser;
import httl.spi.Translator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * MultiParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setParser(Parser)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiParser extends AbstractParser {
    
    private final List<AbstractParser> parsers = new ArrayList<AbstractParser>();

    public void setParsers(AbstractParser[] parsers) {
        add(parsers);
    }

    public void add(AbstractParser... parsers) {
        if (parsers != null && parsers.length > 0) {
            for (AbstractParser parser : parsers) {
                if (parser != null && ! this.parsers.contains(parser)) {
                    this.parsers.add(parser);
                }
            }
        }
    }

    public void remove(AbstractParser... parsers) {
        if (parsers != null && parsers.length > 0) {
            for (AbstractParser parser : parsers) {
                if (parser != null) {
                    this.parsers.remove(parser);
                }
            }
        }
    }
    
    public void clear() {
        this.parsers.clear();
    }

    @Override
    public void setEngine(Engine engine) {
        super.setEngine(engine);
        this.engine = engine;
        for (AbstractParser parser : parsers) {
            parser.setEngine(engine);
        }
    }

    protected String doParse(Resource resource, boolean stream, String source, Translator translator, 
                             List<String> parameters, List<Class<?>> parameterTypes, 
                             Set<String> variables, Map<String, Class<?>> types, Map<String, Class<?>> macros) throws IOException, ParseException {
        for (AbstractParser parser : parsers) {
            source = parser.doParse(resource, stream, source, translator, parameters, parameterTypes, variables, types, macros);
        }
        return source;
    }

}
