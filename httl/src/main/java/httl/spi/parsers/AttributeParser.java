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
package httl.spi.parsers;

import httl.Engine;
import httl.spi.engines.DefaultEngine;
import httl.spi.filters.AttributeSyntaxFilter;

/**
 * AttributeParser. (SPI, Singleton, ThreadSafe)
 * 
 * @deprecated Replace to: template.filter=<code>httl.spi.filters.AttributeSyntaxFilter</code>
 * @see httl.spi.filters.AttributeSyntaxFilter
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
@Deprecated
public class AttributeParser extends TemplateParser {

	private Engine engine;

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	@Override
	public void init() {
		if (engine instanceof DefaultEngine) {
			((DefaultEngine) engine).setTemplateFilter(new AttributeSyntaxFilter());
		}
		super.init();
	}

}