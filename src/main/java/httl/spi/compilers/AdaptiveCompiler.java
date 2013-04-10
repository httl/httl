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

import httl.internal.util.ClassUtils;
import httl.spi.Compiler;
import httl.spi.Logger;

import java.text.ParseException;

/**
 * AdaptiveCompiler. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setCompiler(Compiler)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class AdaptiveCompiler implements Compiler {

	private Compiler compiler;

	private Logger logger;

	private String codeDirectory;

	/**
	 * httl.properties: loggers=httl.spi.loggers.Log4jLogger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
		if (compiler instanceof AbstractCompiler) {
			((AbstractCompiler) compiler).setLogger(logger);
		}
	}

	/**
	 * httl.properties: code.directory=/tmp
	 */
	public void setCodeDirectory(String codeDirectory) {
		this.codeDirectory = codeDirectory;
		if (compiler instanceof AbstractCompiler) {
			((AbstractCompiler) compiler).setCodeDirectory(codeDirectory);
		}
	}

	/**
	 * httl.properties: lint.unchecked=true
	 */
	public void setLintUnchecked(boolean unchecked) {
		if (compiler instanceof JdkCompiler) {
			((JdkCompiler) compiler).setLintUnchecked(unchecked);
		}
	}

	/**
	 * httl.properties: compile.version=1.7
	 */
	public void setCompileVersion(String version) {
		if (version == null || ClassUtils.isBeforeJava6(version)) {
			JavassistCompiler javassistCompiler = new JavassistCompiler();
			javassistCompiler.setLogger(logger);
			javassistCompiler.setCodeDirectory(codeDirectory);
			compiler = javassistCompiler;
		} else {
			JdkCompiler jdkCompiler = new JdkCompiler();
			jdkCompiler.setCompileVersion(version);
			jdkCompiler.setLogger(logger);
			jdkCompiler.setCodeDirectory(codeDirectory);
			compiler = jdkCompiler;
		}
	}

	public void init() {
		if (compiler == null) {
			setCompileVersion(ClassUtils.getJavaVersion());
		}
		if (compiler instanceof JdkCompiler) {
			((JdkCompiler) compiler).init();
		}
	}

	public Class<?> compile(String code) throws ParseException {
		return compiler.compile(code);
	}

}