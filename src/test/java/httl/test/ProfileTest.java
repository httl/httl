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
import httl.test.model.Book;
import httl.test.model.User;
import httl.test.util.DiscardOutputStream;
import httl.test.util.DiscardWriter;
import httl.util.ClassUtils;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * ProfileTest
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ProfileTest extends TestCase {

    @Test
    public void testTemplate() throws Exception {
    	boolean profile = "true".equals(System.getProperty("profile"));
    	if (! profile) {
    		return;
    	}
        boolean stream = "true".equals(System.getProperty("stream"));
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
        Map<String, Book> bookmap = new TreeMap<String, Book>();
        Map<String, Map<String, Object>> mapbookmap = new TreeMap<String, Map<String, Object>>();
        List<Map<String, Object>> mapbooklist = new ArrayList<Map<String, Object>>();
        for (Book book : books) {
            bookmap.put(book.getTitle().replaceAll("\\s+", ""), book);
            Map<String, Object> genericBook = ClassUtils.getBeanProperties(book);
            mapbookmap.put(book.getTitle().replaceAll("\\s+", ""), genericBook);
            mapbooklist.add(genericBook);
        }
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("user", user);
        context.put("booklist", Arrays.asList(books));
        context.put("bookmap", bookmap);
        context.put("mapbookmap", mapbookmap);
        context.put("mapbooklist", mapbooklist);
        context.put("books", books);
        context.put("emptybooks", new Book[0]);
        OutputStream outputStream = new DiscardOutputStream();
        Writer writer = new DiscardWriter();;
        for(;;) {
        	Engine engine = Engine.getEngine("httl-profile.properties");
        	String dir = engine.getProperty("template.directory");
        	if (dir.length() > 0) {
        		dir += "/";
        	}
	        File directory = new File(this.getClass().getClassLoader().getResource(dir + "templates/").getFile());
	        File[] files = directory.listFiles();
	        for (int i = 0, n = files.length; i < n; i ++) {
	            File file = files[i];
	            /*if (! "block.httl".equals(file.getName())) {
	                continue;
	            }*/
	            //System.out.println(file.getName());
	            Template template = engine.getTemplate("/templates/" + file.getName());
	            try {
	            	if (stream)
	            		template.render(context, outputStream);
	            	else
	            		template.render(context, writer);
	            } catch (Exception e) {
	            	throw new IllegalStateException("\n================================\n" + template.getCode() + "\n================================\n" + e.getMessage(), e);
	            }
	            synchronized (ProfileTest.class) {
	            	ProfileTest.class.wait(50);
				}
	        }
        }
    }

}