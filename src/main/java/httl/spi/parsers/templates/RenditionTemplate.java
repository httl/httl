package httl.spi.parsers.templates;

import httl.Context;
import httl.Template;
import httl.spi.Rendition;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Map;

public class RenditionTemplate extends TemplateWrapper {

	private final Rendition rendition;

	public RenditionTemplate(Template template, Rendition rendition) {
		super(template);
		this.rendition = rendition;
	}

	@Override
	public void render(Map<String, Object> parameters, OutputStream output)
			throws IOException, ParseException {
		if (Context.getContext().getOut() != output) {
			Context.pushContext(this, parameters, output);
			try {
				rendition.render(Context.getContext());
			} finally {
				Context.popContext();
			}
		} else {
			rendition.render(Context.getContext());
		}
	}

	@Override
	public void render(Map<String, Object> parameters, Writer writer)
			throws IOException, ParseException {
		if (Context.getContext().getOut() != writer) {
			Context.pushContext(this, parameters, writer);
			try {
				rendition.render(Context.getContext());
			} finally {
				Context.popContext();
			}
		} else {
			rendition.render(Context.getContext());
		}
	}

}
