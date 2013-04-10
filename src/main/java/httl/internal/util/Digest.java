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
package httl.internal.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {

	public static String getMD5(String value) {
		return getDigest("MD5", value);
	}

	public static String getSHA(String value) {
		return getDigest("SHA", value);
	}
	
	public static String getDigest(String digest, String value) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(digest);
			messageDigest.reset();
			messageDigest.update(value.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		return getHEX(messageDigest.digest());
	}

	public static String getHEX(byte[] byteArray) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				buf.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				buf.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return buf.toString();
	}

}