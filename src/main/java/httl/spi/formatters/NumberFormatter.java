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
package httl.spi.formatters;

import httl.spi.Formatter;
import httl.internal.util.NumberUtils;

import java.text.DecimalFormat;

/**
 * NumberFormatter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setFormatter(Formatter)
 * @see httl.spi.translators.InterpretedTranslator#setFormatter(Formatter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class NumberFormatter extends AbstractFormatter<Number> {
	
	private String numberFormat;
	
	/**
	 * httl.properties: number.format=###,##0.###
	 */
	public void setNumberFormat(String numberFormat) {
		new DecimalFormat(numberFormat).format(0);
		this.numberFormat = numberFormat;
	}

	public String toString(String key, Number value) {
		return NumberUtils.format(value, numberFormat);
	}

}