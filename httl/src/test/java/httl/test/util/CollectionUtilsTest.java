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

import static org.junit.Assert.assertEquals;
import httl.util.CollectionUtils;

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