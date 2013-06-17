/*
 * Copyright 2011-2013 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.spi.filters;

import httl.spi.Filter;

/**
 * MultiFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTemplateFilter(Filter)
 * @see httl.spi.translators.CompiledTranslator#setTextFilter(Filter)
 * @see httl.spi.translators.CompiledTranslator#setValueFilter(Filter)
 * @see httl.spi.translators.InterpretedTranslator#setTemplateFilter(Filter)
 * @see httl.spi.translators.InterpretedTranslator#setTextFilter(Filter)
 * @see httl.spi.translators.InterpretedTranslator#setValueFilter(Filter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class MultiFilter implements Filter {
	
	private Filter[] filters;
	
	/**
	 * httl.properties: filters=httl.spi.filters.CompressBlankFilter
	 */
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

	public String filter(String key, String value) {
		if (filters == null || filters.length == 0) {
			return value;
		}
		if (filters.length == 1) {
			return filters[0].filter(key, value);
		}
		for (Filter filter : filters) {
			value = filter.filter(key, value);
		}
		return value;
	}

	public char[] filter(String key, char[] value) {
		if (filters == null || filters.length == 0) {
			return value;
		}
		if (filters.length == 1) {
			return filters[0].filter(key, value);
		}
		for (Filter filter : filters) {
			value = filter.filter(key, value);
		}
		return value;
	}

	public byte[] filter(String key, byte[] value) {
		if (filters == null || filters.length == 0) {
			return value;
		}
		if (filters.length == 1) {
			return filters[0].filter(key, value);
		}
		for (Filter filter : filters) {
			value = filter.filter(key, value);
		}
		return value;
	}

}