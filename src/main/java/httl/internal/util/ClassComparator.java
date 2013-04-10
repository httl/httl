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

import java.util.Comparator;

/**
 * ClassComparator. (Tool, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ClassComparator implements Comparator<Class<?>> {

	public static final ClassComparator COMPARATOR = new ClassComparator();

	private ClassComparator() {
	}

	public int compare(Class<?> o1, Class<?> o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		// 空值排在后面
		if (o1 == null) {
			return 1;
		}
		if (o2 == null) {
			return -1;
		}
		// 父类型排在后面
		if (o1.isAssignableFrom(o2)) { // o1是o2的父类型
			return 1;
		}
		if (o2.isAssignableFrom(o1)) { // o2是o1的父类型
			return -1;
		}
		// 互不相关的类型按类名排序
		return o1.getName().compareTo(o2.getName());
	}
}