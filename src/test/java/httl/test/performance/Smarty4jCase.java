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

import java.io.File;
import java.io.Writer;
import java.util.Map;

import org.lilystudio.smarty4j.Context;
import org.lilystudio.smarty4j.Engine;
import org.lilystudio.smarty4j.Template;

/**
 * Smarty4jCase
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Smarty4jCase implements Case {

    public void count(Counter counter, int times, String name, Map<String, Object> map, Writer writer, Writer discardWriter) throws Exception {
        String path = "/" + name + ".st";
    	Context context = new Context();
        context.putAll(map);
        counter.beginning();
        Engine engine = new Engine();
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("performance/books.st").getFile());
        String directory = file.getParentFile().getAbsolutePath();
        engine.setTemplatePath(directory);
        counter.initialized();
        Template template = engine.getTemplate(path);
        counter.compiled();
        template.merge(context, writer);
        counter.executed();
		for (int i = times; i >= 0; i--) {
			engine.getTemplate(path).merge(context, discardWriter);
		}
        counter.finished();
    }
    
}
