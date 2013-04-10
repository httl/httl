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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *  (Tool, Prototype, ThreadUnsafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class UrlUtils {

	public static final String PROTOCOL_SEPARATOR = "://";

	public static final String PATH_SEPARATOR = "/";

	public static final char PATH_SEPARATOR_CHAR = '/';

	public static final char PATH_PARENT_CHAR = '.';

	public static final char WINDOWS_PATH_SEPARATOR_CHAR = '\\';

	public static String relativeUrl(String name , String relativeName) throws MalformedURLException {
		if (StringUtils.isEmpty(name) 
				|| StringUtils.isEmpty(relativeName))
			return name;
		if (name.charAt(0) == PATH_SEPARATOR_CHAR
				|| name.charAt(0) == WINDOWS_PATH_SEPARATOR_CHAR)
			return name; // 根目录开头，不添加当前路径
		int parent = getParentLevel(name); // 双点号开头，表示上级目录
		if (parent > 0) {
			name = name.substring(parent * 3);
		}
		return getParentDirectory(relativeName, parent) + name;
	}
	
	public static int getParentLevel(String name) {
		int max = name.length() - 3;
		int n = 0;
		for (int i = 0; i < max; i += 3) {
			if (name.charAt(i) == PATH_PARENT_CHAR
					&& name.charAt(i + 1) == PATH_PARENT_CHAR
					&& (name.charAt(i + 2) == PATH_SEPARATOR_CHAR
						|| name.charAt(i + 2) == WINDOWS_PATH_SEPARATOR_CHAR)) {
				n ++;
			} else {
				break;
			}
		}
		return n;
	}
	
	public static String removeSuffix(String name) {
		int i = name.lastIndexOf('.');
		if (i > 0) {
			int j = name.lastIndexOf('/');
			if (i > j) {
				return name.substring(i);
			}
		}
		return name;
	}
	
	public static String cleanName(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("name == null");
		}
		int len = name.length();
		StringBuilder buf = null;
		for (int i = 0; i < len; i ++) {
			char ch = name.charAt(i);
			if (ch == '\\') {
				if (buf == null) {
					buf = new StringBuilder(len);
					buf.append(name.substring(0, i));
				}
				buf.append('/');
			} else if (i == 0 && ch != '/') {
				buf = new StringBuilder(len + 1);
				buf.append('/');
				buf.append(ch);
			} else if (buf != null) {
				buf.append(ch);
			}
		}
		if (buf != null) {
			return buf.toString();
		}
		return name;
	}
	
	public static String[] cleanDirectory(String[] names) {
		String[] result = new String[names.length];
		for (int i = 0; i < names.length; i ++) {
			result[i] = cleanDirectory(names[i]);
		}
		return result;
	}

	public static String cleanDirectory(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("directory == null");
		}
		int last = name.length() - 1;
		StringBuilder buf = null;
		for (int i = 0; i <= last; i ++) {
			char ch = name.charAt(i);
			if (i == last && (ch == '/' || ch == '\\')) {
				if (buf == null) {
					return name.substring(0, last);
				} else {
					// ignore
				}
			} else if (ch == '\\') {
				if (buf == null) {
					buf = new StringBuilder(name.length());
					buf.append(name.substring(0, i));
				}
				buf.append('/');
			} else if (i == 0 && ch != '/') {
				buf = new StringBuilder(name.length() + 1);
				buf.append('/');
				buf.append(ch);
			} else if (buf != null) {
				buf.append(ch);
			}
		}
		if (buf != null) {
			return buf.toString();
		}
		return name;
	}

	public static String getParentDirectory(String url, int parent) {
		if (url != null) {
			for (int i = url.length() - 1; i >= 0; i --) {
				char ch = url.charAt(i);
				if (ch == PATH_SEPARATOR_CHAR
						|| ch == WINDOWS_PATH_SEPARATOR_CHAR) {
					parent --;
					if (parent < 0) {
						return url.substring(0, i + 1);
					}
				}
			}
		}
		return PATH_SEPARATOR;
	}

	public static final String JAR_URL_SEPARATOR = "!/";

	/** URL prefix for loading from the file system: "file:" */
	public static final String FILE_URL_PREFIX = "file:";
	
	public static List<String> listUrl(URL rootDirUrl, String suffix) throws IOException {
		if ("file".equals(rootDirUrl.getProtocol())) {
			return listFile(new File(rootDirUrl.getFile()), suffix);
		} else {
			return listJarUrl(rootDirUrl, suffix);
		}
	}
	
	public static List<String> listFile(File dirFile, final String suffix) throws IOException {
		List<String> list = new ArrayList<String>();
		addListFile(list, "/", dirFile, suffix);
		return list;
	}
	
	private static void addListFile(List<String> list, String dir, File dirFile, final String suffix) throws IOException {
		for (File file : dirFile.listFiles()) {
			if (file.isHidden() || ! file.canRead()) {
				continue;
			}
			if (file.isDirectory()) {
				addListFile(list, dir + file.getName() + "/", file, suffix);
			} else if (isMatch(file.getName(), suffix)) {
				list.add(dir + file.getName());
			}
		}
	}
	
	private static boolean isMatch(String name, String suffix) {
		if (StringUtils.isEmpty(suffix)) {
			return true;
		}
		return name.endsWith(suffix);
	}
	
	public static List<String> listZip(ZipFile zipFile, String suffix) throws IOException {
		List<String> result = new ArrayList<String>();
		for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			if (! entry.isDirectory()) {
				String name = entry.getName();
				if (isMatch(name, suffix)) {
					if (! name.startsWith("/")) {
						name = "/" + name;
					}
					result.add(name);
				}
			}
		}
		return result;
	}
	
	public static List<String> listJar(JarFile jarFile, String suffix) throws IOException {
		List<String> result = new ArrayList<String>();
		for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
			JarEntry entry = (JarEntry) entries.nextElement();
			if (! entry.isDirectory()) {
				String name = entry.getName();
				if (isMatch(name, suffix)) {
					if (! name.startsWith("/")) {
						name = "/" + name;
					}
					result.add(name);
				}
			}
		}
		return result;
	}

	private static List<String> listJarUrl(URL rootDirUrl, String suffix) throws IOException {
		URLConnection con = rootDirUrl.openConnection();
		JarFile jarFile = null;
		String jarFileUrl = null;
		String rootEntryPath = null;
		boolean newJarFile = false;
		if (con instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			JarURLConnection jarCon = (JarURLConnection) con;
			jarCon.setUseCaches(false);
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			JarEntry jarEntry = jarCon.getJarEntry();
			rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
		} else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			String urlFile = rootDirUrl.getFile();
			int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
			if (separatorIndex != -1) {
				jarFileUrl = urlFile.substring(0, separatorIndex);
				rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
				jarFile = getJarFile(jarFileUrl);
			}
			else {
				jarFile = new JarFile(urlFile);
				jarFileUrl = urlFile;
				rootEntryPath = "";
			}
			newJarFile = true;
		}
		try {
			if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
				// Root entry path must end with slash to allow for proper matching.
				// The Sun JRE does not return a slash here, but BEA JRockit does.
				rootEntryPath = rootEntryPath + "/";
			}
			List<String> result = new ArrayList<String>();
			for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = (JarEntry) entries.nextElement();
				String entryPath = entry.getName();
				if (entryPath.startsWith(rootEntryPath)) {
					String relativePath = entryPath.substring(rootEntryPath.length());
					if (relativePath.endsWith(suffix)) {
						result.add(relativePath);
					}
				}
			}
			return result;
		} finally {
			// Close jar file, but only if freshly obtained -
			// not from JarURLConnection, which might cache the file reference.
			if (newJarFile) {
				jarFile.close();
			}
		}
	}

	private static JarFile getJarFile(String jarFileUrl) throws IOException {
		if (jarFileUrl.startsWith(FILE_URL_PREFIX)) {
			try {
				return new JarFile(toURI(jarFileUrl).getSchemeSpecificPart());
			}
			catch (URISyntaxException ex) {
				// Fallback for URLs that are not valid URIs (should hardly ever happen).
				return new JarFile(jarFileUrl.substring(FILE_URL_PREFIX.length()));
			}
		}
		else {
			return new JarFile(jarFileUrl);
		}
	}
	
	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}
	
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(location.replace(" ", "%20"));
	}
	
}