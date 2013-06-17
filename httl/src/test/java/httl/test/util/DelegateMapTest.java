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

import httl.util.DelegateMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

public class DelegateMapTest {

	@Test
	public void testIterator() {
		Map<String, String> parent = new HashMap<String, String>();
		parent.put("a", "x");
		parent.put("c", "1");
		Map<String, String> current = new HashMap<String, String>();
		current.put("b", "y");
		current.put("c", "2");
		Map<String, String> delegate = new DelegateMap<String, String>(parent, current);
		delegate.put("c", "z");
		
		assertEquals(5, delegate.size());
		assertEquals("x", delegate.get("a"));
		assertEquals("y", delegate.get("b"));
		assertEquals("z", delegate.get("c"));
		
		Map<String, String> entrySet = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : delegate.entrySet()) {
			entrySet.put(entry.getKey(), entry.getValue());
		}
		assertEquals(3, entrySet.size());
		assertEquals("x", entrySet.get("a"));
		assertEquals("y", entrySet.get("b"));
		assertEquals("z", entrySet.get("c"));
		
		Set<String> keySet = new HashSet<String>();
		for (String key : delegate.keySet()) {
			keySet.add(key);
		}
		assertEquals(3, keySet.size());
		assertTrue(keySet.contains("a"));
		assertTrue(keySet.contains("b"));
		assertTrue(keySet.contains("c"));
		
		Collection<String> values = new HashSet<String>();
		for (String value : delegate.values()) {
			values.add(value);
		}
		assertEquals(5, values.size());
		assertTrue(values.contains("1"));
		assertTrue(values.contains("2"));
		assertTrue(values.contains("x"));
		assertTrue(values.contains("y"));
		assertTrue(values.contains("z"));
	}

}