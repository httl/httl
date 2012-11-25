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
package httl.test.util;

import httl.util.StringUtils;
import junit.framework.Assert;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testEscapeString() {
		Assert.assertEquals("a\\\"b\\\"c\\\'d\\\'e\\\\1\\t2\\n3\\r4\\b5\\f6", StringUtils.escapeString("a\"b\"c\'d\'e\\1\t2\n3\r4\b5\f6"));
	}

	@Test
	public void testUnescapeString() {
		Assert.assertEquals("a\"b\"c\'d\'e\\1\t2\n3\r4\b5\f6", StringUtils.unescapeString("a\\\"b\\\"c\\\'d\\\'e\\\\1\\t2\\n3\\r4\\b5\\f6"));
	}

	@Test
	public void testEscapeHtml() {
		Assert.assertEquals("a&lt;table border=&quot;0&quot; color=&apos;red&apos;&gt;b&amp;lt;c&lt;/table&gt;d", StringUtils.escapeHtml("a<table border=\"0\" color=\'red\'>b&lt;c</table>d"));
	}

	@Test
	public void testUnescapeHtml() {
		Assert.assertEquals("a<table border=\"0\" color=\'red\'>b&lt;c</table>d", StringUtils.unescapeHtml("a&lt;table border=&quot;0&quot; color=&apos;red&apos;&gt;b&amp;lt;c&lt;/table&gt;d"));
	}

	@Test
	public void testTrimBlankLine() {
		Assert.assertEquals("12345678", StringUtils.trimBlankLine("12345678"));
		Assert.assertEquals("", StringUtils.escapeString(StringUtils.trimBlankLine("\t\r\b\f ")));
		Assert.assertEquals("", StringUtils.escapeString(StringUtils.trimBlankLine("\n\t\r\b\f ")));
		Assert.assertEquals("", StringUtils.escapeString(StringUtils.trimBlankLine("\t\r\b\f \n")));
		Assert.assertEquals(StringUtils.escapeString("\t\n"), StringUtils.escapeString(StringUtils.trimBlankLine("\t\r\b\f \n\t\n")));
		Assert.assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n\t\r\b\f\n"), StringUtils.escapeString(StringUtils.trimBlankLine("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n\t\r\b\f\n")));
		Assert.assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n"), StringUtils.escapeString(StringUtils.trimBlankLine(" \t\r\b\f\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f ")));
		Assert.assertEquals(StringUtils.escapeString("\t\r\b\f\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f\n"), StringUtils.escapeString(StringUtils.trimBlankLine(" \t\n\t\r\b\f\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f\n\t ")));
	}

	@Test
	public void testClearBlankLine() {
		Assert.assertEquals("12345678", StringUtils.clearBlankLine("12345678"));
		Assert.assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n"), StringUtils.escapeString(StringUtils.clearBlankLine("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n\t\r\b\f\n")));
		Assert.assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n"), StringUtils.escapeString(StringUtils.clearBlankLine("\n\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f\n\t\n\n")));
		Assert.assertEquals(StringUtils.escapeString("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8\n"), StringUtils.escapeString(StringUtils.clearBlankLine(" \t\n\n1\t2\n3\r4\b5\f6 \t7 \t\n\t\n\r\b\f8\n\t\r\b\f\n\t\n\n \t")));
	}

	@Test
	public void testClearBlank() {
		Assert.assertEquals("12345678", StringUtils.clearBlank("12345678"));
		Assert.assertEquals("12345678", StringUtils.clearBlank("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8"));
		Assert.assertEquals("12345678", StringUtils.clearBlank(" 1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8 "));
	}

	@Test
	public void testCompressBlank() {
		Assert.assertEquals("12345678", StringUtils.clearBlank("12345678"));
		Assert.assertEquals("1 2 3 4 5 6 7 8", StringUtils.compressBlank("1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8"));
		Assert.assertEquals(" 1 2 3 4 5 6 7 8 ", StringUtils.compressBlank(" 1\t2\n3\r4\b5\f6 \t7 \t\n\r\b\f8 "));
	}

}
