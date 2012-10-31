package httl.test;

import httl.test.model.Book;
import httl.test.model.User;
import httl.test.performance.BeetlCase;
import httl.test.performance.Case;
import httl.test.performance.Counter;
import httl.test.performance.FreemarkerCase;
import httl.test.performance.HttlCase;
import httl.test.performance.JavaCase;
import httl.test.performance.Smarty4jCase;
import httl.test.performance.VelocityCase;
import httl.util.IgnoredWriter;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;

public class PerformanceTest {

    @Test
    public void testPerformance() throws Exception {
    	int count = getProperty("count", 10000);
        int size = getProperty("size", 100);
        Random random = new Random();
        Book[] books = new Book[size];
        for (int i = 0; i < size; i ++) {
            books[i] = new Book(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Date(), random.nextInt(100) + 10, random.nextInt(60) + 30);
        }
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("user", new User("liangfei", "admin", "Y"));
        context.put("books", books);
        Case[] cases = new Case[] { new BeetlCase(), new Smarty4jCase(), new FreemarkerCase(), new VelocityCase(), new HttlCase(), new JavaCase() };
        System.out.println("=======test environment========");
        System.out.println("os: " + System.getProperty("os.name") + " " + System.getProperty("os.version")  + ", cpu: " + Runtime.getRuntime().availableProcessors() 
        		+ ", jvm: " + System.getProperty("java.version") + ", memory: " + Runtime.getRuntime().totalMemory()
        		+ ", count: " + count + ", size: " + size);
        for (int i = 0; i < cases.length; i ++) {
        	Case c = cases[i];
        	String name = c.getClass().getSimpleName().replace("Case", "");
            System.out.println("========" + name.toLowerCase() + "========");
            Counter counter = new Counter();
            StringWriter writer = new StringWriter();
            c.count("books", new HashMap<String, Object>(context), writer, new IgnoredWriter(), count, counter);
            System.out.println("initialize: " + counter.getInitialized() + "ms, " +
            		"compile: " + counter.getCompiled() + "ms, " +
            		"first: " + counter.getExecuted() + "ms/" + writer.getBuffer().length() + "b, " +
            		"total: " + counter.getFinished() + "ms/" + count + ", " +
            		"tps: " + (counter.getFinished() == 0 ? 0L : (1000L * count / counter.getFinished())) + "/s.");
        }
    }
    
    private static int getProperty(String key, int defaultValue) {
    	String value = System.getProperty(key);
    	if (value != null && value.length() > 0 && value.matches("\\d+")) {
    		return Integer.parseInt(value);
    	}
    	return defaultValue;
    }
    
    public static void main(String[] args) throws Exception {
        new PerformanceTest().testPerformance();
    }
    
}
