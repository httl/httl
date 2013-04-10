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
package httl.spi.converters;

import httl.internal.util.ClassComparator;
import httl.internal.util.ClassUtils;
import httl.spi.Converter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MultiConverter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setMapConverter(Converter)
 * @see httl.spi.translators.InterpretedTranslator#setMapConverter(Converter)
 * @see httl.spi.translators.CompiledTranslator#setOutConverter(Converter)
 * @see httl.spi.translators.InterpretedTranslator#setOutConverter(Converter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MultiConverter implements Converter<Object, Object> {

	private final Map<Class<?>, Converter<Object, Object>> converters = new ConcurrentHashMap<Class<?>, Converter<Object, Object>>();

	private Map<Class<?>, Converter<Object, Object>> sortedConverters;

	public void setConverters(Converter<Object, Object>[] converters) {
		if (converters != null && converters.length > 0) {
			for (Converter<Object, Object> converter : converters) {
				if (converter != null) {
					Class<?> type = ClassUtils.getGenericClass(converter.getClass());
					if (type != null && ! this.converters.containsKey(type)) {
						this.converters.put(type, converter);
					}
				}
			}
			Map<Class<?>, Converter<Object, Object>> sorted = new TreeMap<Class<?>, Converter<Object, Object>>(ClassComparator.COMPARATOR);
			sorted.putAll(this.converters);
			this.sortedConverters = Collections.unmodifiableMap(sorted);
		}
	}

	public Object convert(Object value, Map<String, Class<?>> types) throws IOException, ParseException {
		if (value != null && converters != null) {
			Class<?> cls = value.getClass();
			Converter<Object, Object> converter = (Converter<Object, Object>) converters.get(cls);
			if (converter != null) {
				return converter.convert(value, types);
			} else if (sortedConverters != null) {
				for (Map.Entry<Class<?>, Converter<Object, Object>> entry : sortedConverters.entrySet()) {
					if (entry.getKey().isAssignableFrom(cls)) {
						converter = entry.getValue();
						converters.put(cls, converter);
						return converter.convert(value, types);
					}
				}
			}
		}
		return value;
	}

}