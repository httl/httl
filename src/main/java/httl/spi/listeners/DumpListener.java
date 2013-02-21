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
package httl.spi.listeners;

import httl.Context;
import httl.internal.util.Reqiured;
import httl.spi.Codec;
import httl.spi.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.tools.ant.util.DateUtils;

/**
 * DumpListener. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.interceptors.ListenerInterceptor#setBeforeListener(Listener)
 * @see httl.spi.interceptors.ListenerInterceptor#setAfterListener(Listener)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class DumpListener implements Listener {

	private File dumpDirectory;

	private Codec dumpCodec;

	@Reqiured
	public void setDumpDirectory(String dumpDirectory) {
		this.dumpDirectory = new File(dumpDirectory);
	}

	@Reqiured
	public void setDumpCodec(Codec dumpCodec) {
		this.dumpCodec = dumpCodec;
	}

	public void render(Context context) throws IOException, ParseException {
		if (dumpDirectory == null || dumpCodec == null)
			return;
		String prefix = context.getTemplate().getName() + "/" + DateUtils.format(new Date(), "yyyyMMddHHmmssSSS");
		String suffix = "." + dumpCodec.getFormat();
		File file = new File(dumpDirectory, prefix + suffix);
		int i = 2;
		while (file.exists()) {
			file = new File(dumpDirectory, prefix + "-" + (i ++) + suffix);
		}
		FileWriter out = new FileWriter(file);
		try {
			out.write(dumpCodec.toString("context", context));
			out.flush();
		} finally {
			out.close();
		}
	}

}