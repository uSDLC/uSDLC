/*
 * Copyright 2011 the Authors for http://usdlc.net
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
package usdlc.actor

import groovy.lang.Closure;

import java.io.PrintStream;
import java.util.regex.Matcher
import java.util.regex.Pattern
import usdlc.Exchange
import usdlc.Exchange.Response;
import usdlc.MimeTypes;
import usdlc.Store
import static usdlc.MimeTypes.mimeType

/**
 * User: paul
 * Date: 5/07/11
 * Time: 10:43 AM
 */
abstract class Actor implements Runnable {
	/** variables to pass between scripts as globals   */
	def context = [:]
	def dslContext = [:]
	/** Http Exchange data - including request and response  */
	Exchange exchange
	/** Convenience to write to the response/browser  */
	PrintStream out
	/** Implement by concrete classes to be run as part of the browser/server exchange  */
	abstract void run()
	/** Actors run with a known binding use by all in the session */
	void run(Map binding) {
		context = binding
		exchange = context.exchange
		out = exchange.response.out
		context.session = exchange.request.session
		context.getters = [:]
		context.setters = [:]
		run()
	}
	/**
	 * Special for a script set up as a DSL inclusion.
	 */
	void runScript() {
		wrapOutput('.txt', context) { delegate.run() }
	}
	/**
	 * Special to run the script while wrapping the output for best effort.
	 */
	void runScript(Map binding) {
		wrapOutput(script.path, binding) { delegate.run(binding) }
	}
	/**
	 * Called when running an actor in-context by using Setup and Cleanup.
	 */
	static void wrap(List<Store> actors, Map context) {
		try {
			def base = Store.base(actors[0].parent)
			runFiles(~/^Setup\..*/, base, context)
			actors.each { Store actor ->
				load(actor)?.runScript(context)
			}
			runFiles(~/^Cleanup\..*/, base, context)
		} catch (AssertionError assertion) {
			error(assertion, context)
		} catch (Throwable throwable) {
			error(throwable, context)
			throwable.printStackTrace()
		} finally {
			context.each { key, value ->
				value.&close ?: value.close()
			}
		}
	}

	static error(Throwable throwable, Map context) {
		wrapOutput('.txt', context, { Response response ->
			response.write throwable.message
			// TODO: Display one line of stack dump also
		})
	}

	static void runFiles(Pattern pattern, Store base, Map context) {
		base.dir(pattern) { String path ->
			load(Store.base(path))?.runScript(context)
		}
	}
	/**
	 * Called to see if a URL refers to an actor/dsl. It creates an instance of the class.
	 * Null is returned if no actor class or dsl script exists.
	 */
	static Actor load(Store store) {
		Matcher match = (store.path =~ ~/\.(\w+)$/)
		def actor = null
		if (match) {
			String language = match[-1][1]
			try {
				if (language in DslActor.cache) {
					actor = DslActor.cache[language].newInstance()
				} else {
					def className = "usdlc.actor.${language.capitalize()}Actor"
					actor = Class.forName(className).newInstance()
				}
			} catch (ClassNotFoundException cnfe) {
				actor = DslActor.newInstance(language).newInstance()
			}
			actor?.script = store
		}
		actor
	}
	Store script
	/**
	 * Wrap data of defined mime-type as if it were to be included in a HTML file
	 */
	static wrapOutput(String fileName, Map context, Closure closure) {
		Response response = context.exchange.response
		def type = mimeType(fileName)
		def wrapper = mimeTypeWrappers.get(mimeType(fileName), ['<!--', '-->'])
		response.write wrapper[0]
		try {
			closure(response)
		} finally {
			response.write wrapper[1]
		}
	}
	static mimeTypeWrappers = [
		'text/html': ['', ''],
		'application/javascript': ['<script>', '</script>'],
		'text/plain': ['<pre>', '</pre>']]
}