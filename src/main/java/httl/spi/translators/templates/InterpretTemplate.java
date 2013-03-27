package httl.spi.translators.templates;

import httl.Context;
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.ast.Block;
import httl.ast.Macro;
import httl.ast.Statement;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.translators.visitors.InterpretVisitor;
import httl.spi.translators.visitors.VariableVisitor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterpretTemplate extends AbstractTemplate {

	private final Map<String, Class<?>> variables;

	private final Map<String, Template> macros;

	private Converter<Object, Object> mapConverter;

	private Converter<Object, Object> outConverter;
	
	private Formatter<Object> formatter;

	private Filter textFilter;

	private Filter valueFilter;

	private String[] forVariable = new String[] { "for" };

	private String ifVariable = "if";

	private String outputEncoding;

	public InterpretTemplate(Resource resource, Node root, Template parent) throws ParseException {
		super(resource, root, parent);
		VariableVisitor visitor = new VariableVisitor();
		root.accept(visitor);
		this.variables = Collections.unmodifiableMap(visitor.getVariables());
		Map<String, Template> macros = new HashMap<String, Template>();
		if (root instanceof Block) {
			List<Statement> nodes = ((Block) root).getChildren();
			for (Node node : nodes) {
				if (node instanceof Macro) {
					InterpretTemplate macro = new InterpretTemplate(resource, node, this);
					macro.setMapConverter(mapConverter);
					macro.setOutConverter(outConverter);
					macro.setFormatter(formatter);
					macro.setValueFilter(valueFilter);
					macro.setTextFilter(textFilter);
					macro.setForVariable(forVariable);
					macro.setIfVariable(ifVariable);
					macro.setOutputEncoding(outputEncoding);
					macros.put(((Macro) node).getName(), macro);
				}
			}
		}
		this.macros = Collections.unmodifiableMap(macros);
	}

	private Object convertOut(Object out) throws IOException, ParseException {
		if (outConverter != null && out != null
				&& ! (out instanceof OutputStream) 
				&& ! (out instanceof Writer)) {
			return outConverter.convert(out, getVariables());
		}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> convertMap(Object context) throws ParseException {
		if (mapConverter != null && context != null && ! (context instanceof Map)) {
			try {
				context = mapConverter.convert(context, getVariables());
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		if (context == null || context instanceof Map) {
			return (Map<String, Object>) context;
		} else {
			throw new IllegalArgumentException("No such Converter to convert the " + context.getClass().getName() + " to Map.");
		}
	}

	public void setMapConverter(Converter<Object, Object> mapConverter) {
		this.mapConverter = mapConverter;
	}

	public void setOutConverter(Converter<Object, Object> outConverter) {
		this.outConverter = outConverter;
	}

	public void setFormatter(Formatter<Object> formatter) {
		this.formatter = formatter;
	}

	public void setTextFilter(Filter textFilter) {
		this.textFilter = textFilter;
	}

	public void setValueFilter(Filter valueFilter) {
		this.valueFilter = valueFilter;
	}

	public void setForVariable(String[] forVariable) {
		this.forVariable = forVariable;
	}

	public void setIfVariable(String ifVariable) {
		this.ifVariable = ifVariable;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public Map<String, Class<?>> getVariables() {
		return variables;
	}

	public Map<String, Template> getMacros() {
		return macros;
	}

	public void render(Object context, Object out) throws IOException, ParseException {
		out = convertOut(out);
		if (out == null) {
			throw new IllegalArgumentException("out == null");
		} else if (out instanceof OutputStream) {
			render(convertMap(context), (OutputStream) out);
		} else if (out instanceof Writer) {
			render(convertMap(context), (Writer) out);
		} else {
			throw new IllegalArgumentException("No such Converter to convert the " + out.getClass().getName() + " to OutputStream or Writer.");
		}
	}
	
	public void render(Map<String, Object> map, Object out) throws IOException, ParseException {
		Context context = Context.pushContext(map).setTemplate(this);
		if (out instanceof OutputStream) {
			context.setOut((OutputStream) out);
		} else if (out instanceof Writer) {
			context.setOut((Writer) out);
		}
		try {
			InterpretVisitor visitor = new InterpretVisitor();
			visitor.setOut(out);
			visitor.setFormatter(formatter);
			visitor.setValueFilter(valueFilter);
			visitor.setTextFilter(textFilter);
			visitor.setForVariable(forVariable);
			visitor.setIfVariable(ifVariable);
			visitor.setOutputEncoding(outputEncoding);
			accept(visitor);
		} finally {
			Context.popContext();
		}
	}

}
