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
import httl.util.IOUtils;
import httl.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * ExceptionTest
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 * @author Jerry Lee (oldratlee AT gmail DOT com)
 */
@RunWith(Parameterized.class)
public class ExceptionTest {
	static boolean profile = "true".equals(System.getProperty("profile"));

	@Parameterized.Parameters
	public static Collection<Object[]> prepareData() throws Exception {
		if (!profile)
			System.out.println("========httl-exception.properties========");
		Engine engine = Engine.getEngine("httl-exception.properties");

		String dir = engine.getProperty("template.directory", "");
		if (dir.length() > 0 && dir.startsWith("/")) {
			dir = dir.substring(1);
		}
		if (dir.length() > 0 && ! dir.endsWith("/")) {
			dir += "/";
		}
		File directory = new File(ExceptionTest.class.getClassLoader().getResource(dir + "templates/").getFile());
		assertTrue(directory.isDirectory());

		final List<Object[]> retTestData = new ArrayList<Object[]>();
		File[] files = directory.listFiles();
		for (int i = 0, n = files.length; i < n; i ++) {
			File file = files[i];
			// if (! "undefined_method.httl".equals(file.getName())) continue; // 指定模板测试
			retTestData.add(new Object[]{file.getName()});
		}

		return retTestData;
	}

	public ExceptionTest(String templateName) {
		this.templateName = templateName;
	}

	private String templateName;

	@Test
	public void testException() throws Exception {
		boolean profile = "true".equals(System.getProperty("profile"));
		Engine engine = Engine.getEngine("httl-exception.properties");
		String dir = engine.getProperty("template.directory", "");
		if (dir.length() > 0 && dir.startsWith("/")) {
			dir = dir.substring(1);
		}
		if (dir.length() > 0 && ! dir.endsWith("/")) {
			dir += "/";
		}
		URL url = this.getClass().getClassLoader().getResource(dir + "results/" + templateName + ".txt");
		if (url == null) {
			throw new FileNotFoundException("Not found file: " + dir + "results/" + templateName + ".txt");
		}
		File result = new File(url.getFile());
		if (! result.exists()) {
			throw new FileNotFoundException("Not found file: " + result.getAbsolutePath());
		}
		try {
			engine.getTemplate("/templates/" + templateName);
			fail(templateName);
		} catch (ParseException e) {
			e.printStackTrace();
			if (! profile) {
				String message = e.getMessage();
				assertTrue(StringUtils.isNotEmpty(message));
				List<String> expected = IOUtils.readLines(new FileReader(result));
				assertTrue(expected != null && expected.size() > 0);
				for (String part : expected)  {
					assertTrue(StringUtils.isNotEmpty(part));
					part = StringUtils.unescapeString(part).trim();
					assertTrue(templateName + ", No such: \n====\n" + part + "\n====\nin exception message:\n====\n" + message + "\n====\n", message.contains(part));
				}
			}
		}
	}
}