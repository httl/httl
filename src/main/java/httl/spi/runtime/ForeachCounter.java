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
package httl.spi.runtime;

import java.io.Serializable;

/**
 * ForeachCounter. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ForeachCounter implements Serializable {

    private static final long serialVersionUID = -6011370058720809056L;
    
    private final ForeachCounter parent;

    private final int size;
    
    private final int level;
    
    private int index = 0;

    public ForeachCounter(ForeachCounter parent, int s, int l) {
        this.parent = parent;
        this.size = s;
        this.level = l;
    }

    public void increment() {
        index ++;
    }
    
    public ForeachCounter getParent() {
        return parent;
    }

    public int getSize() {
        return size;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getIndex() {
        return index;
    }
    
}
