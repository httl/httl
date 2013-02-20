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

import httl.internal.util.UrlUtils;
import junit.framework.Assert;

import org.junit.Test;

public class UrlUtilsTest {

	@Test
	public void testCleanName() {
		Assert.assertEquals("/a/b/c", UrlUtils.cleanName("/a/b/c"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanName("a/b/c"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanName("\\a\\b\\c"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanName("a\\b\\c"));
	}

	@Test
	public void testCleanDirectory() {
		Assert.assertEquals("/a/b/c", UrlUtils.cleanDirectory("/a/b/c"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanDirectory("a/b/c"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanDirectory("\\a\\b\\c"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanDirectory("a\\b\\c"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanDirectory("/a/b/c/"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanDirectory("a/b/c/"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanDirectory("\\a\\b\\c\\"));
		Assert.assertEquals("/a/b/c", UrlUtils.cleanDirectory("a\\b\\c\\"));
	}

	@Test
	public void testGetParentLevel() {
		Assert.assertEquals(0, UrlUtils.getParentLevel("/a/b/c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("a/b/c"));
		Assert.assertEquals(1, UrlUtils.getParentLevel("../a/b/c"));
		Assert.assertEquals(2, UrlUtils.getParentLevel("../../a/b/c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("a/./c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("./a/b/c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("./a/./c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("a/../c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("a/b/c/../"));
		Assert.assertEquals(1, UrlUtils.getParentLevel("../a/../c"));
		Assert.assertEquals(1, UrlUtils.getParentLevel("../a/../c/../"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("\\a\\b\\c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("a\\b\\c"));
		Assert.assertEquals(1, UrlUtils.getParentLevel("..\\a\\b\\c"));
		Assert.assertEquals(2, UrlUtils.getParentLevel("..\\..\\a\\b\\c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("a\\.\\c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel(".\\a\\b\\c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel(".\\a\\.\\c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("a\\..\\c"));
		Assert.assertEquals(0, UrlUtils.getParentLevel("a\\b\\c\\..\\"));
		Assert.assertEquals(1, UrlUtils.getParentLevel("..\\a\\..\\c"));
		Assert.assertEquals(1, UrlUtils.getParentLevel("..\\a\\..\\c\\..\\"));
	}

	@Test
	public void testGetParentDirectory() {
		Assert.assertEquals("/a/b/", UrlUtils.getParentDirectory("/a/b/c", 0));
		Assert.assertEquals("/a/", UrlUtils.getParentDirectory("/a/b/c", 1));
		Assert.assertEquals("/", UrlUtils.getParentDirectory("/a/b/c", 2));
		Assert.assertEquals("a/b/", UrlUtils.getParentDirectory("a/b/c", 0));
		Assert.assertEquals("a/", UrlUtils.getParentDirectory("a/b/c", 1));
		Assert.assertEquals("/", UrlUtils.getParentDirectory("a/b/c", 2));
		Assert.assertEquals("\\a\\b\\", UrlUtils.getParentDirectory("\\a\\b\\c", 0));
		Assert.assertEquals("\\a\\", UrlUtils.getParentDirectory("\\a\\b\\c", 1));
		Assert.assertEquals("\\", UrlUtils.getParentDirectory("\\a\\b\\c", 2));
		Assert.assertEquals("a\\b\\", UrlUtils.getParentDirectory("a\\b\\c", 0));
		Assert.assertEquals("a\\", UrlUtils.getParentDirectory("a\\b\\c", 1));
		Assert.assertEquals("/", UrlUtils.getParentDirectory("a\\b\\c", 2));
	}

}