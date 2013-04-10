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
package httl.internal.util;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * StringUtils. (Tool, Static, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class StringUtils {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+(\\.[.0-9]+)?[BSILFDbsilfd]?$");

	private static final Pattern SYMBOL_PATTERN = Pattern.compile("[^(_a-zA-Z0-9)]");

	public static String getVaildName(String name) {
		return SYMBOL_PATTERN.matcher(name).replaceAll("_");
	}

	public static boolean isNumber(String value) {
		return isEmpty(value) ? false : NUMBER_PATTERN.matcher(value).matches();
	}

	public static boolean isNumber(char[] value) {
		if (value == null || value.length == 0) {
			return false;
		}
		for (char ch : value) {
			if (ch != '.' && (ch <= '0' || ch >= '9')) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNumber(byte[] value) {
		if (value == null || value.length == 0) {
			return false;
		}
		for (byte ch : value) {
			if (ch != '.' && (ch <= '0' || ch >= '9')) {
				return false;
			}
		}
		return true;
	}

	private static final Pattern NAMED_PATTERN = Pattern.compile("^[_A-Za-z][_0-9A-Za-z]*$");
	
	public static boolean isNamed(String value) {
		return NAMED_PATTERN.matcher(value).matches();
	}

	private static final Pattern TYPED_PATTERN = Pattern.compile("^[_A-Za-z][_.0-9A-Za-z]*$");
	
	public static boolean isTyped(String value) {
		return TYPED_PATTERN.matcher(value).matches();
	}

	private static final Pattern FUNCTION_PATTERN = Pattern.compile("^\\.[_A-Za-z][_0-9A-Za-z]*$");
	
	public static boolean isFunction(String value) {
		return FUNCTION_PATTERN.matcher(value).matches();
	}

	public static boolean isEmpty(byte[] value) {
		return value == null || value.length == 0;
	}

	public static boolean isNotEmpty(byte[] value) {
		return ! isEmpty(value);
	}

	public static boolean isEmpty(char[] value) {
		return value == null || value.length == 0;
	}

	public static boolean isNotEmpty(char[] value) {
		return ! isEmpty(value);
	}

	public static boolean isEmpty(String value) {
		return value == null || value.length() == 0;
	}

	public static boolean isNotEmpty(String value) {
		return ! isEmpty(value);
	}

	public static boolean isBlank(String value) {
		if (StringUtils.isNotEmpty(value)) {
			int len = value.length();
			for (int i = 0; i < len; i ++) {
				char ch = value.charAt(i);
				switch (ch) {
					case ' ': case '\t': case '\n': case '\r': case '\b': case '\f':
						break;
					default:
						return false;
				}
			}
		}
		return true;
	}
	
	public static boolean isNotBlank(String value) {
		return ! isBlank(value);
	}

	public static String toString(Object value) {
		if (value == null)
			return null;
		if (value.getClass().isArray()) {
			if (value instanceof boolean[]) {
				return Arrays.toString((boolean[]) value);
			} else if (value instanceof byte[]) {
				return Arrays.toString((byte[]) value);
			} else if (value instanceof short[]) {
				return Arrays.toString((short[]) value);
			} else if (value instanceof int[]) {
				return Arrays.toString((int[]) value);
			} else if (value instanceof long[]) {
				return Arrays.toString((long[]) value);
			} else if (value instanceof float[]) {
				return Arrays.toString((float[]) value);
			} else if (value instanceof double[]) {
				return Arrays.toString((double[]) value);
			} else if (value instanceof char[]) {
				return String.valueOf((char[]) value);
			} else if (value instanceof Object[]) {
				return Arrays.toString((Object[]) value);
			}
		}
		return String.valueOf(value);
	}
	
	public static String toByteString(byte[] bytes) {
		StringBuilder buf = new StringBuilder(bytes.length * 5);
		for (byte b : bytes) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append((int)b);
		}
		return buf.toString();
	}

	public static String toCharString(char[] chars) {
		StringBuilder buf = new StringBuilder(chars.length * 4);
		for (char c : chars) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append('\'');
			switch (c) {
				case '\\':
					buf.append("\\\\");
					break;
				case '\'':
					buf.append("\\'");
					break;
				case '\t':
					buf.append("\\t");
					break;
				case '\n':
					buf.append("\\n");
					break;
				case '\r':
					buf.append("\\r");
					break;
				case '\f':
					buf.append("\\f");
					break;
				case '\b':
					buf.append("\b");
					break;
				default:
					buf.append(c);
					break;
			}
			buf.append('\'');
		}
		return buf.toString();
	}

	public static String escapeString(String src) {
		if (StringUtils.isEmpty(src)) {
			return src;
		}
		int len = src.length();
		StringBuilder buf = null;
		for (int i = 0; i < len; i ++) {
			char ch = src.charAt(i);
			String rep;
			switch (ch) {
				case '\\':
					rep = "\\\\";
					break;
				case '\"':
					rep = "\\\"";
					break;
				case '\'':
					rep = "\\\'";
					break;
				case '\t':
					rep = "\\t";
					break;
				case '\n':
					rep = "\\n";
					break;
				case '\r':
					rep = "\\r";
					break;
				case '\b':
					rep = "\\b";
					break;
				case '\f':
					rep = "\\f";
					break;
				default:
					rep = null;
					break;
			}
			if (rep != null) {
				if (buf == null) {
					buf = new StringBuilder(len * 2);
					if(i > 0) {
						buf.append(src.substring(0, i));
					}
				}
				buf.append(rep);
			} else {
				if (buf != null) {
					buf.append(ch);
				}
			}
		}
		if (buf != null) {
			return buf.toString();
		}
		return src;
	}

	public static char[] escapeString(char[] src) {
		if (src == null || src.length == 0) {
			return src;
		}
		int len = src.length;
		int off = 0;
		char[] buf = null;
		for (int i = 0; i < len; i ++) {
			char ch = src[i];
			char rep;
			switch (ch) {
				case '\\':
					rep = '\\';
					break;
				case '\"':
					rep = '\"';
					break;
				case '\'':
					rep = '\'';
					break;
				case '\t':
					rep = 't';
					break;
				case '\n':
					rep = 'n';
					break;
				case '\r':
					rep = 'r';
					break;
				case '\b':
					rep = 'b';
					break;
				case '\f':
					rep = 'f';
					break;
				default:
					rep = 0;
					break;
			}
			if (rep != 0) {
				if (buf == null) {
					buf = expand(src, off = i, 2);
				}
				buf[off ++] = '\\';
				buf[off ++] = rep;
			} else {
				if (buf != null) {
					buf[off ++] = ch;
				}
			}
		}
		if (buf != null) {
			if (buf.length > off) {
				char[] newBuf = new char[off];
				System.arraycopy(buf, 0, newBuf, 0, off);
				return newBuf;
			}
			return buf;
		}
		return src;
	}

	public static byte[] escapeString(byte[] src) {
		if (src == null || src.length == 0) {
			return src;
		}
		int len = src.length;
		int off = 0;
		byte[] buf = null;
		byte pre = 0;
		for (int i = 0; i < len; i ++) {
			byte ch = src[i];
			byte rep;
			switch (ch) {
				case 92:
					rep = '\\';
					break;
				case 34:
					rep = '\"';
					break;
				case 39:
					rep = '\'';
					break;
				case 9:
					rep = 't';
					break;
				case 10:
					rep = 'n';
					break;
				case 13:
					rep = 'r';
					break;
				case 8:
					rep = 'b';
					break;
				case 12:
					rep = 'f';
					break;
				default:
					rep = 0;
					break;
			}
			if (rep != 0 && pre >= 0) {
				if (buf == null) {
					buf = expand(src, off = i, 2);
				}
				buf[off ++] = '\\';
				buf[off ++] = rep;
			} else {
				if (buf != null) {
					buf[off ++] = ch;
				}
			}
			pre = ch;
		}
		if (buf != null) {
			if (buf.length > off) {
				byte[] newBuf = new byte[off];
				System.arraycopy(buf, 0, newBuf, 0, off);
				return newBuf;
			}
			return buf;
		}
		return src;
	}

	public static String unescapeString(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		StringBuilder buf = null;
		int len = value.length() - 1;
		for (int i = 0; i < len; i ++) {
			char ch = value.charAt(i);
			if (ch == '\\') {
				int j = i;
				i ++;
				ch = value.charAt(i);
				switch (ch) {
					case '\\':
						ch = '\\';
						break;
					case '\"':
						ch = '\"';
						break;
					case '\'':
						ch = '\'';
						break;
					case 't':
						ch = '\t';
						break;
					case 'n':
						ch = '\n';
						break;
					case 'r':
						ch = '\r';
						break;
					case 'b':
						ch = '\b';
						break;
					case 'f':
						ch = '\f';
						break;
					default:
						j --;
				}
				if (buf == null) {
					buf = new StringBuilder(len);
					if(j > 0) {
						buf.append(value.substring(0, j));
					}
				}
				buf.append(ch);
			} else if (buf != null) {
				buf.append(ch);
			}
		}
		if (buf != null) {
			buf.append(value.charAt(len));
			return buf.toString();
		}
		return value;
	}

	public static String escapeXml(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		int len = value.length();
		StringBuilder buf = null;
		for (int i = 0; i < len; i ++) {
			char ch = value.charAt(i);
			switch (ch) {
				case '<':
					if (buf == null) {
						buf = new StringBuilder(len * 2);
						if(i > 0) {
							buf.append(value.substring(0, i));
						}
					}
					buf.append("&lt;");
					break;
				case '>':
					if (buf == null) {
						buf = new StringBuilder(len * 2);
						if(i > 0) {
							buf.append(value.substring(0, i));
						}
					}
					buf.append("&gt;");
					break;
				case '\"':
					if (buf == null) {
						buf = new StringBuilder(len * 2);
						if(i > 0) {
							buf.append(value.substring(0, i));
						}
					}
					buf.append("&quot;");
					break;
				case '\'':
					if (buf == null) {
						buf = new StringBuilder(len * 2);
						if(i > 0) {
							buf.append(value.substring(0, i));
						}
					}
					buf.append("&apos;");
					break;
				case '&':
					if (buf == null) {
						buf = new StringBuilder(len * 2);
						if(i > 0) {
							buf.append(value.substring(0, i));
						}
					}
					buf.append("&amp;");
					break;
				default:
					if (buf != null) {
						buf.append(ch);
					}
					break;
			}
		}
		if (buf != null) {
			return buf.toString();
		}
		return value;
	}

	public static char[] escapeXml(char[] src) {
		if (src == null || src.length == 0) {
			return src;
		}
		int len = src.length;
		int off = 0;
		char[] buf = null;
		for (int i = 0; i < len; i ++) {
			char ch = src[i];
			switch (ch) {
				case '<':
					if (buf == null) {
						buf = expand(src, off = i, 4);
					} else if (buf.length < off + 4) {
						buf = expand(buf, off, 4);
					}
					buf[off ++] = '&';
					buf[off ++] = 'l';
					buf[off ++] = 't';
					buf[off ++] = ';';
					break;
				case '>':
					if (buf == null) {
						buf = expand(src, off = i, 4);
					} else if (buf.length < off + 4) {
						buf = expand(buf, off, 4);
					}
					buf[off ++] = '&';
					buf[off ++] = 'g';
					buf[off ++] = 't';
					buf[off ++] = ';';
					break;
				case '\"':
					if (buf == null) {
						buf = expand(src, off = i, 6);
					} else if (buf.length < off + 6) {
						buf = expand(buf, off, 6);
					}
					buf[off ++] = '&';
					buf[off ++] = 'q';
					buf[off ++] = 'u';
					buf[off ++] = 'o';
					buf[off ++] = 't';
					buf[off ++] = ';';
					break;
				case '\'':
					if (buf == null) {
						buf = expand(src, off = i, 6);
					} else if (buf.length < off + 6) {
						buf = expand(buf, off, 6);
					}
					buf[off ++] = '&';
					buf[off ++] = 'a';
					buf[off ++] = 'p';
					buf[off ++] = 'o';
					buf[off ++] = 's';
					buf[off ++] = ';';
					break;
				case '&':
					if (buf == null) {
						buf = expand(src, off = i, 5);
					} else if (buf.length < off + 5) {
						buf = expand(buf, off, 5);
					}
					buf[off ++] = '&';
					buf[off ++] = 'a';
					buf[off ++] = 'm';
					buf[off ++] = 'p';
					buf[off ++] = ';';
					break;
				default:
					if (buf != null) {
						if (buf.length < off + 1) {
							buf = expand(buf, off, 1);
						}
						buf[off ++] = ch;
					}
					break;
			}
		}
		if (buf != null) {
			if (buf.length > off) {
				char[] newBuf = new char[off];
				System.arraycopy(buf, 0, newBuf, 0, off);
				return newBuf;
			}
			return buf;
		}
		return src;
	}

	public static byte[] escapeXml(byte[] src) {
		if (src == null || src.length == 0) {
			return src;
		}
		int len = src.length;
		int off = 0;
		byte[] buf = null;
		byte pre = 0;
		for (int i = 0; i < len; i ++) {
			byte ch = src[i];
			switch (ch) {
				case 60:
					if (pre >= 0) {
						if (buf == null) {
							buf = expand(src, off = i, 4);
						} else if (buf.length < off + 4) {
							buf = expand(buf, off, 4);
						}
						buf[off ++] = '&';
						buf[off ++] = 'l';
						buf[off ++] = 't';
						buf[off ++] = ';';
						break;
					}
				case 62:
					if (pre >= 0) {
						if (buf == null) {
							buf = expand(src, off = i, 4);
						} else if (buf.length < off + 4) {
							buf = expand(buf, off, 4);
						}
						buf[off ++] = '&';
						buf[off ++] = 'g';
						buf[off ++] = 't';
						buf[off ++] = ';';
						break;
					}
				case 34:
					if (pre >= 0) {
						if (buf == null) {
							buf = expand(src, off = i, 6);
						} else if (buf.length < off + 6) {
							buf = expand(buf, off, 6);
						}
						buf[off ++] = '&';
						buf[off ++] = 'q';
						buf[off ++] = 'u';
						buf[off ++] = 'o';
						buf[off ++] = 't';
						buf[off ++] = ';';
						break;
					}
				case 39:
					if (pre >= 0) {
						if (buf == null) {
							buf = expand(src, off = i, 6);
						} else if (buf.length < off + 6) {
							buf = expand(buf, off, 6);
						}
						buf[off ++] = '&';
						buf[off ++] = 'a';
						buf[off ++] = 'p';
						buf[off ++] = 'o';
						buf[off ++] = 's';
						buf[off ++] = ';';
						break;
					}
				case 38:
					if (pre >= 0) {
						if (buf == null) {
							buf = expand(src, off = i, 5);
						} else if (buf.length < off + 5) {
							buf = expand(buf, off, 5);
						}
						buf[off ++] = '&';
						buf[off ++] = 'a';
						buf[off ++] = 'm';
						buf[off ++] = 'p';
						buf[off ++] = ';';
						break;
					}
				default:
					if (buf != null) {
						if (buf.length < off + 1) {
							buf = expand(buf, off, 1);
						}
						buf[off ++] = ch;
					}
					break;
			}
			pre = ch;
		}
		if (buf != null) {
			if (buf.length > off) {
				byte[] newBuf = new byte[off];
				System.arraycopy(buf, 0, newBuf, 0, off);
				return newBuf;
			}
			return buf;
		}
		return src;
	}

	private static char[] expand(char[] src, int off, int inc) {
		int len = Math.max(src.length * 2, off + inc);
		char[] dest = new char[len];
		if (off > 0) {
			System.arraycopy(src, 0, dest, 0, off);
		}
		return dest;
	}

	private static byte[] expand(byte[] src, int off, int inc) {
		int len = Math.max(src.length * 2, off + inc);
		byte[] dest = new byte[len];
		if (off > 0) {
			System.arraycopy(src, 0, dest, 0, off);
		}
		return dest;
	}

	public static String unescapeXml(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		StringBuilder buf = null;
		int len = value.length();
		int len3 = len - 3;
		int len4 = len - 4;
		int len5 = len - 5;
		for (int i = 0; i < len; i ++) {
			char ch = value.charAt(i);
			if (ch == '&' && i < len3) {
				int j = i;
				char ch1 = value.charAt(i + 1);
				switch (ch1) {
					case 'l':
						if (value.charAt(i + 2) == 't'
							&& value.charAt(i + 3) == ';') {
							i += 3;
							if (buf == null) {
								buf = new StringBuilder(len3);
								if(j > 0) {
									buf.append(value.substring(0, j));
								}
							}
							buf.append('<');
						} else if (buf != null) {
							buf.append('&');
						}
						break;
					case 'g':
						if (value.charAt(i + 2) == 't'
							&&value.charAt(i + 3) == ';') {
							i += 3;
							if (buf == null) {
								buf = new StringBuilder(len3);
								if(j > 0) {
									buf.append(value.substring(0, j));
								}
							}
							buf.append('>');
						} else if (buf != null) {
							buf.append('&');
						}
						break;
					case 'a':
						if (i < len4 && value.charAt(i + 2) == 'm'
								&& value.charAt(i + 3) == 'p'
								&& value.charAt(i + 4) == ';') {
							i += 4;
							if (buf == null) {
								buf = new StringBuilder(len4);
								if(j > 0) {
									buf.append(value.substring(0, j));
								}
							}
							buf.append('&');
						} else if (i < len5 && value.charAt(i + 2) == 'p'
								&& value.charAt(i + 3) == 'o'
								&& value.charAt(i + 4) == 's'
								&& value.charAt(i + 5) == ';') {
							i += 5;
							if (buf == null) {
								buf = new StringBuilder(len5);
								if(j > 0) {
									buf.append(value.substring(0, j));
								}
							}
							buf.append('\'');
						} else if (buf != null) {
							buf.append('&');
						}
						break;
					case 'q':
						if (i < len5 && value.charAt(i + 2) == 'u'
								&& value.charAt(i + 3) == 'o'
								&& value.charAt(i + 4) == 't'
								&& value.charAt(i + 5) == ';') {
							i += 5;
							if (buf == null) {
								buf = new StringBuilder(len5);
								if(j > 0) {
									buf.append(value.substring(0, j));
								}
							}
							buf.append('\"');
						} else if (buf != null) {
							buf.append('&');
						}
						break;
					default:
						if (buf != null) {
							buf.append('&');
						}
						break;
				}
			} else if (buf != null) {
				buf.append(ch);
			}
		}
		if (buf != null) {
			return buf.toString();
		}
		return value;
	}

	public static String clearBlank(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		int len = value.length();
		StringBuilder buf = null;
		boolean blank = false;
		for (int i = 0; i < len; i ++) {
			char ch = value.charAt(i);
			switch (ch) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
				case '\b':
				case '\f':
					if (! blank) {
						blank = true;
						if (buf == null) {
							buf = new StringBuilder(len);
							if (i > 0) {
								buf.append(value.substring(0, i));
							}
						}
					}
					break;
				default:
					if (blank) {
						blank = false;
					}
					if (buf != null) {
						buf.append(ch);
					}
					break;
			}
		}
		if (buf != null) {
			return buf.toString();
		}
		return value;
	}

	public static String compressBlank(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		int len = value.length();
		StringBuilder buf = null;
		boolean blank = false;
		for (int i = 0; i < len; i ++) {
			char ch = value.charAt(i);
			switch (ch) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
				case '\b':
				case '\f':
					if (! blank) {
						blank = true;
						if (buf == null) {
							buf = new StringBuilder(len);
							if (i > 0) {
								buf.append(value.substring(0, i));
							}
						}
						buf.append(' ');
					}
					break;
				default:
					if (blank) {
						blank = false;
					}
					if (buf != null) {
						buf.append(ch);
					}
					break;
			}
		}
		if (buf != null) {
			return buf.toString();
		}
		return value;
	}
	
	public static String trimBlankLine(String value, boolean left, boolean right) {
		if (StringUtils.isEmpty(value) || (! left && ! right)) {
			return value;
		}
		int len = value.length();
		int len1 = len - 1;
		int start = 0;
		if (left) {
			loop: for (int i = 0; i < len; i ++) {
				char ch = value.charAt(i);
				switch (ch) {
					case ' ':
					case '\t':
					case '\r':
					case '\b':
					case '\f':
						if (i < len1) {
							continue;
						}
					case '\n':
						start = i + 1;
					default:
						break loop;
				}
			}
		}
		int end = len;
		if (right) {
			loop: for (int i = len1; i > start; i --) {
				char ch = value.charAt(i);
				switch (ch) {
					case ' ':
					case '\t':
					case '\r':
					case '\b':
					case '\f':
						if (i == start + 1) {
							end = start;
							break loop;
						}
						continue;
					case '\n':
						end = i + 1;
					default:
						break loop;
				}
			}
		}
		if (start > 0 || end < len) {
			if (start == end) {
				return "";
			}
			return value.substring(start, end);
		}
		return value;
	}

	public static String trimBlankLine(String value) {
		return trimBlankLine(value, true, true);
	}

	public static String trimLeftBlankLine(String value) {
		return trimBlankLine(value, true, false);
	}

	public static String trimRightBlankLine(String value) {
		return trimBlankLine(value, false, true);
	}

	public static String clearBlankLine(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		int len = value.length();
		int len1 = len - 1;
		StringBuilder buf = null;
		int pre = 0;
		boolean blank = true;
		for (int i = 0; i < len; i ++) {
			char ch = value.charAt(i);
			if (buf != null) {
				buf.append(ch);
			}
			switch (ch) {
				case ' ':
				case '\t':
				case '\r':
				case '\b':
				case '\f':
					if (i < len1) {
						break;
					}
				case '\n':
					if (blank) {
						if (buf == null) {
							buf = new StringBuilder(len);
							if (pre > 0) {
								buf.append(value.substring(0, pre + 1));
							}
						} else {
							buf.setLength(buf.length() - i + pre);
						}
					} else {
						blank = true;
					}
					pre = i;
					break;
				default:
					if (blank) {
						blank = false;
					}
					break;
			}
		}
		if (buf != null) {
			return buf.toString();
		}
		return value;
	}

	public static String getConditionCode(Class<?> type, String code, String[] sizers) throws ParseException {
		if (type != boolean.class) {
			if (type == byte.class 
					|| type == short.class
					|| type == int.class
					|| type == long.class
					|| type == float.class
					|| type == double.class) {
				code = "(" + code + ") != 0";
			} else if (type == char.class) {
				code = "(" + code + ") != \'\\0\'";
			} else if (type == String.class) {
				code = "(" + code + ")  != null && (" + code + ").length() > 0";
			} else if (type == Boolean.class) {
				code = "(" + code + ")  != null && (" + code + ").booleanValue()";
			} else if (type.isArray()) {
				code = "(" + code + ") != null && (" + code + ").length > 0";
			} else if (Collection.class.isAssignableFrom(type)) {
				code = "(" + code + ") != null && (" + code + ").size() > 0";
			} else if (Map.class.isAssignableFrom(type)) {
				code = "(" + code + ") != null && (" + code + ").size() > 0";
			} else {
				String method = ClassUtils.getSizeMethod(type, sizers);
				if (StringUtils.isNotEmpty(method)) {
					code = "(" + code + ") != null && (" + code + ")." + method + " > 0";
				} else {
					code = "(" + code + ") != null";
				}
			}
		}
		return code;
	}
	
	public static String getLocationMessage(String name, Reader reader, int offset) {
		String location = "";
		if (offset <= 0) {
			return location;
		}
		try {
			int line = 1;
			int column = 0;
			int count = 0;
			int len = 0;
			char[] buf = new char[128];
			StringBuilder cur = new StringBuilder();
			while ((len = reader.read(buf)) > 0) {
				for (int i = 0; i < len; i ++) {
					char ch = buf[i];
					if (ch == '\n') {
						line ++;
						column = 0;
						cur.setLength(0);
					} else {
						column ++;
						cur.append(ch);
					}
					if (count >= offset) {
						int padding = 20;
						String before;
						if (cur.length() <= padding) {
							before = cur.toString();
						} else {
							before = cur.substring(cur.length() - padding);
						}
						int c = i + 1;
						int remain = len - c;
						StringBuilder after = new StringBuilder();
						boolean breaked = false;
						if (remain > 0) {
							for (int j = c; j < padding + c && j < buf.length; j ++) {
								if (buf[j]== '\r' || buf[j] == '\n') {
									breaked = true;
									break;
								}
								after.append(buf[j]);
							}
						}
						if (! breaked && remain < padding) {
							char[] b = new char[padding - remain];
							int l = reader.read(b);
							if (l > 0) {
								for (int j = 0; j < l; j ++) {
									if (b[j] == '\r' || b[j] == '\n') {
										break;
									}
									after.append(b[j]);
								}
							}
						}
						StringBuilder msg = new StringBuilder();
						msg.append("line: " + line + ", column: " + column + ", char: " + ch + ", in: \n" + name + "\n");
						for (int j = 0; j < padding * 2; j ++) {
							msg.append("=");
						}
						msg.append("\n");
						msg.append("...");
						msg.append(before);
						msg.append(after);
						msg.append("...");
						msg.append("\n");
						for (int j = 0; j < before.length() + 2; j ++) {
							msg.append(" ");
						}
						msg.append("^-here\n");
						for (int j = 0; j < padding * 2; j ++) {
							msg.append("=");
						}
						msg.append("\n");
						return msg.toString();
					}
					count ++;
				}
			}
		} catch (Throwable t) {
		}
		return location;
	}

	public static String removeCommaValue(String values, String value) {
		return StringUtils.joinByComma(CollectionUtils.remove(StringUtils.splitByComma(values), value));
	}

	public static String joinByComma(String[] values) {
		return joinBy(values, ",");
	}

	public static String joinBy(String[] values, String sep) {
		StringBuilder buf = new StringBuilder();
		for (String value : values) {
			if (buf.length() > 0) {
				buf.append(sep);
			}
			buf.append(value);
		}
		return buf.toString();
	}

	private static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*\\,\\s*");

	public static String[] splitByComma(String name) {
		return name == null ? new String[0] : COMMA_SPLIT_PATTERN.split(name);
	}
	
	public static String splitCamelName(String name, String split) {
		return splitCamelName(name, split, false);
	}
	
	public static String splitCamelName(String name, String split, boolean upper) {
		if (StringUtils.isEmpty(name)) {
			return name;
		}
		StringBuilder buf = new StringBuilder(name.length() * 2);
		buf.append(upper ? Character.toUpperCase(name.charAt(0)) : Character.toLowerCase(name.charAt(0)));
		for (int i = 1; i < name.length(); i ++) {
			char c = name.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				buf.append(split);
				buf.append(upper ? c : Character.toLowerCase(c));
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	public static byte[] toBytes(String src, String encoding) {
		try {
			return src.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			return src.getBytes();
		}
	}

	public static boolean endsWith(String value, String[] suffixes) {
		if (value != null && suffixes != null) {
			for (String suffix : suffixes) {
				if (value.endsWith(suffix)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean inArray(String value, String[] values) {
		if (value != null && values != null) {
			for (String v : values) {
				if (value.equals(v)) {
					return true;
				}
			}
		}
		return false;
	}

}