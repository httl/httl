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
package httl.spi.caches;

import httl.spi.Cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * LruCache. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setCache(Cache)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class LruCache extends MapCache {
    
    private static final int DEFAULT_CAPACITY = 1000;
    
	private static class LruMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = -7831882959160110063L;
        
        private final AtomicInteger capacity;
        
        private LruMap(AtomicInteger capacity) {
            this.capacity = capacity;
        }

        @Override
	    protected boolean removeEldestEntry(Entry<K, V> eldest) {
	        return size() > capacity.get();
	    }

	};

    private final AtomicInteger capacity;
    
	public LruCache() {
	    this(DEFAULT_CAPACITY);
	}
	
	public LruCache(int capacity) {
	    this(new AtomicInteger(capacity));
	}

    public LruCache(AtomicInteger capacity) {
        super(Collections.synchronizedMap(new LruMap<Object, Object>(capacity)));
        this.capacity = capacity;
    }

    public int getCacheCapacity() {
        return capacity.get();
    }

    public void setCacheCapacity(int capacity) {
        this.capacity.set(capacity);
    }

}
