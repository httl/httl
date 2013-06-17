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
package httl.spi.switchers;

import java.util.Arrays;
import java.util.List;

import httl.spi.Switcher;
import httl.spi.Filter;

/**
 * StyleFilterSwitcher. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTextFilterSwitcher(Switcher)
 * @see httl.spi.translators.CompiledTranslator#setValueFilterSwitcher(Switcher)
 * @see httl.spi.translators.InterpretedTranslator#setTextFilterSwitcher(Switcher)
 * @see httl.spi.translators.InterpretedTranslator#setValueFilterSwitcher(Switcher)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class StyleFilterSwitcher implements Switcher<Filter> {

	private static final String START_TAG = "<style";

	private static final String END_TAG = "</style>";

	private List<String> styleLocations = Arrays.asList(new String[] {START_TAG, END_TAG});

	private Filter styleFilter;

	/**
	 * httl.properties: style.locations=&lt;style,&lt;/style&gt;
	 */
	public void setStyleLocations(String[] locations) {
		this.styleLocations = Arrays.asList(locations);
	}

	/**
	 * httl.properties: style.filter=httl.spi.filters.StyleFilter
	 */
	public void setStyleFilter(Filter filter) {
		this.styleFilter = filter;
	}

	public List<String> locations() {
		return styleLocations;
	}

	public Filter switchover(String location, Filter defaultFilter) {
		if (START_TAG.equals(location)) {
			return styleFilter;
		}
		return defaultFilter;
	}

}