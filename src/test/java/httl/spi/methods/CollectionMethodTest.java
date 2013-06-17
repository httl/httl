package httl.spi.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class CollectionMethodTest {

	@Test
	public void testRecursiveObject() throws Exception {
		List<Menu> children = new ArrayList<Menu>();
		children.add(new Menu("x"));
		children.add(new Menu("y"));
		children.add(new Menu("z"));
		
		List<Menu> parent = new ArrayList<Menu>();
		parent.add(new Menu("a").setChildren(children));
		parent.add(new Menu("b"));
		parent.add(new Menu("c"));
		
		List<Menu> result = CollectionMethod.recursive(parent, "getChildren");
		
		Assert.assertEquals(6, result.size());
		Assert.assertEquals("a", result.get(0).getName());
		Assert.assertEquals("x", result.get(1).getName());
		Assert.assertEquals("y", result.get(2).getName());
		Assert.assertEquals("z", result.get(3).getName());
		Assert.assertEquals("b", result.get(4).getName());
		Assert.assertEquals("c", result.get(5).getName());
	}

	@Test
	public void testRecursiveList() throws Exception {
		List<Object> children = new ArrayList<Object>();
		children.add("x");
		children.add("y");
		children.add("z");
		
		List<Object> parent = new ArrayList<Object>();
		parent.add("a");
		parent.add(children);
		parent.add("b");
		parent.add("c");
		
		List<Object> result = CollectionMethod.recursive(parent);
		
		Assert.assertEquals(6, result.size());
		Assert.assertEquals("a", result.get(0));
		Assert.assertEquals("x", result.get(1));
		Assert.assertEquals("y", result.get(2));
		Assert.assertEquals("z", result.get(3));
		Assert.assertEquals("b", result.get(4));
		Assert.assertEquals("c", result.get(5));
	}

	@Test
	public void testRecursiveMap() throws Exception {
		Map<String, Object> children = new HashMap<String, Object>();
		children.put("4", "x");
		children.put("5", "y");
		children.put("6", "z");
		
		Map<String, Object> parent = new HashMap<String, Object>();
		parent.put("1", "a");
		parent.put("children", children);
		parent.put("2", "b");
		parent.put("3", "c");
		
		Map<String, Object> result = CollectionMethod.recursive(parent);
		
		Assert.assertEquals(6, result.size());
		Assert.assertEquals("a", result.get("1"));
		Assert.assertEquals("x", result.get("4"));
		Assert.assertEquals("y", result.get("5"));
		Assert.assertEquals("z", result.get("6"));
		Assert.assertEquals("b", result.get("2"));
		Assert.assertEquals("c", result.get("3"));
	}

}
