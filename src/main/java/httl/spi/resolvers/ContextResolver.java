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
package httl.spi.resolvers;

import httl.Context;
import httl.Template;
import httl.spi.Resolver;

/**
 * ContextResolver. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ContextResolver implements Resolver {

	public Object get(String key) {
		if ("parent".equals(key)) {
			return Context.getContext().getParent();
		} else if ("super".equals(key)) {
			return Context.getContext().getSuper();
		} else if ("this".equals(key)) {
			return Context.getContext().getTemplate();
		} else if ("engine".equals(key)) {
			return Context.getContext().getEngine();
		} else if ("out".equals(key)) {
			return Context.getContext().getOut();
		} else if ("level".equals(key)) {
			return Context.getContext().getLevel();
		} else {
			Template template = Context.getContext().getTemplate();
			if (template != null) {
				return template.getMacros().get(key);
			}
			return null;
		}
	}

}