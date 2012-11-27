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
package httl.test.performance;

import httl.Engine;
import httl.Template;

import java.io.Writer;
import java.util.Map;

/**
 * HttlCase
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlCase implements Case {

    public void count(Counter counter, int times, String name, Map<String, Object> context, Writer writer, Writer discardWriter) throws Exception {
    	String path = "performance/" + name + ".httl";
        counter.beginning();
        Engine engine = Engine.getEngine("httl-performance.properties");
        counter.initialized();
        Template template = engine.getTemplate(path);
        counter.compiled();
        template.render(context, writer);
        counter.executed();
		for (int i = times; i >= 0; i--) {
			engine.getTemplate(path).render(context, discardWriter);
		}
        counter.finished();
    }

}
