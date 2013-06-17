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
package httl.web.nutz;

import httl.web.WebEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.view.AbstractPathView;

/**
 * HttlView. (Integration, Prototype, ThreadSafe)
 *
 * @author x-tony AT live DOT cn
 */
public class HttlView extends AbstractPathView {

	public HttlView(String dest) {
		super(dest);
	}

	public void render(HttpServletRequest req, HttpServletResponse resp, Object obj) throws Throwable {
		String path = evalPath(req, obj);
		WebEngine.setRequestAndResponse(req, resp);
		WebEngine.getEngine().getTemplate(getTemplatePath(path, req), req.getLocale()).render(obj, resp);
	}

	protected String getTemplatePath(String path, HttpServletRequest request) {
		String ext = WebEngine.getTemplateSuffix();
		if (Strings.isBlank(path)) { // 空路径，采用默认规则
			path = Files.renameSuffix(Mvcs.getRequestPath(request), ext);
			if (! path.startsWith("/")) {
				path = "/" + path;
			}
		} else if (path.charAt(0) == '/') { // 绝对路径 : 以 '/' 开头的路径不增加 '/WEB-INF'
			if (! path.toLowerCase().endsWith(ext)) {
				path += ext;
			}
		} else { // 包名形式的路径
			path = path.replace('.', '/') + ext;
		}
		return path;
	}
}
