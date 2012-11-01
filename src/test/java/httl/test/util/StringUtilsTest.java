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
		Assert.assertEquals("a&lt;table border=\"0\"&gt;b&amp;lt;c&lt;/table&gt;d", StringUtils.escapeHtml("a<table border=\"0\">b&lt;c</table>d"));
	}

	@Test
	public void testUnescapeHtml() {
		Assert.assertEquals("a<table border=\"0\">b&lt;c</table>d", StringUtils.unescapeHtml("a&lt;table border=\"0\"&gt;b&amp;lt;c&lt;/table&gt;d"));
	}

}
