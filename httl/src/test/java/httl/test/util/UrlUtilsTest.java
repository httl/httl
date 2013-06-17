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
package httl.test.util;

import httl.util.UrlUtils;

import org.junit.Test;
import static org.junit.Assert.*;

public class UrlUtilsTest {

	@Test
	public void testCleanName() {
		assertEquals("/a/b/c", UrlUtils.cleanName("/a/b/c"));
		assertEquals("/a/b/c", UrlUtils.cleanName("a/b/c"));
		assertEquals("/a/b/c", UrlUtils.cleanName("\\a\\b\\c"));
		assertEquals("/a/b/c", UrlUtils.cleanName("a\\b\\c"));
	}

	@Test
	public void testCleanDirectory() {
		assertEquals("/a/b/c", UrlUtils.cleanDirectory("/a/b/c"));
		assertEquals("/a/b/c", UrlUtils.cleanDirectory("a/b/c"));
		assertEquals("/a/b/c", UrlUtils.cleanDirectory("\\a\\b\\c"));
		assertEquals("/a/b/c", UrlUtils.cleanDirectory("a\\b\\c"));
		assertEquals("/a/b/c", UrlUtils.cleanDirectory("/a/b/c/"));
		assertEquals("/a/b/c", UrlUtils.cleanDirectory("a/b/c/"));
		assertEquals("/a/b/c", UrlUtils.cleanDirectory("\\a\\b\\c\\"));
		assertEquals("/a/b/c", UrlUtils.cleanDirectory("a\\b\\c\\"));
	}

	@Test
	public void testGetParentLevel() {
		assertEquals(0, UrlUtils.getParentLevel("/a/b/c"));
		assertEquals(0, UrlUtils.getParentLevel("a/b/c"));
		assertEquals(1, UrlUtils.getParentLevel("../a/b/c"));
		assertEquals(2, UrlUtils.getParentLevel("../../a/b/c"));
		assertEquals(0, UrlUtils.getParentLevel("a/./c"));
		assertEquals(0, UrlUtils.getParentLevel("./a/b/c"));
		assertEquals(0, UrlUtils.getParentLevel("./a/./c"));
		assertEquals(0, UrlUtils.getParentLevel("a/../c"));
		assertEquals(0, UrlUtils.getParentLevel("a/b/c/../"));
		assertEquals(1, UrlUtils.getParentLevel("../a/../c"));
		assertEquals(1, UrlUtils.getParentLevel("../a/../c/../"));
		assertEquals(0, UrlUtils.getParentLevel("\\a\\b\\c"));
		assertEquals(0, UrlUtils.getParentLevel("a\\b\\c"));
		assertEquals(1, UrlUtils.getParentLevel("..\\a\\b\\c"));
		assertEquals(2, UrlUtils.getParentLevel("..\\..\\a\\b\\c"));
		assertEquals(0, UrlUtils.getParentLevel("a\\.\\c"));
		assertEquals(0, UrlUtils.getParentLevel(".\\a\\b\\c"));
		assertEquals(0, UrlUtils.getParentLevel(".\\a\\.\\c"));
		assertEquals(0, UrlUtils.getParentLevel("a\\..\\c"));
		assertEquals(0, UrlUtils.getParentLevel("a\\b\\c\\..\\"));
		assertEquals(1, UrlUtils.getParentLevel("..\\a\\..\\c"));
		assertEquals(1, UrlUtils.getParentLevel("..\\a\\..\\c\\..\\"));
	}

	@Test
	public void testGetParentDirectory() {
		assertEquals("/a/b/", UrlUtils.getParentDirectory("/a/b/c", 0));
		assertEquals("/a/", UrlUtils.getParentDirectory("/a/b/c", 1));
		assertEquals("/", UrlUtils.getParentDirectory("/a/b/c", 2));
		assertEquals("a/b/", UrlUtils.getParentDirectory("a/b/c", 0));
		assertEquals("a/", UrlUtils.getParentDirectory("a/b/c", 1));
		assertEquals("/", UrlUtils.getParentDirectory("a/b/c", 2));
		assertEquals("\\a\\b\\", UrlUtils.getParentDirectory("\\a\\b\\c", 0));
		assertEquals("\\a\\", UrlUtils.getParentDirectory("\\a\\b\\c", 1));
		assertEquals("\\", UrlUtils.getParentDirectory("\\a\\b\\c", 2));
		assertEquals("a\\b\\", UrlUtils.getParentDirectory("a\\b\\c", 0));
		assertEquals("a\\", UrlUtils.getParentDirectory("a\\b\\c", 1));
		assertEquals("/", UrlUtils.getParentDirectory("a\\b\\c", 2));
	}

}