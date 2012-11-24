package httl.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import httl.Engine;
import httl.Template;
import httl.spi.resolvers.RequestResolver;
import httl.util.WrappedMap;

public class WebTemplate implements Template {

    private static final String OUTPUT_STREAM = "output.stream";

	private final Template template;

	public WebTemplate(Template template) {
		this.template = template;
	}

	public void render(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
		render(request, response, null);
	}

	public void render(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) throws IOException, ParseException {
		WebEngine.getWebEngine().setServletContext(request.getSession().getServletContext());
		Map<String, Object> parameters = new ParameterMap(request);
		if (model != null) {
			parameters = new WrappedMap<String, Object>(parameters, model);
		}
		WebContext.setWebContext(request, response);
		boolean unresolved = RequestResolver.getServletRequest() == null;
		if (unresolved) {
			RequestResolver.setServletRequest(request);
			RequestResolver.setServletResponse(response);
		}
		try {
			if (WebEngine.getWebEngine().getProperty(OUTPUT_STREAM, false)) {
				render(parameters, response.getOutputStream());
			} else {
				render(parameters, response.getWriter());
			}
			response.flushBuffer();
		} catch (FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} finally {
			WebContext.removeWebContext();
			if (unresolved) {
				RequestResolver.removeServletRequest();
				RequestResolver.removeServletResponse();
			}
		}
	}

	public void render(Map<String, Object> parameters, OutputStream output) throws IOException {
		template.render(parameters, output);
	}

	public void render(Map<String, Object> parameters, Writer writer) throws IOException {
		template.render(parameters, writer);
	}

	public String render(Map<String, Object> parameters) {
		return template.render(parameters);
	}

	public String getName() {
		return template.getName();
	}

	public String getEncoding() {
		return template.getEncoding();
	}

	public long getLastModified() {
		return template.getLastModified();
	}

	public long getLength() {
		return template.getLength();
	}

	public String getSource() throws IOException {
		return template.getSource();
	}

	public Reader getReader() throws IOException {
		return template.getReader();
	}

	public Map<String, Class<?>> getParameterTypes() {
		return template.getParameterTypes();
	}

	public InputStream getInputStream() throws IOException {
		return template.getInputStream();
	}

	public Map<String, Class<?>> getReturnTypes() {
		return template.getReturnTypes();
	}

	public Map<String, Template> getMacros() {
		return template.getMacros();
	}

	public String getCode() {
		return template.getCode();
	}

	public boolean isMacro() {
		return template.isMacro();
	}

	public Engine getEngine() {
		return template.getEngine();
	}

	public int getOffset() {
		return template.getOffset();
	}

}
