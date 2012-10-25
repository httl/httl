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

import httl.util.ClassUtils;
import httl.util.LinkedStack;

import java.io.Serializable;


/**
 * ForeachStatus. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ForeachStatus implements Serializable {
    
    private static final long serialVersionUID = -6011370058720809056L;
    
    private final LinkedStack<ForeachCounter> stack = new LinkedStack<ForeachCounter>();
    
    public <T> T push(T list) {
        stack.push(new ForeachCounter(stack.peek(), ClassUtils.getSize(list), stack.size()));
        return list;
    }
    
    public void pop() {
        stack.pop();
    }
    
    public void increment() {
        stack.peek().increment();
    }

    public ForeachCounter getParent() {
        return stack.peek().getParent();
    }

    public int getSize() {
        return stack.peek().getSize();
    }
    
    public int getLevel() {
        return stack.peek().getLevel();
    }
    
    public int getIndex() {
        return stack.peek().getIndex();
    }
    
    public int getCount() {
        return stack.peek().getIndex() + 1;
    }
    
    public boolean isOdd() {
        return stack.peek().getIndex() % 2 != 0;
    }
    
    public boolean isEven() {
        return stack.peek().getIndex() % 2 == 0;
    }
    
    public boolean isFirst() {
        return stack.peek().getIndex() == 0;
    }
    
    public boolean isLast() {
        ForeachCounter counter = stack.peek();
        return counter.getIndex() >= counter.getSize() - 1;
    }
    
    public boolean isMiddle() {
        ForeachCounter counter = stack.peek();
        return counter.getIndex() > 0 && counter.getIndex() < counter.getSize() - 1;
    }
    
}
