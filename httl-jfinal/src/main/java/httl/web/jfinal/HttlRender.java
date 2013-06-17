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

import com.jfinal.render.Render;
import com.jfinal.render.RenderException;

/**
 * HttlRender. (Integration, Prototype, ThreadSafe)
 * 
 * @author dafei (myaniu AT gmail DOT com)
 */
public class HttlRender extends Render {

	private static final long serialVersionUID = -7218493570717379375L;

	public HttlRender(String view) {
		this.view = view;
	}

	@Override
	public void render() {
		try {
			WebEngine.setRequestAndResponse(request, response);
			WebEngine.getEngine().getTemplate(this.view, request.getLocale()).render(response);
		} catch (Exception e) {
			throw new RenderException(e.getMessage(), e);
		}
	}

}
