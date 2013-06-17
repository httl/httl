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

/**
 * Value Formatter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setFormatter(Formatter)
 * @see httl.spi.translators.InterpretedTranslator#setFormatter(Formatter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Formatter<T> {

	/**
	 * Format the value to a string.
	 * 
	 * @param key - expression key.
	 * @param value - object value.
	 * @return string value
	 */
	String toString(String key, T value);

	/**
	 * Format the value to a char array.
	 * 
	 * @param key - expression key.
	 * @param value - object value.
	 * @return char array value
	 */
	char[] toChars(String key, T value);

	/**
	 * Format the value to a byte array.
	 * 
	 * @param key - expression key.
	 * @param value - object value.
	 * @return byte array value
	 */
	byte[] toBytes(String key, T value);

}