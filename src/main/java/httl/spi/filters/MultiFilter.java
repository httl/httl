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
package httl.spi.filters;

import httl.spi.Filter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AbstractMultiFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setFilter(Filter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class MultiFilter implements Filter {
    
    private final List<Filter> templateFilters = new CopyOnWriteArrayList<Filter>();
    
    public void setFilters(Filter[] filters) {
    	add(filters);
    }

    public void add(Filter... filters) {
        if (filters != null && filters.length > 0) {
            for (Filter filter : filters) {
                if (filter != null && ! templateFilters.contains(filter)) {
                    templateFilters.add(filter);
                }
            }
        }
    }
    
    public void remove(Filter... filters) {
        if (filters != null && filters.length > 0) {
            for (Filter filter : filters) {
                if (filter != null) {
                    templateFilters.remove(filter);
                }
            }
        }
    }
    
    public void clear() {
        templateFilters.clear();
    }

    public String filter(String value) {
    	if (templateFilters.size() == 1) {
    		return templateFilters.get(0).filter(value);
    	}
        if (templateFilters.size() > 0) {
            for (Filter filter : templateFilters) {
                value = filter.filter(value);
            }
        }
        return value;
    }

}
