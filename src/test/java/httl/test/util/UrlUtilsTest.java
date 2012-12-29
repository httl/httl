package httl.test.util;

import httl.util.UrlUtils;
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
