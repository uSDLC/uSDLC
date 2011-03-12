/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.usdlc

import groovy.xml.MarkupBuilder

/**
 * Class for actors to interact with the browser. The actor normally sends back html or scripts. Both are catered for. It can be used stand-alone or as the markup builder when used with Groovy.
 * User: Paul
 * Date: 31/12/10
 * Time: 2:07 PM
 */
class Browser {
	Browser(PrintStream out) {
		this.out = new PrintWriter(out, true)
	}
	PrintWriter out
	boolean hasMarkup = false
	/*
	 yieldUnescaped in markup-builder does not work until we do some xml because it has an invalid status.
	 */
	def yieldUnescaped = {
		out.print it;
		out.flush()
	}
	@Lazy UsdlcMarkupBuilder html = {
		yieldUnescaped = { html.mkp.yieldUnescaped it }
		return new UsdlcMarkupBuilder(out)
	}()
	/**
	 * Static builder for Javascript response to the browser
	 * @return Browser instance ready to go
	 */
	static Browser js(out) {
		def browser = new Browser(out)
		browser.scriptFile = true
		return browser
	}

	static Browser html(out) {
		def browser = new Browser(out)
		return browser
	}

	private scriptFile = false
	private tags = []
	/**
	 * Primary mechanism to send raw HTML for browser interpretation. It will make sure any HTML is in syc before starting.
	 * @param text String to send to the browser
	 */
	Browser text(text) {
		yieldUnescaped text
		return this
	}
	/**
	 * Primary mechanism to send raw data so that the browser dispays but doesn't process HTML. It will make sure any HTML is in syc before starting. Won't work unless you are using the markup-builder.
	 * @param text String to send to the browser
	 * @return this for chaining
	 */
	Browser escape(text) {
		html.mkp.yield text
		return this
	}
	/**
	 * Enter a tag and place it on the stack for later closing.
	 * @param name of tag
	 * @return Browser instance for chaining
	 */
	Browser tag(name) {
		if (inScript()) { end() }
		text "<$name>"
		// name may include attributes (as in "tag class='myClass'"
		tags.push name.split(/\s+/)[0].toLowerCase()
		return this
	}
	/**
	 * Script tag
	 * @return Browser instance for chaining
	 */
	Browser getScript() {
		tag "script"
		return this
	}
	/**
	 * Are we in a script tag?
	 * @return True if we are in a script tag
	 */
	private inScript() {
		if (scriptFile) return true
		return tags && tags[-1] == 'script'
	}
	/**
	 * Close the innermost tag()
	 * @return Browser instance for chaining
	 */
	Browser end() {
		if (tags) {
			text "</${tags.pop()}>"
		}
		return this
	}
	/**
	 * Close off any open tags and markup-builder
	 */
	void close() {
		//noinspection GroovyWhileLoopSpinsOnField
		while (tags) {
			end()
		}
		text '' // flushes any other markup
		out.close()
	}
	/**
	 * Used for printing out a script statement - wrapping it in a tag of necessary.
	 * @param statement for the browser to execute as a script
	 * @return Browser instance for chaining
	 */
	Browser script(statement) {
		if (inScript()) {
			text statement
		} else {
			script.text(statement).end()
		}
		return this
	}
	/**
	 * Use by ajax methods to display an alert message on the browser - also marking the current section as red.
	 * @param message Message to display. Empty for no message (alert colour only)
	 * @return Browser instance for chaining
	 */
	Browser alert(message) {
		script "usdlc.alert('$message')"
		return this
	}
	/**
	 * Other Browser calls will get the results as a coloured stripe on the left.
	 * @param colour Check /usdlc/alerts for a list of colours.
	 * @return Browser instance for chaining
	 */
	Browser highlight(colour) {
		script "usdlc.highlight('$colour')"
		return this
	}
}

class UsdlcMarkupBuilder extends MarkupBuilder {

	UsdlcMarkupBuilder(PrintWriter pw) { super(pw) }

	def text(content) {
		mkp.yieldUnescaped content
		return this
	}
}
