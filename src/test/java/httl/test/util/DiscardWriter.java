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

import java.io.IOException;
import java.io.Writer;

/**
 * DiscardWriter. (Tool, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DiscardWriter extends Writer {

	@Override
	public Writer append(char c) throws IOException {
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end)
			throws IOException {
		return this;
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		return this;
	}

	@Override
	public void write(char[] cbuf) throws IOException {
	}

	@Override
	public void write(int c) throws IOException {
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
	}

	@Override
	public void write(String str) throws IOException {
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

}