package httl.ast;

import httl.Engine;
import httl.Node;
import httl.Resource;
import httl.Template;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Root extends BlockDirective implements Template {

	private final Resource resource;

	public Root(Resource resource) {
		this.resource = resource;
	}

	public String getName() {
		return resource.getName();
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

	public String getSource() {
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

	public Object evaluate() throws ParseException {
		return null;
	}

	public Object evaluate(Object context) throws ParseException {
		return null;
	}

	public void render(Object out) throws IOException, ParseException {
		render((Map<String, Object>) null, out);
	}

	@SuppressWarnings("unchecked")
	public void render(Object context, Object out) throws IOException, ParseException {
		render((Map<String, Object>) context, out);
	}

	public List<Node> getNodes() {
		return (List) getChildren();
	}

	public Template getMacro(String name) {
		return getMacros().get(name);
	}
	
	private Map<String, Template> macros;

	public Map<String, Template> getMacros() {
		if (macros == null) {
			Map<String, Template> map = new HashMap<String, Template>();
			for (Node node : getNodes()) {
				if (node instanceof Macro) {
					Macro macro = (Macro) node;
					map.put(macro.getName(), macro);
				}
			}
			macros = map;
		}
		return macros;
	}

	public boolean isMacro() {
		return false;
	}

	public Class<?> getRootType() {
		return Void.class;
	}

	public Map<String, Class<?>> getVariableTypes() {
		return new HashMap<String, Class<?>>();
	}

	public Class<?> getReturnType() {
		return String.class;
	}

	public Map<String, Class<?>> getExportTypes() {
		return new HashMap<String, Class<?>>();
	}

	public String getCode() {
		return "";
	}

}
