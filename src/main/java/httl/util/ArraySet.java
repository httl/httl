/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * ArraySet
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ArraySet<T> extends ArrayList<T> implements Set<T> {

	private static final long serialVersionUID = 4762663840185149857L;

	public ArraySet(){
		super();
	}

	public ArraySet(Collection<? extends T> c){
		super(c);
	}

	public ArraySet(int initialCapacity){
		super(initialCapacity);
	}

}
