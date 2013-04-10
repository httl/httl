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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * IOUtils. (Tool, Static, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class IOUtils {

	public static byte[] readToBytes(InputStream in) throws IOException {
		try {
			UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
			try {
				byte[] buf = new byte[8192];
				int len = 0;
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len);
				}
				return out.toByteArray();
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	public static char[] readToChars(Reader reader) throws IOException {
		try {
			StringBuilder buffer = new StringBuilder();
			char[] buf = new char[8192];
			int len = 0;
			while ((len = reader.read(buf)) != -1) {
				buffer.append(buf, 0, len);
			}
			char[] result = new char[buffer.length()];
			buffer.getChars(0, buffer.length(), result, 0);
			return result;
		} finally {
			reader.close();
		}
	}

	public static String readToString(Reader reader) throws IOException {
		try {
			StringBuilder buffer = new StringBuilder();
			char[] buf = new char[8192];
			int len = 0;
			while ((len = reader.read(buf)) != -1) {
				buffer.append(buf, 0, len);
			}
			return buffer.toString();
		} finally {
			reader.close();
		}
	}

	public static List<String> readLines(Reader reader) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		try {
			List<String> lines = new ArrayList<String>();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		} finally {
			bufferedReader.close();
		}
	}
	
	public static void copy(Reader in, Writer out) throws IOException {
		try {
			char[] buf = new char[8192];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
		} finally {
			in.close();
		}
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buf = new byte[8192];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
		} finally {
			in.close();
		}
	}
	
	private IOUtils() {}

}