package httl.test.script;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.Assert;

import org.junit.Test;

public class ScriptTest {

	@Test
	public void testScript() throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		manager.put("welcome", "hello"); // 设置全局变量
		ScriptEngine engine = manager.getEngineByName("httl");
		engine.put("page", "test"); // 设置引擎变量
		Bindings bindings = engine.createBindings();
		bindings.put("user", "liangfei"); // 设置执行变量
		String result = (String) engine.eval("<!--#var(String welcome, String page, String user)-->${welcome}, ${user}, this is ${page} page."); // 执行表达式
		Assert.assertEquals("hello, liangfei, this is test page.", result);
	}

}
