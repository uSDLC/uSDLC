/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.usdlc

import groovy.xml.MarkupBuilder

/**
 * User: Paul Marrington
 * Date: 15/03/11
 * Time: 6:42 PM
 */
class HtmlBuilder extends MarkupBuilder {
	HtmlBuilder() { super(new PrintWriter((my = Environment.data()).out as PrintStream)) }
	/**
	 * There is a need to dump text that is unprocessed. Calling the standard is unweildy, so here goes...
	 * @param content text to put into the HTML stream unprocessed - so it can include tags.
	 * @return The builder so you can string them together.
	 */
	HtmlBuilder text(content) {
		"".splitEachLine(/a/) {}
		mkp.yieldUnescaped content
		return this
	}

	HtmlBuilder error(message = '') {
		pre {
			text(message)
		}
		script {
			text('usdlc.failed();')
			if (my.query.linkId) {
				text("usdlc.runningLinkClass('a#$my.query.linkId', 'error')")
			}
		}
		return this
	}
	/**
	 * Primary mechanism to send raw data so that the browser dispays but doesn't process HTML. It will make sure any HTML is in syc before starting.
	 * @param text String to send to the browser
	 * @return this for chaining
	 */
	HtmlBuilder escape(text) {
		mkp.yield text
		return this
	}
	/**
	 * Enter a tag and place it on the stack for later closing.
	 * @param name of tag
	 * @return Browser instance for chaining
	 */
	HtmlBuilder tag(name) {
		nodeStack.push(name)
		createNode(name)
		return this
	}
	/**
	 * Close the innermost tag()
	 * @return Browser instance for chaining
	 */
	HtmlBuilder end() {
		nodeCompleted(null, nodeStack.pop())
		return this
	}
	/**
	 * Close off any open tags and markup-builder
	 */
	void close() {
		//noinspection GroovyWhileLoopSpinsOnField
		while (nodeStack) { end() }
		my.out.close()
	}

	def nodeStack = [], pw, my
}
