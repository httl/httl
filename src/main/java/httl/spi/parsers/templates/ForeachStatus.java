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
package httl.spi.parsers.templates;

import httl.util.ClassUtils;

import java.io.Serializable;

/**
 * ForeachStatus. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ForeachStatus implements Serializable {

    private static final long serialVersionUID = -6011370058720809056L;
    
    private final ForeachStatus parent;

    private final Object data;
    
    private final int size;
    
    private final int level;
    
    private int index = 0;

    public ForeachStatus(ForeachStatus parent, Object data) {
        this.parent = parent;
        this.data = data;
        this.size = ClassUtils.getSize(data);
        this.level = parent == null ? 0 : parent.getLevel() + 1;
    }

    public void increment() {
        index ++;
    }
    
    public ForeachStatus getParent() {
        return parent;
    }

    public Object getData() {
        return data;
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

    public int getCount() {
        return index + 1;
    }
    
    public boolean isOdd() {
        return index % 2 != 0;
    }
    
    public boolean isEven() {
        return index % 2 == 0;
    }
    
    public boolean isFirst() {
        return index == 0;
    }
    
    public boolean isLast() {
        return index >= size - 1;
    }
    
    public boolean isMiddle() {
        return index > 0 && index < size - 1;
    }
}
