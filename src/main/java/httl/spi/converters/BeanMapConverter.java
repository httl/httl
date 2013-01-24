/*
 * Copyright 2011-2012 HTTL Team.
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
package httl.spi.converters;

import httl.spi.Converter;
import httl.util.BeanMap;

import java.util.Map;

/**
 * BeanMapConverter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.DefaultTranslator#setMapConverter(Converter)
 * @see httl.spi.parsers.AbstractParser#setMapConverter(Converter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class BeanMapConverter implements Converter<Object, Map<String, Object>> {

	public Map<String, Object> convert(Object value) throws Exception {
		return new BeanMap(value);
	}

}