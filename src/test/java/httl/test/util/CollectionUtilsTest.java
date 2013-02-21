package httl.test.util;

import junit.framework.Assert;
import httl.internal.util.CollectionUtils;

import org.junit.Test;

public class CollectionUtilsTest {

	@Test
	public void testRemoveArray() throws Exception {
		String[] result = CollectionUtils.remove(new String[] {"a", "b", "c"}, "a");
		Assert.assertEquals(2, result.length);
		Assert.assertEquals("b", result[0]);
		Assert.assertEquals("c", result[1]);
		
		result = CollectionUtils.remove(new String[] {"a", "b", "c"}, "b");
		Assert.assertEquals(2, result.length);
		Assert.assertEquals("a", result[0]);
		Assert.assertEquals("c", result[1]);
		
		result = CollectionUtils.remove(new String[] {"a", "b", "c"}, "c");
		Assert.assertEquals(2, result.length);
		Assert.assertEquals("a", result[0]);
		Assert.assertEquals("b", result[1]);
		
		result = CollectionUtils.remove(new String[] {"a", "b", "a", "c"}, "a");
		Assert.assertEquals(2, result.length);
		Assert.assertEquals("b", result[0]);
		Assert.assertEquals("c", result[1]);
	}

}
