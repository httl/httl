package httl.spi.translators.templates;

import httl.Context;
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.ast.Block;
import httl.ast.Macro;
import httl.ast.Statement;
import httl.internal.util.StringSequence;
import httl.spi.Converter;
import httl.spi.Filter;
import httl.spi.Formatter;
import httl.spi.Interceptor;
import httl.spi.Listener;
import httl.spi.Switcher;
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

	private Switcher<Filter> textFilterSwitcher;

	private Switcher<Filter> valueFilterSwitcher;

	private Switcher<Formatter<Object>> formatterSwitcher;

	private String filterVariable = "filter";

	private String formatterVariable = "formatter";

	private Interceptor interceptor;

	private String[] forVariable = new String[] { "for" };

	private String ifVariable = "if";

	private String outputEncoding;

	private List<StringSequence> importSequences;
	
	private Map<Class<?>, Object> importMethods;

	private Map<String, Template> importMacros;

	private String[] importPackages;

	public InterpretTemplate(Resource resource, Node root, Template parent) throws IOException, ParseException {
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
					macros.put(((Macro) node).getName(), macro);
				}
			}
		}
		this.macros = Collections.unmodifiableMap(macros);
	}
	
	public void init() {
		for (Template m : macros.values()) {
			InterpretTemplate macro = (InterpretTemplate) m;
			macro.setInterceptor(interceptor);
			macro.setMapConverter(mapConverter);
			macro.setOutConverter(outConverter);
			macro.setFormatter(formatter);
			macro.setValueFilter(valueFilter);
			macro.setTextFilter(textFilter);
			macro.setForVariable(forVariable);
			macro.setIfVariable(ifVariable);
			macro.setOutputEncoding(outputEncoding);
			macro.setImportSequences(importSequences);
			macro.setImportMethods(importMethods);
			macro.setImportMacros(importMacros);
			macro.setImportPackages(importPackages);
			macro.setTextFilterSwitcher(textFilterSwitcher);
			macro.setValueFilterSwitcher(valueFilterSwitcher);
			macro.setFormatterSwitcher(formatterSwitcher);
			macro.setFilterVariable(filterVariable);
			macro.setFormatterVariable(formatterVariable);
			macro.init();
		}
	}

	private void doRender(Map<String, Object> map, final Object out) throws IOException, ParseException {
		Context context = Context.pushContext(map).setTemplate(this);
		if (out instanceof OutputStream) {
			context.setOut((OutputStream) out);
		} else if (out instanceof Writer) {
			context.setOut((Writer) out);
		}
		try {
			if (interceptor == null) {
				_doRender(out);
			} else {
				interceptor.render(context, new Listener() {
					public void render(Context context) throws IOException, ParseException {
						_doRender(out);
					}
				});
			}
		} finally {
			Context.popContext();
		}
	}
	
	private void _doRender(Object out) throws IOException, ParseException {
		InterpretVisitor visitor = new InterpretVisitor();
		visitor.setTemplate(this);
		visitor.setOut(out);
		visitor.setFormatter(formatter);
		visitor.setValueFilter(valueFilter);
		visitor.setTextFilter(textFilter);
		visitor.setForVariable(forVariable);
		visitor.setIfVariable(ifVariable);
		visitor.setOutputEncoding(outputEncoding);
		visitor.setImportSequences(importSequences);
		visitor.setImportMethods(importMethods);
		visitor.setImportMacros(importMacros);
		visitor.setImportPackages(importPackages);
		visitor.setTextFilterSwitcher(textFilterSwitcher);
		visitor.setValueFilterSwitcher(valueFilterSwitcher);
		visitor.setFormatterSwitcher(formatterSwitcher);
		visitor.setFilterVariable(filterVariable);
		visitor.setFormatterVariable(formatterVariable);
		accept(visitor);
	}

	public void render(Object context, Object out) throws IOException, ParseException {
		out = convertOut(out);
		if (out == null) {
			throw new IllegalArgumentException("out == null");
		} else if (out instanceof OutputStream) {
			doRender(convertMap(context), (OutputStream) out);
		} else if (out instanceof Writer) {
			doRender(convertMap(context), (Writer) out);
		} else {
			throw new IllegalArgumentException("No such Converter to convert the " + out.getClass().getName() + " to OutputStream or Writer.");
		}
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

	public void setTextFilterSwitcher(Switcher<Filter> textFilterSwitcher) {
		this.textFilterSwitcher = textFilterSwitcher;
	}

	public void setValueFilterSwitcher(Switcher<Filter> valueFilterSwitcher) {
		this.valueFilterSwitcher = valueFilterSwitcher;
	}

	public void setFormatterSwitcher(Switcher<Formatter<Object>> formatterSwitcher) {
		this.formatterSwitcher = formatterSwitcher;
	}

	public void setFilterVariable(String filterVariable) {
		this.filterVariable = filterVariable;
	}

	public void setFormatterVariable(String formatterVariable) {
		this.formatterVariable = formatterVariable;
	}

	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}

	public void setImportMethods(Map<Class<?>, Object> importMethods) {
		this.importMethods = importMethods;
	}

	public void setImportSequences(List<StringSequence> importSequences) {
		this.importSequences = importSequences;
	}

	public void setImportMacros(Map<String, Template> importMacros) {
		this.importMacros = importMacros;
	}

	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
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

}
