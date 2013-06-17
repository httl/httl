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

import httl.spi.Switcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MultiSwitcher. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTextFilterSwitcher(Switcher)
 * @see httl.spi.translators.CompiledTranslator#setValueFilterSwitcher(Switcher)
 * @see httl.spi.translators.CompiledTranslator#setFormatterSwitcher(Switcher)
 * @see httl.spi.translators.InterpretedTranslator#setTextFilterSwitcher(Switcher)
 * @see httl.spi.translators.InterpretedTranslator#setValueFilterSwitcher(Switcher)
 * @see httl.spi.translators.InterpretedTranslator#setFormatterSwitcher(Switcher)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiSwitcher<T> implements Switcher<T> {

	private Switcher<T>[] switchers;

	/**
	 * httl.properties: switchers=httl.spi.switchers.ScriptFilterSwitcher
	 */
	@SuppressWarnings("unchecked")
	public void setSwitchers(Switcher<T>[] switchers) {
		if (switchers != null && switchers.length > 0 
				&& this.switchers != null && this.switchers.length > 0) {
			Switcher<T>[] oldSwitchers = this.switchers;
			this.switchers = new Switcher[oldSwitchers.length + switchers.length];
			System.arraycopy(oldSwitchers, 0, this.switchers, 0, oldSwitchers.length);
			System.arraycopy(switchers, 0, this.switchers, oldSwitchers.length, switchers.length);
		} else {
			this.switchers = switchers;
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> locations() {
		if (switchers == null || switchers.length == 0) {
			return Collections.EMPTY_LIST;
		}
		if (switchers.length == 1) {
			return switchers[0].locations();
		}
		List<String> locations = new ArrayList<String>();
		for (Switcher<T> switcher : switchers) {
			locations.addAll(switcher.locations());
		}
		return locations;
	}

	public T switchover(String location, T origin) {
		if (switchers == null || switchers.length == 0) {
			return origin;
		}
		if (switchers.length == 1) {
			return switchers[0].switchover(location, origin);
		}
		for (Switcher<T> switcher : switchers) {
			if (switcher.locations().contains(location)) {
				return switcher.switchover(location, origin);
			}
		}
		return origin;
	}

}