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
import httl.internal.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * DateFormatter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setFormatter(Formatter)
 * @see httl.spi.translators.InterpretedTranslator#setFormatter(Formatter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DateFormatter extends AbstractFormatter<Date> {

	private String dateFormat;

	private TimeZone timeZone;

	/**
	 * httl.properties: date.format=yyyy-MM-dd HH:mm:ss
	 */
	public void setDateFormat(String dateFormat) {
		new SimpleDateFormat(dateFormat).format(new Date());
		this.dateFormat = dateFormat;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = TimeZone.getTimeZone(timeZone);
	}

	public String toString(String key, Date value) {
		return DateUtils.format(value, dateFormat, timeZone);
	}

}