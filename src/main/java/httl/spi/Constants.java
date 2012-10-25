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
package httl.spi;

/**
 * Constants. (SPI, Static, ThreadSafe)
 * 
 * @see httl.spi.Configurable#configure(java.util.Map)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Constants {

    public static final String CONFIGURE           = "configure";

    public static final String SET_ENGINE          = "setEngine";

    public static final String NULL                = "null";

    public static final String PLUS                = "+";

    public static final String CACHE               = "cache";

    public static final String LOADER              = "loader";

    public static final String LOADERS             = "loaders";

    public static final String PARSER              = "parser";

    public static final String PARSERS             = "parsers";

    public static final String TRANSLATOR          = "translator";

    public static final String COMPILER            = "compiler";

    public static final String FORMATTER           = "formatter";

    public static final String FORMATTERS          = "formatters";

    public static final String FILTER              = "filter";

    public static final String FILTERS             = "filters";

    public static final String TEXT_FILTER         = "text.filter";

    public static final String TEXT_FILTERS        = "text.filters";

    public static final String FUNCTIONS           = "functions";

    public static final String SEQUENCES           = "sequences";

    public static final String ATTRIBUTE_NAMESPACE = "attribute.namespace";

    public static final String FOREACH_STATUS      = "foreach.status";

    public static final String IMPORT_PACKAGES     = "import.packages";

    public static final String TEMPLATE_DIRECTORY  = "template.directory";

    public static final String TEMPLATE_SUFFIX     = "template.suffix";

    public static final String CACHE_CAPACITY      = "cache.capacity";

    public static final String RELOADABLE          = "reloadable";

    public static final String PRECOMPILED         = "precompiled";

    public static final String COMPILE_DIRECTORY   = "compile.directory";

    public static final String JAVA_VERSION        = "java.version";

    public static final String INPUT_ENCODING      = "input.encoding";

    public static final String OUTPUT_ENCODING     = "output.encoding";

    public static final String LOCALE              = "locale";

    public static final String OUTPUT_STREAM       = "output.stream";

    public static final String TIME_ZONE           = "time.zone";

    public static final String DATE_FORMAT         = "date.format";

    public static final String NUMBER_FORMAT       = "number.format";

    public static final String NULL_VALUE          = "null.value";

    public static final String TRUE_VALUE          = "true.value";

    public static final String FALSE_VALUE         = "false.value";

    public static final String HTTL                = "httl";

}
