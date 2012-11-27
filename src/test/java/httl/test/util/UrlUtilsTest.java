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

}
