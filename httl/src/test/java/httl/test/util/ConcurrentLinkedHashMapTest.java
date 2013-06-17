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

import httl.util.ConcurrentLinkedHashMap;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConcurrentLinkedHashMapTest {

	@Test
	public void testLru() {
		Map<String, String> map = new ConcurrentLinkedHashMap<String, String>(2);
		map.put("a", "1");
		assertEquals(1, map.size());
		assertEquals("1", map.get("a"));
		map.put("b", "2");
		assertEquals(2, map.size());
		assertEquals("1", map.get("a"));
		assertEquals("2", map.get("b"));
		map.put("c", "3");
		assertEquals(2, map.size());
		assertNull(map.get("a"));
		assertEquals("2", map.get("b"));
		assertEquals("3", map.get("c"));
	}

}