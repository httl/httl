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

import java.text.ParseException;

/**
 * Object Codec. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Codec extends Formatter<Object> {

	/**
	 * Get the codec format.
	 * 
	 * @return format
	 */
	String getFormat();

	/**
	 * The string is decodable by this codec.
	 * 
	 * @param string - encoded string
	 * @return decodable
	 */
	boolean isValueOf(String string);

	/**
	 * The string is decodable by this codec.
	 * 
	 * @param chars - encoded string
	 * @return decodable
	 */
	boolean isValueOf(char[] chars);

	/**
	 * The string is decodable by this codec.
	 * 
	 * @param bytes - encoded string
	 * @return decodable
	 */
	boolean isValueOf(byte[] bytes);

	/**
	 * Decode the string as a bean object.
	 * 
	 * @param string - encoded string
	 * @param type - bean type
	 * @return bean object
	 * @throws ParseException If the string cannot be parsed
	 */
	<T> T valueOf(String string, Class<T> type) throws ParseException;

	/**
	 * Decode the string as a bean object.
	 * 
	 * @param chars - encoded string
	 * @param type - bean type
	 * @return bean object
	 * @throws ParseException If the string cannot be parsed
	 */
	<T> T valueOf(char[] chars, Class<T> type) throws ParseException;

	/**
	 * Decode the string as a bean object.
	 * 
	 * @param bytes - encoded string
	 * @param type - bean type
	 * @return bean object
	 * @throws ParseException If the string cannot be parsed
	 */
	<T> T valueOf(byte[] bytes, Class<T> type) throws ParseException;

}