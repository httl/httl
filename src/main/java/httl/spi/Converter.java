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
package httl.spi;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * Object Converter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setMapConverter(Converter)
 * @see httl.spi.translators.InterpretedTranslator#setMapConverter(Converter)
 * @see httl.spi.translators.CompiledTranslator#setOutConverter(Converter)
 * @see httl.spi.translators.InterpretedTranslator#setOutConverter(Converter)
 * 
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Converter<F, T> {

	/**
	 * Convert the value.
	 * 
	 * @param value - origin value
	 * @param types - convert types
	 * @return converted value
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	T convert(F value, Map<String, Class<?>> types) throws IOException, ParseException;

}