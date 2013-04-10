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
package httl.internal.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * DateUtils. (Tool, Static, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DateUtils {

	private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final ThreadLocal<SimpleDateFormat> DEFAULT_LOCAL = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(DEFAULT_FORMAT);
		}
	};

	private static final ThreadLocal<Map<String, SimpleDateFormat>> LOCAL = new ThreadLocal<Map<String, SimpleDateFormat>>();

	public static DateFormat getDateFormat(String format, TimeZone timeZone) {
		if (StringUtils.isEmpty(format) || DEFAULT_FORMAT.equals(format)) {
			if (timeZone == null) {
				return DEFAULT_LOCAL.get();
			} else {
				format = DEFAULT_FORMAT;
			}
		}
		Map<String, SimpleDateFormat> formatters = LOCAL.get();
		if (formatters == null) {
			formatters= new HashMap<String, SimpleDateFormat>();
			LOCAL.set(formatters);
		}
		String key = format;
		if (timeZone != null) {
			key += timeZone.getID();
		}
		SimpleDateFormat formatter = formatters.get(key);
		if (formatter == null) {
			formatter = new SimpleDateFormat(format);
			if (timeZone != null) {
				formatter.setTimeZone(timeZone);
			}
			formatters.put(key, formatter);
		}
		return formatter;
	}

	public static String format(Date value) {
		return format(value, DEFAULT_FORMAT, null);
	}

	public static String format(Date value, String format) {
		return format(value, format, null);
	}

	public static Date parse(String value, String format) {
		return parse(value, format, null);
	}

	public static String format(Date value, String format, TimeZone timeZone) {
		if (value == null) {
			return null;
		}
		return getDateFormat(format, timeZone).format(value);
	}

	public static Date parse(String value, String format, TimeZone timeZone) {
		try {
			return getDateFormat(format, timeZone).parse(value);
		} catch (ParseException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}