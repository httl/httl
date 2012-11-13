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

/**
 * MultiFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setFilter(Filter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class MultiFilter implements Filter {
    
    private Filter[] filters;
    
    public void setFilters(Filter[] filters) {
    	if (filters != null && filters.length > 0 
    			&& this.filters != null && this.filters.length > 0) {
    		Filter[] oldFilters = this.filters;
    		this.filters = new Filter[oldFilters.length + filters.length];
    		System.arraycopy(oldFilters, 0, this.filters, 0, oldFilters.length);
    		System.arraycopy(filters, 0, this.filters, oldFilters.length, filters.length);
    	} else {
    		this.filters = filters;
    	}
    }

    public String filter(String value) {
    	if (filters == null || filters.length == 0) {
    		return value;
    	}
    	if (filters.length == 1) {
    		return filters[0].filter(value);
    	}
        for (Filter filter : filters) {
            value = filter.filter(value);
        }
        return value;
    }

}
