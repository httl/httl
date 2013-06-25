package httl.spi.parsers;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TemplateParserTest {

	@Test
	public void testSplitDefine() throws Exception {
		// without type
		List<String> list = TemplateParser.splitDefine("book");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("book", list.get(0));

		list = TemplateParser.splitDefine("book1, book2");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("book1", list.get(0));
		Assert.assertEquals(" book2", list.get(1));

		list = TemplateParser.splitDefine("book1, book2, book3");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("book1", list.get(0));
		Assert.assertEquals(" book2", list.get(1));
		Assert.assertEquals(" book3", list.get(2));

		// with simple type
		list = TemplateParser.splitDefine("Book book");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("Book book", list.get(0));

		list = TemplateParser.splitDefine("Book book1, Book book2");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Book book1", list.get(0));
		Assert.assertEquals(" Book book2", list.get(1));

		list = TemplateParser.splitDefine("Book book1, Book book2, Book book3");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("Book book1", list.get(0));
		Assert.assertEquals(" Book book2", list.get(1));
		Assert.assertEquals(" Book book3", list.get(2));

		// with generic type
		list = TemplateParser.splitDefine("Map<String, Book> books");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("Map<String, Book> books", list.get(0));

		list = TemplateParser.splitDefine("Map<String, Book> bookMap, Map<String, Book> bookMap2");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Map<String, Book> bookMap", list.get(0));
		Assert.assertEquals(" Map<String, Book> bookMap2", list.get(1));

		list = TemplateParser.splitDefine("Map<String, Book> bookMap, Map<String, Book> bookMap2, Map<String, Book> bookMap3");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("Map<String, Book> bookMap", list.get(0));
		Assert.assertEquals(" Map<String, Book> bookMap2", list.get(1));
		Assert.assertEquals(" Map<String, Book> bookMap3", list.get(2));

		// with multi type
		list = TemplateParser.splitDefine("Book book1, book2");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Book book1", list.get(0));
		Assert.assertEquals(" book2", list.get(1));

		list = TemplateParser.splitDefine("Map<String, Book> bookMap, book");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Map<String, Book> bookMap", list.get(0));
		Assert.assertEquals(" book", list.get(1));

		list = TemplateParser.splitDefine("Map<String, Book> bookMap, Book book");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Map<String, Book> bookMap", list.get(0));
		Assert.assertEquals(" Book book", list.get(1));

		list = TemplateParser.splitDefine("Map<String, Book> bookMap, Book book1, book2");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("Map<String, Book> bookMap", list.get(0));
		Assert.assertEquals(" Book book1", list.get(1));
		Assert.assertEquals(" book2", list.get(2));
		
		list = TemplateParser.splitDefine("book1, Book book2");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("book1", list.get(0));
		Assert.assertEquals(" Book book2", list.get(1));

		list = TemplateParser.splitDefine("book, Map<String, Book> bookMap");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("book", list.get(0));
		Assert.assertEquals(" Map<String, Book> bookMap", list.get(1));

		list = TemplateParser.splitDefine("Book book, Map<String, Book> bookMap");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Book book", list.get(0));
		Assert.assertEquals(" Map<String, Book> bookMap", list.get(1));

		list = TemplateParser.splitDefine("book1, Book book2, Map<String, Book> bookMap");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("book1", list.get(0));
		Assert.assertEquals(" Book book2", list.get(1));
		Assert.assertEquals(" Map<String, Book> bookMap", list.get(2));
	}

	@Test
	public void testSplitAssign() throws Exception {
		// without type
		List<String> list = TemplateParser.splitAssign("book = books.get(0)");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("book = books.get(0)", list.get(0));

		list = TemplateParser.splitAssign("book1 = books.get(0), book2 = books.get(1)");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("book1 = books.get(0)", list.get(0));
		Assert.assertEquals(" book2 = books.get(1)", list.get(1));

		list = TemplateParser.splitAssign("book1 = books.get(0), book2 = books.get(1), book3 = books.get(2)");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("book1 = books.get(0)", list.get(0));
		Assert.assertEquals(" book2 = books.get(1)", list.get(1));
		Assert.assertEquals(" book3 = books.get(2)", list.get(2));

		// with simple type
		list = TemplateParser.splitAssign("Book book = books.get(0)");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("Book book = books.get(0)", list.get(0));

		list = TemplateParser.splitAssign("Book book1 = books.get(0), Book book2 = books.get(1)");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Book book1 = books.get(0)", list.get(0));
		Assert.assertEquals(" Book book2 = books.get(1)", list.get(1));

		list = TemplateParser.splitAssign("Book book1 = books.get(0), Book book2 = books.get(1), Book book3 = books.get(2)");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("Book book1 = books.get(0)", list.get(0));
		Assert.assertEquals(" Book book2 = books.get(1)", list.get(1));
		Assert.assertEquals(" Book book3 = books.get(2)", list.get(2));

		// with generic type
		list = TemplateParser.splitAssign("Map<String, Book> books = books.toMap()");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("Map<String, Book> books = books.toMap()", list.get(0));

		list = TemplateParser.splitAssign("Map<String, Book> bookMap = books.toMap(), Map<String, Book> bookMap2 = books2.toMap()");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Map<String, Book> bookMap = books.toMap()", list.get(0));
		Assert.assertEquals(" Map<String, Book> bookMap2 = books2.toMap()", list.get(1));

		list = TemplateParser.splitAssign("Map<String, Book> bookMap = books.toMap(), Map<String, Book> bookMap2 = books2.toMap(), Map<String, Book> bookMap3 = books3.toMap()");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("Map<String, Book> bookMap = books.toMap()", list.get(0));
		Assert.assertEquals(" Map<String, Book> bookMap2 = books2.toMap()", list.get(1));
		Assert.assertEquals(" Map<String, Book> bookMap3 = books3.toMap()", list.get(2));

		// with multi type
		list = TemplateParser.splitAssign("Map<String, Book> bookMap = books.toMap(), Book book = books.get(0)");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("Map<String, Book> bookMap = books.toMap()", list.get(0));
		Assert.assertEquals(" Book book = books.get(0)", list.get(1));

		list = TemplateParser.splitAssign("Map<String, Book> bookMap = books.toMap(), Book book1 = books.get(0), book2 = books.get(1)");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("Map<String, Book> bookMap = books.toMap()", list.get(0));
		Assert.assertEquals(" Book book1 = books.get(0)", list.get(1));
		Assert.assertEquals(" book2 = books.get(1)", list.get(2));

		// without equals string
		list = TemplateParser.splitAssign("pair = \"a = b\"");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("pair = \"a = b\"", list.get(0));
		
		list = TemplateParser.splitAssign("pair1 = \"a \\\" = b\\\", pair2 = \"a = b\"");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("pair1 = \"a \\\" = b\\\"", list.get(0));
		Assert.assertEquals(" pair2 = \"a = b\"", list.get(1));
		
		// without equals type
		list = TemplateParser.splitAssign("enable = user.role == 'member'");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("enable = user.role == 'member'", list.get(0));
		
		list = TemplateParser.splitAssign("enable1 = user.role == 'member', enable2 = user.role == 'member'");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("enable1 = user.role == 'member'", list.get(0));
		Assert.assertEquals(" enable2 = user.role == 'member'", list.get(1));

		list = TemplateParser.splitAssign("enable1 = user.role == 'member', enable2 = user.role == 'member', enable3 = user.role == 'member'");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("enable1 = user.role == 'member'", list.get(0));
		Assert.assertEquals(" enable2 = user.role == 'member'", list.get(1));
		Assert.assertEquals(" enable3 = user.role == 'member'", list.get(2));
		
		// without compare type
		list = TemplateParser.splitAssign("access = user.age >= 18");
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("access = user.age >= 18", list.get(0));
		
		list = TemplateParser.splitAssign("access1 = user.age >= 18, access2 = user.age >= 18");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("access1 = user.age >= 18", list.get(0));
		Assert.assertEquals(" access2 = user.age >= 18", list.get(1));

		list = TemplateParser.splitAssign("access1 = user.age >= 18, access2 = user.age >= 18, access3 = user.age >= 18");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("access1 = user.age >= 18", list.get(0));
		Assert.assertEquals(" access2 = user.age >= 18", list.get(1));
		Assert.assertEquals(" access3 = user.age >= 18", list.get(2));

		// without multi compare type
		list = TemplateParser.splitAssign("enable = user.role == 'member', access = user.age >= 18");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("enable = user.role == 'member'", list.get(0));
		Assert.assertEquals(" access = user.age >= 18", list.get(1));

		list = TemplateParser.splitAssign("enable = user.role == 'member', disable = user.role != 'member', access = user.age >= 18, forbid = user.age <= 18");
		Assert.assertEquals(4, list.size());
		Assert.assertEquals("enable = user.role == 'member'", list.get(0));
		Assert.assertEquals(" disable = user.role != 'member'", list.get(1));
		Assert.assertEquals(" access = user.age >= 18", list.get(2));
		Assert.assertEquals(" forbid = user.age <= 18", list.get(3));
	}

}
