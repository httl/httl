package httl.spi.filters;

import org.junit.Assert;
import org.junit.Test;

public class CommentSyntaxFilterTest {

	@Test
	public void testTrimComment() throws Exception {
		CommentSyntaxFilter filter = new CommentSyntaxFilter();
		filter.setCommentLeft("<!--");
		filter.setCommentRight("-->");
		Assert.assertEquals("xxx", filter.filter("", "xxx"));
		Assert.assertEquals("<!--yyy-->", filter.filter("", "<!--yyy-->"));
		Assert.assertEquals(" <!-- zzz --> ", filter.filter("", " <!-- zzz --> "));
		
		Assert.assertEquals("aaa", filter.filter("", "aaa<!--"));
		Assert.assertEquals("bbb", filter.filter("", "-->bbb"));
		Assert.assertEquals("ccc", filter.filter("", "-->ccc<!--"));
		
		Assert.assertEquals("aaa", filter.filter("", "aaa<!-- "));
		Assert.assertEquals("bbb", filter.filter("", " -->bbb"));
		Assert.assertEquals("ccc", filter.filter("", " -->ccc<!-- "));
	}

}
