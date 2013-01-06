/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package httl.spi.methods;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import httl.util.Base64;
import httl.util.Digest;

/**
 * CodecMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CodecMethod {

	private CodecMethod() {}

	public static String toMd5(String value) {
		return value == null ? null : Digest.getMD5(value);
	}

	public static String toSha(String value) {
		return value == null ? null : Digest.getSHA(value);
	}

	public static String toDigest(String value, String digest) {
		return value == null ? null : Digest.getDigest(digest, value);
	}

	public static String toBase64(String value) {
		return value == null ? null : Base64.encodeBytes(value.getBytes());
	}

	public static String parseBase64(String value) {
		try {
			return value == null ? null : new String(Base64.decode(value));
		} catch (IOException e) {
			return value;
		}
	}

	public static String toXbean(Object object) {
		if (object == null) {
			return null;
		}
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		XMLEncoder xe = new XMLEncoder(bo);
		try {
			xe.writeObject(object);
			xe.flush();
		} finally {
			xe.close();
		}
		return new String(bo.toByteArray());
	}

	public static Object parseXbean(String xml) {
		if (xml == null) {
			return null;
		}
		ByteArrayInputStream bi = new ByteArrayInputStream(xml.getBytes());
		XMLDecoder xd = new XMLDecoder(bi);
		try {
			return xd.readObject();
		} finally {
			xd.close();
		}
	}

}
