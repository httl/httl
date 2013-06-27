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
package httl.test.util;

import static org.junit.Assert.assertEquals;
import httl.util.StringUtils;

import java.text.DecimalFormat;

import org.junit.Test;

public class StringUtilsTest {
	static boolean profile = "true".equals(System.getProperty("profile"));

	@Test
	public void testEscapeString() {
		assertEquals("a\\\"b\\\"c\\\'d\\\'e\\\\1\\t2\\n3\\r4\\b5\\f6", StringUtils.escapeString("a\"b\"c\'d\'e\\1\t2\n3\r4\b5\f6"));
	}

	@Test
	public void testUnescapeString() {
		assertEquals("''\"", StringUtils.unescapeString("'\\'\\\""));
		assertEquals("a\"b\"c\'d\'e\\1\t2\n3\r4\b5\f6", StringUtils.unescapeString("a\\\"b\\\"c\\\'d\\\'e\\\\1\\t2\\n3\\r4\\b5\\f6"));
	}

	@Test
	public void escapeXmlBytes() {
	    assertEquals("中&lt;文&gt;字&quot;符", new String(StringUtils.escapeXml("中<文>字\"符".getBytes())));
	}

	@Test
	public void testEscapeXmlPerformance() {
		String html = "a<table border=\"0\" color=\'red\'>b&lt;c</table>d";
		//char[] html = "a<table border=\"0\" color=\'red\'>b&lt;c</table>d".toCharArray();

		int count = profile ? 10 * 1000 * 1000 : 1000;
		long start = System.nanoTime();
		for (int i = 0; i < count; i ++) {
			StringUtils.escapeXml(html);
		}
		long elapsed = System.nanoTime() - start;
		DecimalFormat format = new DecimalFormat("###,##0.###");
		System.out.println("elapsed: " + format.format(elapsed) + "ns, tps: " + format.format(1000L * 1000L * 1000L * (long) count / elapsed));
	}

	@Test
	public void testEscapeXmlChar() {
		assertEquals("abcd", new String(StringUtils.escapeXml("abcd".toCharArray())));
		assertEquals("a&lt;table border=&quot;0&quot; color=&apos;red&apos;&gt;b&amp;lt;c&lt;/table&gt;d", new String(StringUtils.escapeXml("a<table border=\"0\" color=\'red\'>b&lt;c</table>d".toCharArray())));
	}

	@Test
	public void testEscapeXml() {
		assertEquals("abcd", StringUtils.escapeXml("abcd"));
		assertEquals("a&lt;table border=&quot;0&quot; color=&apos;red&apos;&gt;b&amp;lt;c&lt;/table&gt;d", StringUtils.escapeXml("a<table border=\"0\" color=\'red\'>b&lt;c</table>d"));
	}

	@Test
	public void testUnescapeXml() {
		assertEquals("abcd", StringUtils.unescapeXml("abcd"));
		assertEquals("a<table border=\"0\" color=\'red\'>b&lt;c</table>d", StringUtils.unescapeXml("a&lt;table border=&quot;0&quot; color=&apos;red&apos;&gt;b&amp;lt;c&lt;/table&gt;d"));
	}

	@Test
	public void testTrimBlankLine() {
		assertEquals("12345678", StringUtils.trimBlankLine("12345678"));
		assertEquals("", StringUtils.escapeString(StringUtils.trimBlankLine("\t\r\b\f ")));
		assertEquals("", StringUtils.escapeString(StringUtils.trimBlankLine("\n\t\b\f ")));
		assertEquals("", StringUtils.escapeString(StringUtils.trimBlankLine("\t\b\f \n")));
		assertEquals(StringUtils.escapeString("\t\r"), StringUtils.escapeString(StringUtils.trimBlankLine("\n\t\r\b\f ")));
		assertEquals(StringUtils.escapeString("\b\f \n"), StringUtils.escapeString(StringUtils.trimBlankLine("\t\r\b\f \n")));
		assertEquals(StringUtils.escapeString("\b\f \n\t\n"), StringUtils.escapeString(StringUtils.trimBlankLine("\t\r\b\f \n\t\n")));
		assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n\t\r\b\f\n"), StringUtils.escapeString(StringUtils.trimBlankLine("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n\t\r\b\f\n")));
		assertEquals(StringUtils.escapeString("\b\f\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r"), StringUtils.escapeString(StringUtils.trimBlankLine(" \t\r\b\f\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f ")));
		assertEquals(StringUtils.escapeString("\t\r\b\f\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f\n"), StringUtils.escapeString(StringUtils.trimBlankLine(" \t\n\t\r\b\f\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f\n\t ")));
		
		//LF
		assertEquals("", StringUtils.trimBlankLine("\n"));
		assertEquals("\n", StringUtils.trimBlankLine("\n\n"));

		//CRLF
		assertEquals("", StringUtils.trimBlankLine("\r\n"));
		assertEquals("\r\n", StringUtils.trimBlankLine("\r\n\r\n"));

		//CR
		assertEquals("", StringUtils.trimBlankLine("\r"));
		assertEquals("\r", StringUtils.trimBlankLine("\r\r"));

		//Mix
		assertEquals("\r\n", StringUtils.trimBlankLine("\r\r\n"));

		//with blanks
		assertEquals("", StringUtils.trimBlankLine(" \t  \n    \t      "));
		assertEquals("\n", StringUtils.trimBlankLine("  \t  \n\n   \t  "));
	}
	
	@Test
	public void testClearBlankLine() {
		assertEquals("12345678", StringUtils.clearBlankLine("12345678"));
		assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n"), StringUtils.escapeString(StringUtils.clearBlankLine("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n\t\r\b\f\n")));
		assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n"), StringUtils.escapeString(StringUtils.clearBlankLine("\n\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f\n\t\n\n")));
		assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n"), StringUtils.escapeString(StringUtils.clearBlankLine(" \t\n\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f\n\t\n\n \t")));
	}

	@Test
	public void testClearBlank() {
		assertEquals("12345678", StringUtils.clearBlank("12345678"));
		assertEquals("12345678", StringUtils.clearBlank("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8"));
		assertEquals("12345678", StringUtils.clearBlank(" 1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8 "));
	}

	@Test
	public void testCompressBlank() {
		assertEquals("12345678", StringUtils.clearBlank("12345678"));
		assertEquals("1 2 3 4 5 6 7 8", StringUtils.compressBlank("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8"));
		assertEquals(" 1 2 3 4 5 6 7 8 ", StringUtils.compressBlank(" 1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8 "));
	}

}