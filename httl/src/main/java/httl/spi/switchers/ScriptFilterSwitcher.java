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
 * ScriptFilterSwitcher. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTextFilterSwitcher(Switcher)
 * @see httl.spi.translators.CompiledTranslator#setValueFilterSwitcher(Switcher)
 * @see httl.spi.translators.InterpretedTranslator#setTextFilterSwitcher(Switcher)
 * @see httl.spi.translators.InterpretedTranslator#setValueFilterSwitcher(Switcher)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ScriptFilterSwitcher implements Switcher<Filter> {

	private static final String START_TAG = "<script";

	private static final String END_TAG = "</script>";

	private List<String> scriptLocations = Arrays.asList(new String[] {START_TAG, END_TAG});

	private Filter scriptFilter;

	/**
	 * httl.properties: script.locations=&lt;script,&lt;/script&gt;
	 */
	public void setScriptLocations(String[] locations) {
		this.scriptLocations = Arrays.asList(locations);
	}

	/**
	 * httl.properties: script.filter=httl.spi.filters.ScriptFilter
	 */
	public void setScriptFilter(Filter filter) {
		this.scriptFilter = filter;
	}

	public List<String> locations() {
		return scriptLocations;
	}

	public Filter switchover(String location, Filter defaultFilter) {
		if (START_TAG.equals(location)) {
			return scriptFilter;
		}
		return defaultFilter;
	}

}