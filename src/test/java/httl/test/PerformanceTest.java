package httl.test;

import httl.test.model.Book;
import httl.test.model.User;
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
        int size = 100;
        int times = 1000;
        Random random = new Random();
        Book[] books = new Book[size];
        for (int i = 0; i < size; i ++) {
            books[i] = new Book(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new Date(), random.nextInt(100) + 10, random.nextInt(60) + 30);
        }
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("user", new User("liangfei", "admin"));
        context.put("books", books);
        Case[] cases = new Case[] {new FreemarkerCase(), new VelocityCase(), new Smarty4jCase(), new HttlCase(), new JavaCase()};
        for (int i = 0; i < cases.length; i ++) {
            System.out.println("=============");
            Case c = cases[i];
            Counter counter = new Counter();
            StringWriter writer = new StringWriter();
            c.count("books", new HashMap<String, Object>(context), writer, new IgnoredWriter(), times, counter);
            System.out.println(c.getClass().getSimpleName().replace("Case", "") + ": initialize: " + counter.getInitialized() + "ms, compile: " + counter.getCompiled() + "ms, first: " + counter.getExecuted() + "ms/" + writer.getBuffer().length() + "b, total: " + counter.getFinished() + "ms/" + times + "t, tps: " + (counter.getFinished() == 0 ? 0 : (1000 * times / counter.getFinished())) + "t/s.");
        }
    }
    
    public static void main(String[] args) throws Exception {
        new PerformanceTest().testPerformance();
    }
    
}
