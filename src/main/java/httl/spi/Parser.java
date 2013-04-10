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

import httl.Node;

import java.io.IOException;
import java.text.ParseException;

/**
 * Template Parser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTemplateParser(Parser)
 * @see httl.spi.translators.InterpretedTranslator#setTemplateParser(Parser)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Parser {

	/**
	 * Parse the template/expression.
	 * 
	 * @param source - template/expression source.
	 * @param offset - template/expression offset.
	 * @return template/expression root node
	 */
	Node parse(String source, int offset) throws IOException, ParseException;

}