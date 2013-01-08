/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.methods;

import java.util.ArrayList;
import java.util.List;

/**
 * StringMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class StringMethod {

	private StringMethod() {}

	public static String toUnderlineName(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		StringBuilder buf = new StringBuilder(name.length() * 2);
		buf.append(Character.toLowerCase(name.charAt(0)));
		for (int i = 1; i < name.length(); i ++) {
			char c = name.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				buf.append('_');
				buf.append(Character.toLowerCase(c));
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	public static String toCamelName(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		StringBuilder buf = new StringBuilder(name.length());
		boolean upper = false;
		for (int i = 0; i < name.length(); i ++) {
			char c = name.charAt(i);
			if (c == '_') {
				upper = true;
			} else {
				if (upper) {
					upper = false;
					c = Character.toUpperCase(c);
				}
				buf.append(c);
			}
		}
		return buf.toString();
	}

	public static String toCapitalName(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		StringBuilder buf = new StringBuilder(name.length());
		boolean upper = true;
		for (int i = 0; i < name.length(); i ++) {
			char c = name.charAt(i);
			if (c == '_') {
				upper = true;
			} else {
				if (upper) {
					upper = false;
					c = Character.toUpperCase(c);
				}
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	public static String clip(String value, int max) {
		if (value == null || value.length() == 0 || max < 1) {
			return value;
		}
		if (value.length() > max) {
			return value.substring(0, max) + "...";
		}
		return value;
	}

	public static String repeat(String value, int count) {
		if (value == null || value.length() == 0 || count <= 0) {
			return value;
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < count; i ++) {
			buf.append(value);
		}
		return buf.toString();
	}

	public static String[] split(String value, char separator) {
		if (value == null || value.length() == 0) {
			return new String[0];
		}
		List<String> list = new ArrayList<String>();
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < value.length(); i ++) {
			char ch = value.charAt(i);
			if (ch == separator) {
				if (buf.length() > 0) {
					list.add(buf.toString());
					buf.setLength(0);
				}
			} else {
				buf.append(ch);
			}
		}
		if (buf.length() > 0) {
			list.add(buf.toString());
		}
		return list.toArray(new String[list.size()]);
	}

}
