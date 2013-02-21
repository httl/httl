package httl.spi.filters;

import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

public class AttributeSyntaxFilter extends AbstractFilter {

	private String varDirective = "var";

	private String setDirective = "set";

	private String ifDirective = "if";

	private String elseifDirective = "elseif";

	private String elseDirective = "else";

	private String foreachDirective = "foreach";

	private String breakifDirective = "breakif";

	private String macroDirective = "macro";

	private String endDirective = "end";

	private String attributeNamespace;

	/**
	 * httl.properties: var.directive=var
	 */
	public void setVarDirective(String varDirective) {
		this.varDirective = varDirective;
	}

	/**
	 * httl.properties: set.directive=set
	 */
	public void setSetDirective(String setDirective) {
		this.setDirective = setDirective;
	}

	/**
	 * httl.properties: if.directive=if
	 */
	public void setIfDirective(String ifDirective) {
		this.ifDirective = ifDirective;
	}

	/**
	 * httl.properties: elseif.directive=elseif
	 */
	public void setElseifDirective(String elseifDirective) {
		this.elseifDirective = elseifDirective;
	}

	/**
	 * httl.properties: else.directive=else
	 */
	public void setElseDirective(String elseDirective) {
		this.elseDirective = elseDirective;
	}

	/**
	 * httl.properties: foreach.directive=foreach
	 */
	public void setForeachDirective(String foreachDirective) {
		this.foreachDirective = foreachDirective;
	}

	/**
	 * httl.properties: breakif.directive=breakif
	 */
	public void setBreakifDirective(String breakifDirective) {
		this.breakifDirective = breakifDirective;
	}

	/**
	 * httl.properties: macro.directive=macro
	 */
	public void setMacroDirective(String macroDirective) {
		this.macroDirective = macroDirective;
	}

	/**
	 * httl.properties: end.directive=end
	 */
	public void setEndDirective(String endDirective) {
		this.endDirective = endDirective;
	}

	/**
	 * httl.properties: attribute.namespace=httl
	 */
	public void setAttributeNamespace(String attributeNamespace) {
		if (! attributeNamespace.endsWith(":")) {
			attributeNamespace = attributeNamespace + ":";
		}
		this.attributeNamespace = attributeNamespace;
	}

	private boolean isDirective(String name) {
		return varDirective.equals(name) || setDirective.equals(name) 
				 ||ifDirective.equals(name) || elseifDirective.equals(name)
				 || elseDirective.equals(name) || foreachDirective.equals(name)
				 || breakifDirective.equals(name) || macroDirective.equals(name) 
				 || endDirective.equals(name);
	}

	// 是否为块指令判断
	private boolean isBlockDirective(String name) {
		return ifDirective.equals(name) || elseifDirective.equals(name)
				 || elseDirective.equals(name) || foreachDirective.equals(name)
				  || macroDirective.equals(name);
	}

	public String filter(String key, String value) {
		Source source = new Source(value);
		OutputDocument document = new OutputDocument(source);
		replaceChildren(source, document);
		return document.toString();
	}

	// 替换子元素中的指令属性
	private void replaceChildren(Segment segment, OutputDocument document) {
		// 迭代子元素，逐个查找
		List<Element> elements = segment.getChildElements();
		if (elements != null) {
			for (Element element : elements) {
				if (element != null) {
					// ---- 标签属性处理 ----
					List<String> blockDirectiveNames = new ArrayList<String>();
					List<String> blockDirectiveValues = new ArrayList<String>();
					List<Attribute> blockDirectiveAttributes = new ArrayList<Attribute>();
					String lineDirectiveName = null;
					String lineDirectiveValue = null;
					// 迭代标签属性，查找指令属性
					Attributes attributes = element.getAttributes();
					if (attributes != null) {
						for (Attribute attribute : attributes) {
							if (attribute != null) {
								String name = attribute.getName();
								if (name != null && (isDirective(name) || (attributeNamespace != null && name.startsWith(attributeNamespace)))) { // 识别名称空间
									String directiveName = attributeNamespace != null ? name.substring(attributeNamespace.length()) : name;
									if (directiveName.matches("^[a-z|A-Z|0-9|_|\\.]+$")) { // 符合命名
										String value = attribute.getValue();
										if (isBlockDirective(directiveName)) {
											blockDirectiveNames.add(directiveName);
											blockDirectiveValues.add(value);
											blockDirectiveAttributes.add(attribute);
										} else {
											if (lineDirectiveName != null)
												throw new RuntimeException("一个标签上只能有一个<b>行指令</b>属性! 出现两个行指令属性: " + lineDirectiveName + "和" + directiveName + ", 请检查标签:" + element.getStartTag().toString());
											lineDirectiveName = directiveName;
											lineDirectiveValue = value;
										}
									}
								}
							}
						}
					}
					// ---- 块指令处理 ----
					if (blockDirectiveNames.size() > 0) {
						StringBuffer buf = new StringBuffer();
						for (int i = 0; i < blockDirectiveNames.size(); i ++) { // 按顺序添加块指令
							String blockDirectiveName = (String)blockDirectiveNames.get(i);
							String blockDirectiveValue = (String)blockDirectiveValues.get(i);
							buf.append("#");
							buf.append(blockDirectiveName);
							buf.append("(");
							buf.append(blockDirectiveValue);
							buf.append(")");
						}
						document.insert(element.getBegin(), buf.toString()); // 插入块指令
					}
					// ---- 行指令处理 ----
					if (lineDirectiveName != null) { // 如果是行指令, 替换整个标签内容.
						StringBuffer buf = new StringBuffer();
						buf.append("#");
						buf.append(lineDirectiveName);
						buf.append("(");
						buf.append(lineDirectiveValue);
						buf.append(")");
						document.replace(element.getBegin(), element.getEnd(), buf.toString()); // 替换为行指令
					} else { // 否则表示全为块指令
						for (int i = 0; i < blockDirectiveAttributes.size(); i ++) {
							Attribute attribute = (Attribute)blockDirectiveAttributes.get(i);
							document.remove(attribute); // 移除属性
						}
						replaceChildren(element, document); // 递归处理子标签
					}
					// ---- 结束指令处理 ----
					if (blockDirectiveNames.size() > 0) {
						StringBuffer buf = new StringBuffer();
						for (int i = blockDirectiveNames.size() - 1; i >= 0; i --) { // 倒序添加结束指令
							String blockDirectiveName = (String)blockDirectiveNames.get(i);
							buf.append("#");
							buf.append(endDirective);
							buf.append("(");
							buf.append(blockDirectiveName);
							buf.append(")");
						}
						document.insert(element.getEnd(), buf.toString()); // 插入结束指令
					}
					// 清理临时容器
					blockDirectiveNames.clear();
					blockDirectiveValues.clear();
					blockDirectiveAttributes.clear();
				}
			}
		}
	}

}
