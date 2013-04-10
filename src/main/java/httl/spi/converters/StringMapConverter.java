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

import httl.internal.util.StringUtils;
import httl.spi.Codec;
import httl.spi.Compiler;
import httl.spi.Converter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * StringMapConverter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setMapConverter(Converter)
 * @see httl.spi.translators.InterpretedTranslator#setMapConverter(Converter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class StringMapConverter implements Converter<String, Map<String, Object>> {

	private final BeanMapConverter beanMapConverter = new BeanMapConverter();
	
	private Compiler compiler;

	private String formats = "";

	private Codec[] codecs;

	/**
	 * httl.properties: compiler=httl.spi.compilers.JdkCompiler
	 */
	public void setCompiler(Compiler compiler) {
		this.beanMapConverter.setCompiler(compiler);
		this.compiler = compiler;
	}

	public void setCodecs(Codec[] codecs) {
		this.codecs = codecs;
		StringBuilder buf = new StringBuilder();
		for (Codec codec : codecs) {
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append(codec.getFormat());
		}
		this.formats = buf.toString();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> convert(String value, Map<String, Class<?>> types) throws IOException,
			ParseException {
		if (StringUtils.isEmpty(value))
			return null;
		if (codecs != null) {
			value = value.trim();
			for (Codec codec : codecs) {
				if (codec.isValueOf(value)) {
					Class<?> type = BeanMapConverter.getBeanClass(String.valueOf(
							System.identityHashCode(types)), types, compiler, null);
					Object bean = codec.valueOf(value, type);
					if (bean instanceof Map) {
						return (Map<String, Object>) bean;
					}
					return beanMapConverter.convert(bean, types);
				}
			}
		}
		throw new IllegalArgumentException("Unsupported format of the string \"" + value + "\", only support format: "
					+ formats + ". Please add config codecs+=com.your.YourFormatStringCodec in httl.properties.");
	}

}