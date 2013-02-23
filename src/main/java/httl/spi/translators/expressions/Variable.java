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
package httl.spi.translators.expressions;

import httl.spi.Translator;
import httl.internal.util.ClassUtils;

import java.text.ParseException;
import java.util.Map;

/**
 * Variable. (SPI, Prototype, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Variable extends Node {

	private static final long serialVersionUID = 1L;
	
	private Class<?> defaultType;

	private final String name;
	
	public Variable(Translator translator, String name, int offset, Map<String, Class<?>> parameterTypes, Class<?> defaultType){
		super(parameterTypes, offset);
		this.name = name;
		this.defaultType = defaultType;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public Class<?> getReturnType() throws ParseException {
		Class<?> type = getVariableTypes().get(name);
		if (type == null && defaultType != null) {
			return defaultType;
		}
		return type;
	}

	public String getCode() throws ParseException {
		return ClassUtils.filterJavaKeyword(name);
	}

}
