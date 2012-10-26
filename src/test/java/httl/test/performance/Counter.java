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

/**
 * Counter
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class Counter {
    
    private long beginning;
    
    private long initialized;

    private long compiled;

    private long executed;

    private long finished;
    
    public void beginning() {
        beginning = System.currentTimeMillis();
    }

    public void initialized() {
        initialized = System.currentTimeMillis();
    }

    public void compiled() {
        compiled = System.currentTimeMillis();
    }

    public void executed() {
        executed = System.currentTimeMillis();
    }

    public void finished() {
        finished = System.currentTimeMillis();
    }

    public long getInitialized() {
        return initialized - beginning;
    }
    
    public long getCompiled() {
        return compiled - initialized;
    }
    
    public long getExecuted() {
        return executed - compiled;
    }

    public long getFinished() {
        return finished - executed;
    }

}
