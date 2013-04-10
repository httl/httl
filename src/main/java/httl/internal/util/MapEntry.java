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

import java.util.Map.Entry;

/**
 * MapEntry
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class MapEntry<K, V> implements Entry<K, V> {
	
	private K key;

	private V value;

	@SuppressWarnings("unchecked")
	public MapEntry(boolean key, V value){
		this.key = (K) Boolean.valueOf(key);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public MapEntry(char key, V value){
		this.key = (K) Character.valueOf(key);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public MapEntry(byte key, V value){
		this.key = (K) Byte.valueOf(key);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public MapEntry(short key, V value){
		this.key = (K) Short.valueOf(key);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public MapEntry(int key, V value){
		this.key = (K) Integer.valueOf(key);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public MapEntry(long key, V value){
		this.key = (K) Long.valueOf(key);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public MapEntry(float key, V value){
		this.key = (K) Float.valueOf(key);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public MapEntry(double key, V value){
		this.key = (K) Double.valueOf(key);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public MapEntry(K key, boolean value){
		this.key = key;
		this.value = (V) Boolean.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(K key, char value){
		this.key = key;
		this.value = (V) Character.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(K key, byte value){
		this.key = key;
		this.value = (V) Byte.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(K key, short value){
		this.key = key;
		this.value = (V) Short.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(K key, int value){
		this.key = key;
		this.value = (V) Integer.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(K key, long value){
		this.key = key;
		this.value = (V) Long.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(K key, float value){
		this.key = key;
		this.value = (V) Float.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(K key, double value){
		this.key = key;
		this.value = (V) Double.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(boolean key, boolean value){
		this.key = (K) Boolean.valueOf(key);
		this.value = (V) Boolean.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(char key, char value){
		this.key = (K) Character.valueOf(key);
		this.value = (V) Character.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(byte key, byte value){
		this.key = (K) Byte.valueOf(key);
		this.value = (V) Byte.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(short key, short value){
		this.key = (K) Short.valueOf(key);
		this.value = (V) Short.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(int key, int value){
		this.key = (K) Integer.valueOf(key);
		this.value = (V) Integer.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(long key, long value){
		this.key = (K) Long.valueOf(key);
		this.value = (V) Long.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(float key, float value){
		this.key = (K) Float.valueOf(key);
		this.value = (V) Float.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	public MapEntry(double key, double value){
		this.key = (K) Double.valueOf(key);
		this.value = (V) Double.valueOf(value);
	}

	public MapEntry(K key, V value){
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public V setValue(V value) {
		V old = this.value;
		this.value = value;
		return old;
	}

	@Override
	public String toString() {
		return key + ": " + value;
	}

}