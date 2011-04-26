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
package usdlc

import groovy.xml.MarkupBuilder

/**
 * User: Paul Marrington
 * Date: 15/03/11
 * Time: 6:42 PM
 */
abstract class BrowserBuilder extends MarkupBuilder {
	static newInstance(mimeType) {
		def my = Environment.data()
		my.mimeType = mimeType
		def builder
		switch (my.mimeType) {
			case "text/html":
				builder = new HtmlBuilder(new PrintWriter(my.out as PrintStream))
				break
			case "application/javascript":
				builder = new JsBuilder()
				break
			default:
				builder = new TextBuilder()
				break
		}
		builder.my = my
		return builder
	}

	BrowserBuilder() { super() }

	BrowserBuilder(pw) { super(pw as PrintWriter) }

	abstract BrowserBuilder text(content)

	abstract BrowserBuilder error(message)

	abstract BrowserBuilder escape(content)

	abstract _tag(name)

	abstract _end(name)
	/**
	 * Enter a tag and place it on the stack for later closing.
	 * @param name of tag
	 * @return Browser instance for chaining
	 */
	BrowserBuilder tag(name) {
		nodeStack.push(name)
		_tag(name)
		return this
	}
	/**
	 * Close the innermost tag()
	 * @return Browser instance for chaining
	 */
	BrowserBuilder end() {
		_end(nodeStack.pop())
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

	def nodeStack = [], my, pw
}

class HtmlBuilder extends BrowserBuilder {
	HtmlBuilder(pw) { super(pw as PrintWriter) }
	/**
	 * There is a need to dump text that is unprocessed. Calling the standard is unwieldy, so here goes...
	 * @param content text to put into the HTML stream unprocessed - so it can include tags.
	 * @return The builder so you can string them together.
	 */
	HtmlBuilder text(content) {
		mkp.yieldUnescaped content
		return this
	}
	/**
	 * Dump out an error message in a form that the browser will recognise
	 * @param message
	 * @return
	 */
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
	 * Primary mechanism to send raw data so that the browser displays but doesn't process HTML. It will make sure any HTML is in syc before starting.
	 * @param text String to send to the browser
	 * @return this for chaining
	 */
	HtmlBuilder escape(text) {
		mkp.yield text
		return this
	}

	def _tag(name) { createNode(name) }

	def _end(name) { nodeCompleted(null, name) }
}

class TextBuilder extends BrowserBuilder {
	/**
	 * There is a need to dump text that is unprocessed. Calling the standard is unwieldy, so here goes...
	 * @param content text to put into the HTML stream unprocessed - so it can include tags.
	 * @return The builder so you can string them together.
	 */
	TextBuilder text(content) {
		my.out.println content
		return this
	}

	TextBuilder error(message = '') {
		text(message)
		return this
	}
	/**
	 * Primary mechanism to send raw data so that the browser displays but doesn't process HTML. It will make sure any HTML is in syc before starting.
	 * @param text String to send to the browser
	 * @return this for chaining
	 */
	TextBuilder escape(content) {
		text(content)
		return this
	}
	/**
	 * Enter a tag and place it on the stack for later closing.
	 * @param name of tag
	 * @return Browser instance for chaining
	 */
	def _tag(name) { my.out.println("<$name>") }
	/**
	 * Close the innermost tag()
	 * @return Browser instance for chaining
	 */
	def _end(name) { my.out.println("</$name>") }
}

class JsBuilder extends BrowserBuilder {
	@Override
	BrowserBuilder text(Object content) { my.out.println(content); return this }

	BrowserBuilder js(Object code) { return text(code) }

	@Override
	BrowserBuilder error(Object message) {
		text("""
		usdlc.dialog('<pre>$message</pre>')
		usdlc.failed()
		""")
		if (my.query.linkId) {
			text("usdlc.runningLinkClass('a#$my.query.linkId', 'error')")
		}
		return this
	}

	@Override
	BrowserBuilder escape(Object content) { my.out.println(content); return this }

	@Override
	_tag(Object name) { text("document.write('<$name>')") }

	@Override
	_end(Object name) { text("document.write('</$name>')") }
}
