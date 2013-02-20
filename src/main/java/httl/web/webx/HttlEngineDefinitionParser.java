/*
 * Copyright 2011-2013 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.web.webx;

import static com.alibaba.citrus.springext.util.DomUtil.and;
import static com.alibaba.citrus.springext.util.DomUtil.name;
import static com.alibaba.citrus.springext.util.DomUtil.sameNs;
import static com.alibaba.citrus.springext.util.DomUtil.subElements;
import static com.alibaba.citrus.springext.util.SpringExtUtil.attributesToProperties;
import static com.alibaba.citrus.springext.util.SpringExtUtil.createManagedMap;
import static com.alibaba.citrus.util.Assert.assertNotNull;
import static com.alibaba.citrus.util.StringUtil.trimToEmpty;
import static com.alibaba.citrus.util.StringUtil.trimToNull;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser;
import com.alibaba.citrus.springext.util.DomUtil.ElementSelector;

/**
 * HttlEngineDefinitionParser. (Integration, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class HttlEngineDefinitionParser extends AbstractSingleBeanDefinitionParser<HttlEngine>  {

	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		attributesToProperties(element, builder, "path", "templateEncoding");
		ElementSelector advancedProperties = and(sameNs(element), name("advanced-properties"));
		for (Element subElement : subElements(element)) {
			if (advancedProperties.accept(subElement)) {
				parseAdvancedProperties(subElement, parserContext, builder);
			}
		}
	}

	private void parseAdvancedProperties(Element element,
			ParserContext parserContext, BeanDefinitionBuilder builder) {
		Map<Object, Object> props = createManagedMap(element, parserContext);
		for (Element subElement : subElements(element, and(sameNs(element), name("property")))) {
			String name = assertNotNull(trimToNull(subElement.getAttribute("name")), "propertyName");
			String value = trimToEmpty(subElement.getAttribute("value"));
			props.put(name, value);
		}
		builder.addPropertyValue("advancedProperties", props);
	}
}