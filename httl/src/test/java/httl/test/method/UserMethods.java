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
package httl.test.method;

import httl.Template;

import java.text.ParseException;

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class UserMethods {

	private UserMethods() {}

	public static String overloadMethod(A1 a1) {
		return "A1";
	}

	public static String overloadMethod(A1B1 a1B1) {
		return "A1B1";
	}

	public static String overloadMethod(A1 a1, A2 a2) {
		return "A1";
	}

	public static String appendHello(Template macro) throws ParseException {
		Object result = macro.evaluate();
		if (result instanceof byte[]) {
			result = new String((byte[]) result);
		}
		return "Hello: " + result;
	}

	public static String overrideMethod(int i) throws ParseException {
		return "i:" + i;
	}

	public static String overrideMethod(Object i) throws ParseException {
		return "o:" + i;
	}

	public static String boxedMethod(Object i) throws ParseException {
		return "boxed:" + i;
	}

}