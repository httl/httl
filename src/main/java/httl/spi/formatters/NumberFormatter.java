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
package httl.spi.formatters;

import httl.spi.Configurable;
import httl.spi.Formatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;


/**
 * NumberFormatter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.Engine#setFormatter(Formatter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class NumberFormatter implements Formatter<Number>, Configurable {
    
    private String numberFormat;
    
    public void configure(Map<String, String> config) {
        String format = config.get(NUMBER_FORMAT);
        if (format != null && format.trim().length() > 0) {
            format = format.trim();
            new DecimalFormat(format).format(0);
            this.numberFormat = format;
        }
    }
    
    public String format(Number value) {
        if (numberFormat == null || numberFormat == null) {
            return NumberFormat.getNumberInstance().format(value);
        }
        return new DecimalFormat(numberFormat).format(value);
    }

}
