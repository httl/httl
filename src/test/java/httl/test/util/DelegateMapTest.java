/*
 * Copyright 2011-2012 HTTL Team.
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

import junit.framework.Assert;

import org.junit.Test;

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
		
		Assert.assertEquals(5, delegate.size());
		Assert.assertEquals("x", delegate.get("a"));
		Assert.assertEquals("y", delegate.get("b"));
		Assert.assertEquals("z", delegate.get("c"));
		
		Map<String, String> entrySet = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : delegate.entrySet()) {
			entrySet.put(entry.getKey(), entry.getValue());
		}
		Assert.assertEquals(3, entrySet.size());
		Assert.assertEquals("x", entrySet.get("a"));
		Assert.assertEquals("y", entrySet.get("b"));
		Assert.assertEquals("z", entrySet.get("c"));
		
		Set<String> keySet = new HashSet<String>();
		for (String key : delegate.keySet()) {
			keySet.add(key);
		}
		Assert.assertEquals(3, keySet.size());
		Assert.assertTrue(keySet.contains("a"));
		Assert.assertTrue(keySet.contains("b"));
		Assert.assertTrue(keySet.contains("c"));
		
		Collection<String> values = new HashSet<String>();
		for (String value : delegate.values()) {
			values.add(value);
		}
		Assert.assertEquals(5, values.size());
		Assert.assertTrue(values.contains("1"));
		Assert.assertTrue(values.contains("2"));
		Assert.assertTrue(values.contains("x"));
		Assert.assertTrue(values.contains("y"));
		Assert.assertTrue(values.contains("z"));
	}

}