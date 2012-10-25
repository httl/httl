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
import httl.spi.Configurable;

import java.util.Map;


/**
 * AdaptiveCache. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setCache(Cache)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class AdaptiveCache implements Cache, Configurable {
    
    private Cache cache;
    
    public void configure(Map<String, String> config) {
        String capacity = config.get(CACHE_CAPACITY);
        if (capacity != null && capacity.trim().length() > 0 && Integer.parseInt(capacity.trim()) > 0) {
            cache = new LruCache(Integer.parseInt(capacity.trim()));
        } else {
            cache = new StrongCache();
        }
        if (cache instanceof Configurable) {
            ((Configurable)cache).configure(config);
        }
    }

    public Object get(Object key) {
        return cache.get(key);
    }

    public void put(Object key, Object value) {
        cache.put(key, value);
    }

    public void remove(Object key) {
        cache.remove(key);
    }

}
