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
package httl.test;

import httl.Context;
import httl.Engine;
import httl.Template;
import httl.internal.util.ClassUtils;
import httl.internal.util.IOUtils;
import httl.internal.util.StringUtils;
import httl.internal.util.UnsafeByteArrayOutputStream;
import httl.spi.Codec;
import httl.spi.Loader;
import httl.spi.loaders.ClasspathLoader;
import httl.spi.loaders.MultiLoader;
import httl.spi.parsers.templates.AdaptiveTemplate;
import httl.test.model.Book;
import httl.test.model.Model;
import httl.test.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

	@SuppressWarnings("unchecked")
	@Test
	public void testTemplate() throws Exception {
		boolean profile = "true".equals(System.getProperty("profile"));
		String include = System.getProperty("includes");
		String exclude = System.getProperty("excludes");
		Set<String> includes = new HashSet<String>();
		Set<String> excludes = new HashSet<String>();
		if (StringUtils.isNotEmpty(include) && ! include.startsWith("$")) {
			includes.addAll(Arrays.asList(include.split("\\,")));
		} else if (StringUtils.isNotEmpty(exclude) && ! exclude.startsWith("$")) {
			excludes.addAll(Arrays.asList(exclude.split("\\,")));
		}
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
			Map<String, Object> genericBook = ClassUtils.getProperties(book);
			mapbookmap.put(book.getTitle().replaceAll("\\s+", ""), genericBook);
			mapbooklist.add(genericBook);
		}
		Map<String, Book> bookmap2 = new TreeMap<String, Book>();
		for (Book book : books2) {
			bookmap2.put(book.getTitle().replaceAll("\\s+", ""), book);
		}
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("chinese", "中文");
		context.put("impvar", "abcxyz");
		context.put("defvar", "mnnm");
		context.put("html", "<a href=\"foo.html\">foo</a>");
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
		Model model = new Model();
		model.setChinese("中文");
		model.setImpvar("abcxyz");
		model.setDefvar("mnnm");
		model.setHtml("<a href=\"foo.html\">foo</a>");
		model.setUser(user);
		model.setBooks(books);
		model.setBooklist(Arrays.asList(books));
		model.setBookmap(bookmap);
		model.setMapbookmap(mapbookmap);
		model.setMapbooklist(mapbooklist);
		model.setEmptybooks(new Book[0]);
		model.setBooks2(books2);
		model.setBooklist2(Arrays.asList(books2));
		model.setBookmap2(bookmap2);
		String[] configs = new String[] { "httl-text.properties", "httl-comment.properties", "httl-javassist.properties", "httl-attribute.properties" };
		for (String config : configs) {
			Engine engine = Engine.getEngine(config);
			Codec[] codecs = engine.getProperty("codecs", Codec[].class);
			String json = codecs[0].toString("context", context);
			if (! profile) {
				Loader loader = engine.getProperty("loader", Loader.class);
				assertEquals(MultiLoader.class, loader.getClass());
				Loader[] loaders = engine.getProperty("loaders", Loader[].class);
				assertEquals(ClasspathLoader.class, loaders[0].getClass());
				loader = engine.getProperty("loaders", ClasspathLoader.class);
				assertEquals(ClasspathLoader.class, loader.getClass());
				String[] suffixes = engine.getProperty("template.suffix", new String[] { ".httl" });
				List<String> list = loader.list(suffixes[0]);
				assertTrue(list.size() > 0);
			}
			Object[] maps = new Object[] {context, model/*, json*/, null};
			for (Object map : maps) {
				if (map instanceof String) continue; // FIXME JSON格式的Map没有顺序，断言失败
				if (! profile) {
					System.out.println("========" + config + " (" + (map == null ? "null" : map.getClass().getSimpleName()) + " parameters)========");
				}
				String dir = engine.getProperty("template.directory", "");
				if (dir.length() > 0 && dir.startsWith("/")) {
					dir = dir.substring(1);
				}
				if (dir.length() > 0 && ! dir.endsWith("/")) {
					dir += "/";
				}
				File directory = new File(this.getClass().getClassLoader().getResource(dir + "templates/").getFile());
				assertTrue(directory.isDirectory());
				File[] files = directory.listFiles();
				long max = profile ? Long.MAX_VALUE : 1;
				for (long m = 0; m < max; m ++) {
					for (int i = 0, n = files.length; i < n; i ++) {
						File file = files[i];
						//if (! "switch_filter.httl".equals(file.getName())) continue; // 指定模板测试
						if ("httl-javassist.properties".equals(config)  // FIXME javassist的foreach 1..3编译不过
								&& "list.httl".equals(file.getName())) continue;
						if (! profile)
							System.out.println(file.getName());
						if (excludes.contains(file.getName()) || 
								(includes.size() > 0 && ! includes.contains(file.getName()))) {
							continue;
						}
						String encoding = "UTF-8";
						if ("gbk.httl".equals(file.getName())) {
							encoding = "GBK";
						}
						Engine _engine = engine;
						if ("extends_default.httl".equals(file.getName())) {
							_engine = Engine.getEngine("httl-extends.properties");
						}
						Template template = _engine.getTemplate("/templates/" + file.getName(), Locale.CHINA, encoding);
						if (! profile) {
							super.assertEquals(AdaptiveTemplate.class, template.getClass());
							super.assertEquals(Locale.CHINA, template.getLocale());
						}
						UnsafeByteArrayOutputStream actualStream = new UnsafeByteArrayOutputStream();
						StringWriter actualWriter = new StringWriter();
						if ("extends_var.httl".equals(file.getName())) {
							if (map instanceof Map) {
								((Map<String, Object>) map).put("extends", "default.httl");
							} else if (map instanceof Model) {
								((Model) map).setExtends("default.httl");
							}
						}
						try {
							template.render(map, actualWriter);
							template.render(map, actualStream);
						} catch (Throwable e) {
							System.out.println("\n================================\n" + template.getCode() + "\n================================\n");
							e.printStackTrace();
							throw new IllegalStateException(e.getMessage() + "\n================================\n" + template.getCode() + "\n================================\n", e);
						}
						if ("extends_var.httl".equals(file.getName())) {
							if (map instanceof Map) {
								((Map<String, Object>) map).remove("extends");
							} else if (map instanceof Model) {
								((Model) map).setExtends(null);
							}
						}
						if (! profile && map != null) {
							URL url = this.getClass().getClassLoader().getResource(dir + "results/" + file.getName() + ".txt");
							if (url == null) {
								throw new FileNotFoundException("Not found file: " + dir + "results/" + file.getName() + ".txt");
							}
							File result = new File(url.getFile());
							if (! result.exists()) {
								throw new FileNotFoundException("Not found file: " + result.getAbsolutePath());
							}
							String expected = IOUtils.readToString(new InputStreamReader(new FileInputStream(result), encoding));
							expected = expected.replace("\r", "");
							if ("httl-text.properties".equals(config) 
									&& ! "comment_cdata_escape.httl".equals(file.getName())
									&& ! template.getSource().contains("read(")) {
								expected = expected.replace("<!--", "").replace("-->", "");
							}
							super.assertEquals(file.getName(), expected, actualWriter.getBuffer().toString().replace("\r", ""));
							super.assertEquals(file.getName(), expected, new String(actualStream.toByteArray()).replace("\r", ""));
							if ("set_parameters.httl".equals(file.getName())) {
								super.assertEquals(file.getName(), "abc", Context.getContext().get("title"));
							}
						}
					}
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

}