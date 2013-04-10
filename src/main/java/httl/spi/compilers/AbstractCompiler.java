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
package httl.spi.compilers;

import httl.spi.Compiler;
import httl.spi.Logger;
import httl.internal.util.ClassUtils;
import httl.internal.util.StringUtils;
import httl.internal.util.VolatileReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract compiler. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setCompiler(Compiler)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractCompiler implements Compiler {
	
	private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([_a-zA-Z][_a-zA-Z0-9\\.]*);");
	
	private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([_a-zA-Z][_a-zA-Z0-9]*)\\s+");

	private static final ConcurrentMap<String, VolatileReference<Class<?>>> CLASS_CACHE = new ConcurrentHashMap<String, VolatileReference<Class<?>>>();

	private File codeDirectory;

	private File compileDirectory;
	
	protected Logger logger;
	
	private volatile boolean first = true;

	/**
	 * httl.properties: loggers=httl.spi.loggers.Log4jLogger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * httl.properties: code.directory=/tmp/javacode
	 */
	public void setCodeDirectory(String codeDirectory) {
		if (codeDirectory != null && codeDirectory.trim().length() > 0) {
			File file = new File(codeDirectory);
			if (file.exists() || file.mkdirs()) {
				this.codeDirectory = file;
			}
		}
	}

	/**
	 * httl.properties: compile.directory=classes
	 */
	public void setCompileDirectory(String directory) {
		if (directory != null && directory.trim().length() > 0) {
			File file = new File(directory);
			if (file.exists() || file.mkdirs()) {
				this.compileDirectory = file;
			}
		}
	}
	
	protected void saveBytecode(String name, byte[] bytecode) throws IOException {
		if (compileDirectory != null) {
			File file = new File(compileDirectory, name.replace('.', '/') + ".class");
			FileOutputStream out = new FileOutputStream(file);
			try {
				out.write(bytecode);
				out.flush();
			} finally {
				out.close();
			}
			if (first) {
				first = false;
				if (logger != null && logger.isInfoEnabled()) {
					logger.info("Compile httl template classes to directory " + compileDirectory.getAbsolutePath());
				}
			}
		}
	}

	private void logJavaCode(Class<?> clazz, String sorceCode) throws IOException {
		if (logger != null && logger.isDebugEnabled()) {
			logger.debug("\n================================\n" + sorceCode + "\n================================\n");
		}
		if (codeDirectory != null) {
			File javaFile = new File(codeDirectory, clazz.getPackage().getName().replace('.', '/') + "/" + clazz.getSimpleName() + ".java");
			File javaDir = javaFile.getParentFile();
			if (javaDir.exists() || javaDir.mkdirs()) {
				FileWriter writer = new FileWriter(javaFile);
				try {
					writer.write(sorceCode);
					writer.flush();
				} finally {
					writer.close();
				}
			}
		}
	}

	public Class<?> compile(String code) throws ParseException {
		String className = null;
		try {
			code = code.trim();
			if (! code.endsWith("}")) {
				throw new ParseException("The java code not endsWith \"}\"", code.length() - 1);
			}
			Matcher matcher = PACKAGE_PATTERN.matcher(code);
			String pkg;
			if (matcher.find()) {
				pkg = matcher.group(1);
			} else {
				pkg = "";
			}
			matcher = CLASS_PATTERN.matcher(code);
			String classSimpleName;
			if (matcher.find()) {
				classSimpleName = matcher.group(1);
			} else {
				throw new ParseException("No such class name in java code.", 0);
			}
			className = StringUtils.isNotEmpty(pkg) ? pkg + "." + classSimpleName : classSimpleName;
			VolatileReference<Class<?>> ref = CLASS_CACHE.get(className);
			if (ref == null) {
				ref = new VolatileReference<Class<?>>();
				VolatileReference<Class<?>> old = CLASS_CACHE.putIfAbsent(className, ref);
				if (old != null) {
					ref = old;
				}
			}
			Class<?> cls = ref.get();
			if (cls == null) {
				synchronized(ref) {
					cls = ref.get();
					if (cls == null) {
						cls = doCompile(className, code);
						ref.set(cls);
					}
				}
			}
			logJavaCode(cls, code);
			return cls;
		} catch (Throwable t) {
			if (logger != null && logger.isErrorEnabled()) {
				logger.error("Failed to compile class, cause: " + t.getMessage() + ", class: " + className + ", code: \n================================\n" + code + "\n================================\n", t);
			}
			if (t instanceof ParseException) {
				throw (ParseException) t;
			}
			throw new ParseException("Failed to compile class, cause: " + t.getMessage() + ", class: " + className + ", stack: " + ClassUtils.toString(t), 0);
		}
	}
	
	protected abstract Class<?> doCompile(String name, String source) throws Exception;

}