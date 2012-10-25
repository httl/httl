/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * NumberUtils. (Tool, Static, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class NumberUtils {

    private static final String DEFAULT_FORMAT = "###,##0.###";

    private static final ThreadLocal<Map<String, DecimalFormat>> LOCAL = new ThreadLocal<Map<String, DecimalFormat>>();

    public static DecimalFormat getDecimalFormat(String format) {
        if (format == null || format.length() == 0) {
            format = DEFAULT_FORMAT;
        }
        Map<String, DecimalFormat> formatters = LOCAL.get();
        if (formatters == null) {
            formatters= new HashMap<String, DecimalFormat>();
            LOCAL.set(formatters);
        }
        DecimalFormat formatter = formatters.get(format);
        if (formatter == null) {
            formatter = new DecimalFormat(format);
            formatters.put(format, formatter);
        }
        return formatter;
    }
    
    public static String formatNumber(Number value, String format) {
        return getDecimalFormat(format).format(value);
    }
    
}
