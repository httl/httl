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
package httl.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * UrlUtils. (Tool, Prototype, ThreadUnsafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class UrlUtils {

	public static final String PROTOCOL_SEPARATOR = "://";

	public static final String PATH_SEPARATOR = "/";

	public static final char PATH_SEPARATOR_CHAR = '/';

	public static final String PARENT_PATH = "..";

	public static final String CURRENT_PATH = ".";

	/**
	 * 清理相对路径. 处理"../"和"./"相对于根目录"/"的正确路径.
	 *
	 * @param url
	 *            相对路径
	 * @return 对根目录的绝对路径
	 * @throws MalformedURLException
	 *             访问路径超越根目录时抛出
	 * @throws NullPointerException
	 *             传入path为空时抛出
	 */
	public static String cleanUrl(String url) throws MalformedURLException {
		if (url == null)
			throw new MalformedURLException("url == null!");

		String domain = "";
		int idx = getDomainIndex(url);
		if (idx > 0) {
			domain = url.substring(0, idx);
			url = url.substring(idx);
		}

		url = url.replace('\\', PATH_SEPARATOR_CHAR);

		String[] tokens = url.split(PATH_SEPARATOR);
		LinkedList<String> list = new LinkedList<String>();
		for (int i = 0, n = tokens.length; i < n; i++) {
			if (PARENT_PATH.equals(tokens[i])) {
				if (list.isEmpty())
					throw new MalformedURLException("非法路径访问，不允许\"../\"访问根目录\"/\"以上的目录！");
				list.removeLast();
			} else if (tokens[i] != null && tokens[i].trim().length() > 0
					&& !CURRENT_PATH.equals(tokens[i])) {
				list.addLast(tokens[i]);
			}
		}
		StringBuffer buf = new StringBuffer();
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			buf.append(PATH_SEPARATOR);
			buf.append((String) iterator.next());
		}
		return domain + buf.toString();
	}

	public static String relativeUrl(String templateName, String relativeName) throws MalformedURLException {
		if (templateName == null || relativeName == null)
			return templateName;
		templateName = templateName.replace('\\', '/');
		relativeName = relativeName.replace('\\', '/');
		if (templateName.trim().charAt(0) == UrlUtils.PATH_SEPARATOR_CHAR) // 根目录开头，不添加当前路径
			return UrlUtils.cleanUrl(templateName);
		return UrlUtils.cleanUrl(UrlUtils.getDirectoryName(relativeName) + templateName);
	}

	/**
	 * 获取不包括文件名的路径
	 *
	 * @param url 路径
	 * @return 去掉文件名的路径
	 */
	public static String getDirectoryName(String url) {
		if (url != null) {
			int idx = url.lastIndexOf(PATH_SEPARATOR_CHAR);
			if (idx >= 0)
				return url.substring(0, idx + 1);
		}
		return PATH_SEPARATOR;
	}

	/**
	 * 获取文件名称，不包含目录的名称。
	 *
	 * @param url 路径
	 * @return 文件名称
	 */
	public static String getFileName(String url) {
		if (url == null)
			return null;
		int begin = url.lastIndexOf(PATH_SEPARATOR_CHAR);
		return url.substring(begin + 1);
	}

	/**
	 * 获取最简名称，不包含目录和后缀的名称。
	 *
	 * @param url 路径
	 * @return 最简名称
	 */
	public static String getSimpleName(String url) {
		if (url == null)
			return null;
		int begin = url.lastIndexOf(PATH_SEPARATOR_CHAR);
		int end = url.lastIndexOf(".");
		if (end < 0)
			end = url.length();
		return url.substring(begin + 1, end);
	}

	/**
	 * 获取URL域名的分割位置
	 *
	 * @param url 路径
	 * @return 域名分割位置
	 */
	public static int getDomainIndex(String url) {
		if (url != null) {
			int protocolIndex = url.indexOf(PROTOCOL_SEPARATOR);
			if (protocolIndex > 0) {
				int domainIndex = url.indexOf(PATH_SEPARATOR_CHAR, protocolIndex + PROTOCOL_SEPARATOR.length());
				if (domainIndex == -1) { // 只有域名的URL
					return url.length();
				} else {
					return domainIndex;
				}
			}
		}
		return -1;
	}

	// 如: 将C:\Documents and Settings\test.html 改成: C:\"Documents and Settings"\test.html
	public static String cleanWindowsPath(String path) {
		if (path != null) {
			String[] tokens = path.split("[\\/|\\\\]");
			if (tokens != null && tokens.length > 0) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0, n = tokens.length; i < n; i ++) {
					String token = tokens[i];
					if (token != null && token.length() > 0) {
						if (i != 0)
							buf.append(File.separatorChar);
						if (token.indexOf(' ') > -1) {
							buf.append('\"');
							buf.append(token);
							buf.append('\"');
						} else {
							buf.append(token);
						}
					}
				}
				return buf.toString();
			}
		}
		return path;
	}
	
	public static final String JAR_URL_SEPARATOR = "!/";

    /** URL prefix for loading from the file system: "file:" */
    public static final String FILE_URL_PREFIX = "file:";
    
    public static List<String> listUrl(URL rootDirUrl, String[] suffixes) throws IOException {
        if ("file".equals(rootDirUrl.getProtocol())) {
            return listFile(new File(rootDirUrl.getFile()), suffixes);
        } else {
            return listJarUrl(rootDirUrl, suffixes);
        }
    }
    
    public static List<String> listFile(File file, final String[] suffixes) throws IOException {
        if (suffixes == null || suffixes.length == 0) {
            return Arrays.asList(file.list());
        } else {
            return Arrays.asList(file.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    for (String suffix : suffixes) {
                        if (name.endsWith(suffix)) {
                            return true;
                        }
                    }
                    return false;
                }
            }));
        }
    }
    
    public static List<String> listZip(ZipFile zipFile, String[] suffixes) throws IOException {
        List<String> result = new ArrayList<String>();
        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String entryPath = entry.getName();
            result.add(entryPath);
        }
        return result;
    }
    
    public static List<String> listJar(JarFile jarFile, String[] suffixes) throws IOException {
        List<String> result = new ArrayList<String>();
        for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String entryPath = entry.getName();
            result.add(entryPath);
        }
        return result;
    }
    
    private static List<String> listJarUrl(URL rootDirUrl, String[] suffixes) throws IOException {
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
                    for (String suffix : suffixes) {
                        if (relativePath.endsWith(suffix)) {
                            result.add(relativePath);
                        }
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
