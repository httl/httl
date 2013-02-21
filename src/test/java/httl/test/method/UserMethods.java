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

/**
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
public class UserMethods {

	public static String overloadMethod(A1 a1) {
		return "A1";
	}

	public static String overloadMethod(A1B1 a1B1) {
		return "A1B1";
	}

	private UserMethods() {}

	public static String overloadMethod(A1 a1, A2 a2) {
		return "A1";
	}
}
