package httl.test;

import httl.Engine;
import httl.test.util.DiscardWriter;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;

public class ProfileTest {

	private static final boolean profile = "true".equals(System.getProperty("profile"));

	@Test
	public void testProfile() throws Exception {
		Writer out = new DiscardWriter();
		Collection<Object[]> datas = TemplateTest.prepareData();
		long max = profile ? Long.MAX_VALUE : 1L;
		for (long m = 0; m < max; m ++) {
			for (Object[] data : datas) {
				String config = (String) data[0];
				Object param = data[1];
				String name = (String) data[2];
				if (! "httl-comment.properties".equals(config) || ! (param instanceof Map))
					continue;
				Engine.getEngine(config).getTemplate("/templates/" + name).render(param, out);
				if (profile) {
					synchronized (TemplateTest.class) {
						try {
							TemplateTest.class.wait(20);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}

}
