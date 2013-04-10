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
package httl.spi;

import httl.Context;

import java.io.IOException;
import java.text.ParseException;

/**
 * Render Interceptor. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setInterceptor(Interceptor)
 * @see httl.spi.translators.InterpretedTranslator#setInterceptor(Interceptor)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public interface Interceptor {

	/**
	 * Intercept the template rendition.
	 * 
	 * @param context - render context
	 * @param listener - render listener.
	 * @throws IOException - If an I/O error occurs
	 * @throws ParseException - If the template cannot be parsed on runtime
	 */
	void render(Context context, Listener listener) throws IOException, ParseException;

}