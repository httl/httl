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

import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Map;

import org.bee.tl.core.GroupTemplate;
import org.bee.tl.core.Template;

public class BeetlCase implements Case {

    public void count(Counter counter, int times, String name, Map<String, Object> context, Writer writer, Writer discardWriter) throws Exception {
        counter.beginning();
        GroupTemplate group = new GroupTemplate();
        group.enableOptimize();
        group.enableNativeCall();
        counter.initialized();
        Template template = group.getReaderTemplate(new InputStreamReader(BeetlCase.class.getClassLoader().getResourceAsStream("performance/books.btl"))); 
        for (Map.Entry<String, Object> entry : context.entrySet()) {
        	template.set(entry.getKey(), entry.getValue());
        }
        counter.compiled();
        template.getText(writer);
        counter.executed();
    	for (int i = times; i >= 0; i --) {
            template.getText(discardWriter);
        }
        counter.finished();
    }
    
}

