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
package httl.spi.filters;

import httl.spi.Filter;
import httl.util.Reqiured;
import httl.util.StringUtils;

/**
 * CommentSyntaxFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTextFilter(Filter)
 * @see httl.spi.translators.InterpretedTranslator#setTextFilter(Filter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CommentSyntaxFilter extends AbstractFilter {

	private String commentLeft;

	private String commentRight;

	private boolean removeDirectiveBlankLine;

	/**
	 * httl.properties: comment.left=&lt;!--
	 */
	@Reqiured
	public void setCommentLeft(String commentLeft) {
		this.commentLeft = commentLeft;
	}

	/**
	 * httl.properties: comment.right=--&gt;
	 */
	@Reqiured
	public void setCommentRight(String commentRight) {
		this.commentRight = commentRight;
	}

	/**
	 * httl.properties: remove.directive.blank.line=true
	 */
	public void setRemoveDirectiveBlankLine(boolean removeDirectiveBlankLine) {
		this.removeDirectiveBlankLine = removeDirectiveBlankLine;
	}

	public String filter(String key, String value) {
		boolean left = false;
		boolean right = false;
		String trim = value.trim();
		if (trim.startsWith(commentRight)) {
			value = value.substring(value.indexOf(commentRight) + commentRight.length());
			left = true;
		}
		if (trim.endsWith(commentLeft)) {
			value = value.substring(0, value.lastIndexOf(commentLeft));
			right = true;
		}
		if (removeDirectiveBlankLine && (left || right)) {
			value = StringUtils.trimBlankLine(value, left, right);
		}
		return value;
	}

}