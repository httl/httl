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
package httl.test.model;

import java.util.List;
import java.util.Map;

public class Model {
	
	private String _extends;
	
	private String chinese;
	
	private String impvar;
	
	private String defvar;
	
	private String html;
	
	public User user; // public field test
	
	private Book[] books;
	
	private List<Book> booklist;
	
	private Map<String, Book> bookmap;
	
	private Map<String, Map<String, Object>> mapbookmap;
	
	private List<Map<String, Object>> mapbooklist;
	
	private Book[] emptybooks;
	
	private Book[] books2;
	
	private List<Book> booklist2;
	
	private Map<String, Book> bookmap2;

	private Map<Integer, Integer> intmap;

	private int begin;

	private int end;
	
	private boolean logined;

	public boolean isLogined() {
		return logined;
	}

	public void setLogined(boolean logined) {
		this.logined = logined;
	}

	public String getExtends() {
		return _extends;
	}

	public void setExtends(String _extends) {
		this._extends = _extends;
	}

	public String getChinese() {
		return chinese;
	}

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public String getImpvar() {
		return impvar;
	}

	public void setImpvar(String impvar) {
		this.impvar = impvar;
	}

	public String getDefvar() {
		return defvar;
	}

	public void setDefvar(String defvar) {
		this.defvar = defvar;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	// public field test
	/*public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}*/

	public Book[] getBooks() {
		return books;
	}

	public void setBooks(Book[] books) {
		this.books = books;
	}

	public List<Book> getBooklist() {
		return booklist;
	}

	public void setBooklist(List<Book> booklist) {
		this.booklist = booklist;
	}

	public Map<String, Book> getBookmap() {
		return bookmap;
	}

	public void setBookmap(Map<String, Book> bookmap) {
		this.bookmap = bookmap;
	}

	public Map<String, Map<String, Object>> getMapbookmap() {
		return mapbookmap;
	}

	public void setMapbookmap(Map<String, Map<String, Object>> mapbookmap) {
		this.mapbookmap = mapbookmap;
	}

	public List<Map<String, Object>> getMapbooklist() {
		return mapbooklist;
	}

	public void setMapbooklist(List<Map<String, Object>> mapbooklist) {
		this.mapbooklist = mapbooklist;
	}

	public Book[] getEmptybooks() {
		return emptybooks;
	}

	public void setEmptybooks(Book[] emptybooks) {
		this.emptybooks = emptybooks;
	}

	public Book[] getBooks2() {
		return books2;
	}

	public void setBooks2(Book[] books2) {
		this.books2 = books2;
	}

	public List<Book> getBooklist2() {
		return booklist2;
	}

	public void setBooklist2(List<Book> booklist2) {
		this.booklist2 = booklist2;
	}

	public Map<String, Book> getBookmap2() {
		return bookmap2;
	}

	public void setBookmap2(Map<String, Book> bookmap2) {
		this.bookmap2 = bookmap2;
	}

	public Map<Integer, Integer> getIntmap() {
		return intmap;
	}

	public void setIntmap(Map<Integer, Integer> intmap) {
		this.intmap = intmap;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}