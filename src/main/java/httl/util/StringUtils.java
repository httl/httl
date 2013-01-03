/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"){} you may not use this file except in compliance with
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
package httl.util;

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
    
    public static boolean isNumber(String value) {
        return NUMBER_PATTERN.matcher(value).matches();
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
    
    public static String toString(Object value) {
        if (value == null)
            return null;
        if (value.getClass().isArray()) {
            if (value instanceof boolean[]) {
                return Arrays.toString((boolean[]) value);
            } else if (value instanceof char[]) {
                return Arrays.toString((char[]) value);
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
            } else if (value instanceof Object[]) {
                return Arrays.toString((Object[]) value);
            }
        }
        return String.valueOf(value);
    }
    
    public static String toByteString(byte[] bytes) {
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append((int)b);
        }
        return buf.toString();
    }

    public static String escapeString(String value) {
    	if (value == null || value.length() == 0) {
            return value;
        }
    	int len = value.length();
        StringBuilder buf = null;
        for (int i = 0; i < len; i ++) {
            char ch = value.charAt(i);
            String str;
            switch (ch) {
                case '\\':
                    str = "\\\\";
                    break;
                case '\"':
                    str = "\\\"";
                    break;
                case '\'':
                    str = "\\\'";
                    break;
                case '\t':
                    str = "\\t";
                    break;
                case '\n':
                    str = "\\n";
                    break;
                case '\r':
                    str = "\\r";
                    break;
                case '\b':
                    str = "\\b";
                    break;
                case '\f':
                    str = "\\f";
                    break;
                default:
                    str = null;
                    break;
            }
            if (str != null) {
                if (buf == null) {
                    buf = new StringBuilder(len * 2);
                    if(i > 0) {
                        buf.append(value.substring(0, i));
                    }
                }
                buf.append(str);
            } else {
                if (buf != null) {
                    buf.append(ch);
                }
            }
        }
        if (buf != null) {
            return buf.toString();
        }
        return value;
    }
    
    public static String unescapeString(String value) {
    	if (value == null || value.length() == 0) {
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

    /**
     * HTML特殊符转义。
     * 
     * @param value 可能带HTML特殊符的串
     * @return 不带HTML特殊符的串
     */
    public static String escapeHtml(String value) {
    	return escapeXml(value);
    }
    
	public static String escapeXml(String value) {
	    if (value == null || value.length() == 0) {
	        return value;
	    }
	    int len = value.length();
	    StringBuilder buf = null;
	    for (int i = 0; i < len; i ++) {
	        char ch = value.charAt(i);
	        switch (ch) {
	            case '&':
	                if (buf == null) {
	                    buf = new StringBuilder(len * 2);
	                    if(i > 0) {
	                        buf.append(value.substring(0, i));
	                    }
	                }
	                buf.append("&amp;");
	                break;
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

    /**
     * HTML特殊符转义还原。
     * 
     * @param value 被转义HTML特殊符的串
     * @return 还原后带HTML特殊符的串
     */
    public static String unescapeHtml(String value) {
    	return unescapeXml(value);
    }
    
    public static String unescapeXml(String value) {
    	if (value == null || value.length() == 0) {
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
    	if (value == null || value.length() == 0) {
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
    	if (value == null || value.length() == 0) {
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

    public static String trimBlankLine(String value) {
    	if (value == null || value.length() == 0) {
            return value;
        }
    	int len = value.length();
    	int len1 = len - 1;
    	int start = 0;
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
    	int end = len;
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
    	if (start > 0 || end < len) {
    		if (start == end) {
    			return "";
    		}
    		return value.substring(start, end);
    	}
        return value;
    }

    public static String clearBlankLine(String value) {
    	if (value == null || value.length() == 0) {
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

    public static String getConditionCode(Class<?> type, String code) throws ParseException {
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
                String method = ClassUtils.getSizeMethod(type);
                if (method != null && method.length() > 0) {
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
    
    public static String splitCamelName(String name, String split) {
    	return splitCamelName(name, split, false);
    }
    
    public static String splitCamelName(String name, String split, boolean upper) {
    	if (name == null || name.length() == 0) {
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

}
