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

import httl.util.ConcurrentLinkedHashMap;

/**
 * LruCache. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setTemplateCache(java.util.Map)
 * @see httl.spi.engines.DefaultEngine#setExpressionCache(java.util.Map)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class LruCache<K, V> extends ConcurrentLinkedHashMap<K, V> {
    
	private static final long serialVersionUID = 1384756602324047236L;

	/**
	 * httl.properties: cache.capacity=1000
	 */
	public void setCacheCapacity(int capacity) {
        super.setCapacity(capacity);
    }

}
