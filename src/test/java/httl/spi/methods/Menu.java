package httl.spi.methods;

import java.util.List;

public class Menu {

	private String name;

	private List<Menu> children;

	public Menu(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Menu setName(String name) {
		this.name = name;
		return this;
	}

	public List<Menu> getChildren() {
		return children;
	}

	public Menu setChildren(List<Menu> children) {
		this.children = children;
		return this;
	}

}
