/*
 * Copyright 1999-2012 Alibaba Group.
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
package httl.test;

import java.util.regex.Pattern;

public class Xxx {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("(\\s*=>?\\s*)");
		System.out.println(pattern.matcher("a=b").find());
		System.out.println(pattern.matcher("a=>b").find());
		System.out.println(pattern.matcher("a = b").find());
		System.out.println(pattern.matcher("a => b").find());
		System.out.println(pattern.matcher("a = > b").find());
		System.out.println(pattern.matcher("a>b").find());
	}

}
