package httl.test.util;

import static org.junit.Assert.assertEquals;
import httl.internal.util.CollectionUtils;

import org.junit.Test;

public class CollectionUtilsTest {

	@Test
	public void testRemoveArray() throws Exception {
		String[] result = CollectionUtils.remove(new String[] {"a", "b", "c"}, "a");
		assertEquals(2, result.length);
		assertEquals("b", result[0]);
		assertEquals("c", result[1]);
		
		result = CollectionUtils.remove(new String[] {"a", "b", "c"}, "b");
		assertEquals(2, result.length);
		assertEquals("a", result[0]);
		assertEquals("c", result[1]);
		
		result = CollectionUtils.remove(new String[] {"a", "b", "c"}, "c");
		assertEquals(2, result.length);
		assertEquals("a", result[0]);
		assertEquals("b", result[1]);
		
		result = CollectionUtils.remove(new String[] {"a", "b", "a", "c"}, "a");
		assertEquals(2, result.length);
		assertEquals("b", result[0]);
		assertEquals("c", result[1]);
	}

}
