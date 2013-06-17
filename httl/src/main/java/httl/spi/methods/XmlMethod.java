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
package httl.spi.methods;

import java.text.ParseException;

/**
 * XmlMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @deprecated Replace to <code>CodecMethod</code>
 * @see httl.spi.methods.CodecMethod
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
@Deprecated
public class XmlMethod extends CodecMethod {

	public String toXml(Object object) {
		return super.encodeXml(object);
	}

	public Object parseXml(String xml) throws ParseException {
		return super.decodeXml(xml);
	}

	public <T> T parseXml(String xml, Class<T> cls) throws ParseException {
		return super.decodeXml(xml, cls);
	}

	public Object parseXml(String xml, String cls) throws ParseException {
		return super.decodeXml(xml, cls);
	}

}