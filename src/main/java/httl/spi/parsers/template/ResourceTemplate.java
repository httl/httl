package httl.spi.parsers.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import httl.Engine;
import httl.Resource;
import httl.Template;

public class ResourceTemplate implements Template {

	private final Resource resource;

	public ResourceTemplate(Resource resource) {
		this.resource = resource;
	}

	public String getName() {
		return resource.getName();
	}

	public String getEncoding() {
		return resource.getEncoding();
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

	@SuppressWarnings("unchecked")
	public Map<String, Class<?>> getParameterTypes() {
		return Collections.EMPTY_MAP;
	}

	public Class<?> getReturnType() {
		return String.class;
	}

	public String getCode() {
		return "";
	}

	public int getOffset() {
		return 0;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Class<?>> getContextTypes() {
		return Collections.EMPTY_MAP;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Template> getMacros() {
		return Collections.EMPTY_MAP;
	}

	public boolean isMacro() {
		return false;
	}

	public Object evaluate(Map<String, Object> parameters) {
		throw new UnsupportedOperationException();
	}

	public void render(Map<String, Object> parameters, OutputStream output)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	public void render(Map<String, Object> parameters, Writer writer)
			throws IOException {
		throw new UnsupportedOperationException();
	}

}
