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
package httl.web.jfinal;

import httl.web.WebEngine;

import com.jfinal.core.JFinal;
import com.jfinal.render.IMainRenderFactory;
import com.jfinal.render.Render;

/**
 * HttlRenderFactory. (Integration, Singleton, ThreadSafe)
 * 
 * @author dafei (myaniu AT gmail DOT com)
 */
public class HttlRenderFactory implements IMainRenderFactory {

	public Render getRender(String view) {
		return new HttlRender(view);
	}

	public String getViewExtension() {
		return WebEngine.getTemplateSuffix(JFinal.me().getServletContext());
	}

}
