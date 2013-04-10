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

import httl.internal.util.Reqiured;
import httl.internal.util.StringUtils;
import httl.spi.Filter;

/**
 * CommentSyntaxFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTextFilter(Filter)
 * @see httl.spi.translators.InterpretedTranslator#setTextFilter(Filter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CommentSyntaxFilter extends AbstractFilter {
	
	private boolean removeDirectiveBlankLine;

	private String commentLeft;

	private String commentRight;

	/**
	 * httl.properties: remove.directive.blank.line=true
	 */
	public void setRemoveDirectiveBlankLine(boolean removeDirectiveBlankLine) {
		this.removeDirectiveBlankLine = removeDirectiveBlankLine;
	}

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

	public String filter(String key, String value) {
		boolean left = false;
		if (value.startsWith(commentRight)) {
			value = value.substring(commentRight.length());
			left = true;
		}
		boolean right = false;
		if (value.endsWith(commentLeft)) {
			value = value.substring(0, value.length() - commentLeft.length());
			right = true;
		}
		if (removeDirectiveBlankLine && (left || right)) {
			value = StringUtils.trimBlankLine(value, left, right);
		}
		return value;
	}

}