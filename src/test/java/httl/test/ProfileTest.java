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
package httl.test;

import httl.Engine;
import httl.test.util.DiscardWriter;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;

public class ProfileTest {

	private static final boolean profile = "true".equals(System.getProperty("profile"));

	@Test
	public void testProfile() throws Exception {
		Writer out = new DiscardWriter();
		Collection<Object[]> datas = TemplateTest.prepareData();
		long max = profile ? Long.MAX_VALUE : 1L;
		for (long m = 0; m < max; m ++) {
			for (Object[] data : datas) {
				String config = (String) data[0];
				Object param = data[1];
				String name = (String) data[2];
				if (! "httl-comment.properties".equals(config) || ! (param instanceof Map))
					continue;
				Engine.getEngine(config).getTemplate("/templates/" + name).render(param, out);
				if (profile) {
					synchronized (TemplateTest.class) {
						try {
							TemplateTest.class.wait(20);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}

}