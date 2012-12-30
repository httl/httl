/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.test;

import httl.Engine;
import httl.Template;
import httl.spi.Loader;
import httl.spi.parsers.template.AdaptiveTemplate;
import httl.test.model.Book;
import httl.test.model.User;
import httl.test.util.DiscardOutputStream;
import httl.test.util.DiscardWriter;
import httl.util.ClassUtils;
import httl.util.IOUtils;
import httl.util.StringUtils;
import httl.util.UnsafeByteArrayOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * TemplateTest
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class TemplateTest extends TestCase {

    @Test
    public void testTemplate() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("+0"));
        User user = new User("liangfei", "admin", "Y");
        Book[] books = new Book[10];
        books[0] = new Book("Practical API Design", "Jaroslav Tulach", "Apress", format.parse("2008-07-29"), 75, 85);
        books[1] = new Book("Effective Java", "Joshua Bloch", "Addison-Wesley Professional", format.parse("2008-05-28"), 55, 70);
        books[2] = new Book("Java Concurrency in Practice", "Doug Lea", "Addison-Wesley Professional", format.parse("2006-05-19"), 60, 60);
        books[3] = new Book("Java Programming Language", "James Gosling", "Prentice Hall", format.parse("2005-08-27"), 65, 75);
        books[4] = new Book("Domain-Driven Design", "Eric Evans", "Addison-Wesley Professional", format.parse("2003-08-30"), 70, 80);
        books[5] = new Book("Agile Project Management with Scrum", "Ken Schwaber", "Microsoft Press", format.parse("2004-03-10"), 40, 80);
        books[6] = new Book("J2EE Development without EJB", "Rod Johnson", "Wrox", format.parse("2011-09-17"), 40, 70);
        books[7] = new Book("Design Patterns", "Erich Gamma", "Addison-Wesley Professional", format.parse("1994-11-10"), 60, 80);
        books[8] = new Book("Agile Software Development, Principles, Patterns, and Practices", " Robert C. Martin", "Prentice Hall", format.parse("2002-10-25"), 80, 75);
        books[9] = new Book("Design by Contract, by Example", "Richard Mitchell", "Addison-Wesley Publishing Company", format.parse("2001-10-22"), 50, 85);
        Book[] books2 = new Book[2];
        books2[0] = new Book("Practical API Design2", "Jaroslav Tulach", "Apress", format.parse("2010-07-29"), 75, 85);
        books2[1] = new Book("Effective Java2", "Joshua Bloch", "Addison-Wesley Professional", format.parse("2010-05-28"), 55, 70);
        Map<String, Book> bookmap = new TreeMap<String, Book>();
        Map<String, Map<String, Object>> mapbookmap = new TreeMap<String, Map<String, Object>>();
        List<Map<String, Object>> mapbooklist = new ArrayList<Map<String, Object>>();
        for (Book book : books) {
            bookmap.put(book.getTitle().replaceAll("\\s+", ""), book);
            Map<String, Object> genericBook = ClassUtils.getBeanProperties(book);
            mapbookmap.put(book.getTitle().replaceAll("\\s+", ""), genericBook);
            mapbooklist.add(genericBook);
        }
        Map<String, Book> bookmap2 = new TreeMap<String, Book>();
        for (Book book : books2) {
            bookmap2.put(book.getTitle().replaceAll("\\s+", ""), book);
        }
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("user", user);
        context.put("books", books);
        context.put("booklist", Arrays.asList(books));
        context.put("bookmap", bookmap);
        context.put("mapbookmap", mapbookmap);
        context.put("mapbooklist", mapbooklist);
        context.put("emptybooks", new Book[0]);
        context.put("books2", books2);
        context.put("booklist2", Arrays.asList(books2));
        context.put("bookmap2", bookmap2);
        String[] configs = new String[] { "httl.properties", "httl-javassist.properties", "httl-attribute.properties" };
        for (String config : configs) {
	        System.out.println("========" + config + "========");
        	Engine engine = Engine.getEngine(config);
        	Loader loader = engine.getProperty("loader", Loader.class);
        	String suffix = engine.getProperty("template.suffix");
        	List<String> list = loader.list(suffix);
        	assertTrue(list.size() > 0);
        	String dir = engine.getProperty("template.directory");
        	if (dir == null) {
        		dir = "";
        	} else {
	        	if (dir.length() > 0 && dir.startsWith("/")) {
	        		dir = dir.substring(1);
	        	}
	        	if (dir.length() > 0 && ! dir.endsWith("/")) {
	        		dir += "/";
	        	}
        	}
	        File directory = new File(this.getClass().getClassLoader().getResource(dir + "templates/").getFile());
	        super.assertTrue(directory.isDirectory());
	        File[] files = directory.listFiles();
	        for (int i = 0, n = files.length; i < n; i ++) {
	            File file = files[i];
	            /*if (! "message.httl".equals(file.getName())) {
	                continue;
	            }*/
	            System.out.println(file.getName());
	            URL url = this.getClass().getClassLoader().getResource(dir + "results/" + file.getName() + ".txt");
	            if (url == null) {
	                throw new FileNotFoundException("Not found file: " + dir + "results/" + file.getName() + ".txt");
	            }
	            File result = new File(url.getFile());
	            if (! result.exists()) {
	                throw new FileNotFoundException("Not found file: " + result.getAbsolutePath());
	            }
	            Template template = engine.getTemplate("/templates/" + file.getName(), Locale.CHINA);
	            super.assertEquals(AdaptiveTemplate.class, template.getClass());
	            super.assertEquals(Locale.CHINA, template.getLocale());
	            String expected = IOUtils.readToString(new FileReader(result));
	            UnsafeByteArrayOutputStream actualStream = new UnsafeByteArrayOutputStream();
	            StringWriter actualWriter = new StringWriter();;
	            try {
	            	template.render(context, actualWriter);
	            	template.render(context, actualStream);
	            } catch (Exception e) {
	            	throw new IllegalStateException("\n================================\n" + template.getCode() + "\n================================\n" + e.getMessage(), e);
	            }
	            super.assertEquals(file.getName(), expected.replace("\r\n", "\\r\\n\r\n").replace("\n", "\\n\n").replace("\t", "\\t\t"), actualWriter.getBuffer().toString().replace("\r\n", "\\r\\n\r\n").replace("\n", "\\n\n").replace("\t", "\\t\t"));
	            super.assertEquals(file.getName(), expected, new String(actualStream.toByteArray()));
	        }
        }
        // parse exception
        {
        	System.out.println("========httl-exception.properties========");
        	Engine engine = Engine.getEngine("httl-exception.properties");
        	String dir = engine.getProperty("template.directory");
        	if (dir == null) {
        		dir = "";
        	} else {
	        	if (dir.length() > 0 && dir.startsWith("/")) {
	        		dir = dir.substring(1);
	        	}
	        	if (dir.length() > 0 && ! dir.endsWith("/")) {
	        		dir += "/";
	        	}
        	}
	        File directory = new File(this.getClass().getClassLoader().getResource(dir + "templates/").getFile());
	        super.assertTrue(directory.isDirectory());
	        File[] files = directory.listFiles();
	        for (int i = 0, n = files.length; i < n; i ++) {
	            File file = files[i];
	            System.out.println(file.getName());
	            URL url = this.getClass().getClassLoader().getResource(dir + "results/" + file.getName() + ".txt");
	            if (url == null) {
	                throw new FileNotFoundException("Not found file: " + dir + "results/" + file.getName() + ".txt");
	            }
	            File result = new File(url.getFile());
	            if (! result.exists()) {
	                throw new FileNotFoundException("Not found file: " + result.getAbsolutePath());
	            }
	            List<String> expected = IOUtils.readLines(new FileReader(result));
	            assertTrue(expected != null && expected.size() > 0);
	            try {
	            	engine.getTemplate("/templates/" + file.getName());
	            	fail(file.getName());
	            } catch (ParseException e) {
	            	String message = e.getMessage();
	            	assertTrue(message != null && message.length() > 0);
            		for (String part : expected)  {
	            		assertTrue(part != null && part.length() > 0);
	            		part = StringUtils.unescapeString(part);
	            		super.assertTrue(file.getName() + ", exception message: \"" + message + "\" not contains: \"" + part + "\"", message.contains(part));
	            	}
	            }
	        }
        }
        // null parameters check
        {
	        context = null;
	        for (String config : configs) {
		        System.out.println("========" + config + " (null parameters)========");
	        	Engine engine = Engine.getEngine(config);
	        	String dir = engine.getProperty("template.directory");
	        	if (dir == null) {
	        		dir = "";
	        	} else {
		        	if (dir.length() > 0 && dir.startsWith("/")) {
		        		dir = dir.substring(1);
		        	}
		        	if (dir.length() > 0 && ! dir.endsWith("/")) {
		        		dir += "/";
		        	}
	        	}
		        File directory = new File(this.getClass().getClassLoader().getResource(dir + "templates/").getFile());
		        super.assertTrue(directory.isDirectory());
		        File[] files = directory.listFiles();
		        for (int i = 0, n = files.length; i < n; i ++) {
		            File file = files[i];
		            System.out.println(file.getName());
		            URL url = this.getClass().getClassLoader().getResource(dir + "results/" + file.getName() + ".txt");
		            if (url == null) {
		                throw new FileNotFoundException("Not found file: " + dir + "results/" + file.getName() + ".txt");
		            }
		            File result = new File(url.getFile());
		            if (! result.exists()) {
		                throw new FileNotFoundException("Not found file: " + result.getAbsolutePath());
		            }
		            Template template = engine.getTemplate("/templates/" + file.getName());
		            super.assertEquals(AdaptiveTemplate.class, template.getClass());
		            try {
		            	template.render(context, new DiscardWriter());
		            	template.render(context, new DiscardOutputStream());
		            } catch (Exception e) {
		            	throw new IllegalStateException("\n================================\n" + template.getCode() + "\n================================\n" + e.getMessage(), e);
		            }
		        }
	        }
        }
    }

}