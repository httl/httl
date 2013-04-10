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

import httl.spi.Filter;
import httl.spi.Switcher;
import httl.internal.util.Reqiured;

/**
 * MultiTextFilterSwitcher. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTextFilterSwitcher(Switcher)
 * @see httl.spi.translators.InterpretedTranslator#setTextFilterSwitcher(Switcher)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiTextFilterSwitcher extends MultiSwitcher<Filter> {

	/**
	 * httl.properties: text.filter.switchers=httl.spi.switchers.JavascriptFilterSwitcher
	 */
	@Reqiured
	public void setTextFilterSwitchers(Switcher<Filter>[] switchers) {
		setSwitchers(switchers);
	}

}