package httl.test.model;

import java.util.List;
import java.util.Map;

public class Model {
	
	private String _extends;
	
	private String chinese;
	
	private String impvar;
	
	private String html;
	
	private User user;
	
	private Book[] books;
	
	private List<Book> booklist;
	
	private Map<String, Book> bookmap;
	
	private Map<String, Map<String, Object>> mapbookmap;
	
	private List<Map<String, Object>> mapbooklist;
	
	private Book[] emptybooks;
	
	private Book[] books2;
	
	private List<Book> booklist2;
	
	private Map<String, Book> bookmap2;

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

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

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

}
