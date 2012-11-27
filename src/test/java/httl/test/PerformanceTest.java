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
import httl.test.util.DiscardWriter;

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
        int list = getProperty("list", 100);
        Random random = new Random();
        Book[] books = new Book[list];
        for (int i = 0; i < list; i ++) {
            books[i] = new Book(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Date(), random.nextInt(100) + 10, random.nextInt(60) + 30);
        }
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("user", new User("liangfei", "admin", "Y"));
        context.put("books", books);
        Case[] cases = new Case[] { new BeetlCase(), new Smarty4jCase(), new FreemarkerCase(), new VelocityCase(), new HttlCase(), new JavaCase() };
        System.out.println("=======test environment========");
        System.out.println("os: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " "+ System.getProperty("os.arch")
        		+ ", cpu: " + Runtime.getRuntime().availableProcessors() + " cores, jvm: " + System.getProperty("java.version") + ", \nmemory: max: " + Runtime.getRuntime().maxMemory() 
        		+ ", total: " + Runtime.getRuntime().totalMemory() + ", free: " + Runtime.getRuntime().freeMemory() 
        		+ ", use: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        System.out.println("=======test parameters========");
        System.out.println("count: " + count + ", list: " + list);
        for (int i = 0; i < cases.length; i ++) {
        	Case c = cases[i];
        	String name = c.getClass().getSimpleName().replace("Case", "");
            System.out.println("========" + name.toLowerCase() + "========");
            Counter counter = new Counter();
            StringWriter writer = new StringWriter();
            // 当开启stream模式后，假设你的原生输出为byte[]流输出，比如HTTP的响应流。
            // 这样Writer实际是包装流的，所有输出String都需要编码成byte[]流，下面使用StreamWriter模拟。
            // 否则将不公平，因为httl会将静态文本编译byte[]，并为动态插值增加getBytes()。
            // 这个getBytes()实际是将Writer的转码提前了，是会增加开销的，但会减少Writer的开销。
            c.count(counter, count, "books", new HashMap<String, Object>(context), writer, new DiscardWriter());
            System.out.println("init: " + counter.getInitialized() + "ms, " +
            		"compile: " + counter.getCompiled() + "ms, " +
            		"first: " + counter.getExecuted() + "ms/" + writer.getBuffer().length() + "byte, " +
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
