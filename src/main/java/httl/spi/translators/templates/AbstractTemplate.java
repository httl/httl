package httl.spi.translators.templates;

import httl.Engine;
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.Visitor;
import httl.ast.Block;
import httl.ast.Macro;
import httl.internal.util.UnsafeStringWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

public abstract class AbstractTemplate implements Template {

	private final Resource resource;

	private final Node root;

	private final Template parent;

	private final String name;

	public AbstractTemplate(Resource resource, Node root, Template parent) {
		this.resource = resource;
		this.root = root;
		this.parent = parent;
		this.name = buildName(resource, root);
	}

	private static String buildName(Resource resource, Node root) {
		StringBuilder builder = new StringBuilder();
		builder.append(resource.getName());
		while (root instanceof Macro) {
			builder.append("#");
			builder.append(((Macro) root).getName());
			root = root.getParent();
		}
		return builder.toString();
	}

	public String getName() {
		return name;
	}

	public String getEncoding() {
		return resource.getEncoding();
	}

	public Locale getLocale() {
		return resource.getLocale();
	}

	public long getLastModified() {
		return resource.getLastModified();
	}

	public long getLength() {
		return resource.getLength();
	}

	public String getSource() throws IOException {
		return resource.getSource();
	}

	public Reader getReader() throws IOException {
		return resource.getReader();
	}

	public InputStream getInputStream() throws IOException {
		return resource.getInputStream();
	}

	public Engine getEngine() {
		return resource.getEngine();
	}

	public int getOffset() {
		return root.getOffset();
	}

	public Template getParent() {
		return parent;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Node> getNodes() {
		return (List) ((Block) root).getChildren();
	}

	public boolean isMacro() {
		return root instanceof Macro;
	}

	public void accept(Visitor visitor) throws IOException, ParseException {
		if (visitor.visit(this)) {
			for (Node node : getNodes()) {
				node.accept(visitor);
			}
		}
	}

	public Object evaluate() throws ParseException {
		return evaluate(null);
	}

	public void render(Object out) throws IOException, ParseException {
		render(null, out);
	}

	public Object evaluate(Object context) throws ParseException {
		UnsafeStringWriter writer = new UnsafeStringWriter();
		try {
			render(context, writer);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return writer.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String name = getName();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AbstractTemplate other = (AbstractTemplate) obj;
		String name = getName();
		String otherName = other.getName();
		if (name == null) {
			if (otherName != null) return false;
		} else if (!name.equals(otherName)) return false;
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}

}
