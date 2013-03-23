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
package httl.spi.parsers;

import httl.Node;
import httl.internal.util.StringSequence;
import httl.spi.Filter;
import httl.spi.Parser;
import httl.spi.Translator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DefaultTranslator. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setTranslator(Translator)
 * @see httl.spi.parsers.TemplateParser#setTranslator(Translator)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ExpressionParser implements Parser {

	private Filter expressionFilter;

	private String[] importGetters;

	private String[] importPackages;

	private final Map<Class<?>, Object> functions = new ConcurrentHashMap<Class<?>, Object>();

	private final List<StringSequence> sequences = new CopyOnWriteArrayList<StringSequence>();

	private String[] importSizers;
	
	/**
	 * httl.properties: import.sizers=size,length,getSize,getLength
	 */
	public void setImportSizers(String[] importSizers) {
		this.importSizers = importSizers;
	}

	/**
	 * httl.properties: expression.filters=httl.spi.filters.UnescapeXmlFilter
	 */
	public void setExpressionFilter(Filter expressionFilter) {
		this.expressionFilter = expressionFilter;
	}

	/**
	 * httl.properties: import.getters=get,getProperty,getAttribute
	 */
	public void setImportGetters(String[] importGetters) {
		this.importGetters = importGetters;
	}

	/**
	 * httl.properties: import.packages=java.util
	 */
	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	public void setImportMethods(Object[] importMethods) {
		for (Object function : importMethods) {
			if (function instanceof Class) {
				this.functions.put((Class<?>) function, function);
			} else {
				this.functions.put(function.getClass(), function);
			}
		}
	}

	/**
	 * httl.properties: import.sequences=Mon Tue Wed Thu Fri Sat Sun Mon
	 */
	public void setImportSequences(String[] sequences) {
		for (String s : sequences) {
			s = s.trim();
			if (s.length() > 0) {
				String[] ts = s.split("\\s+");
				List<String> sequence = new ArrayList<String>();
				for (String t : ts) {
					t = t.trim();
					if (t.length() > 0) {
						sequence.add(t);
					}
				}
				this.sequences.add(new StringSequence(sequence));
			}
		}
	}

	public Node parse(String source, int offset) throws ParseException {
		if (expressionFilter != null) {
			source = expressionFilter.filter(source, source);
		}
		return new ExpressionScanner(functions.keySet(), sequences, importGetters, importSizers, importPackages, offset).parse(source);
	}

}
