/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.methods;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;

/**
 * XmlMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class XmlMethod {

	private XmlMethod() {}

	private static XStream XSTREAM = new XStream(new Xpp3Driver());
	
	public static void setDriver(HierarchicalStreamDriver driver) {
		XSTREAM = new XStream(driver);
	}

	public static String toXml(Object object) {
		return XSTREAM.toXML(object);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseXml(String xml) {
		if (xml == null) {
			return null;
		}
		return (T) XSTREAM.fromXML(xml);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseXml(String xml, Class<T> cls) {
		if (xml == null) {
			return null;
		}
		if (cls == null) {
			return (T) XSTREAM.fromXML(xml);
		}
		try {
			return (T) XSTREAM.fromXML(xml, cls.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
