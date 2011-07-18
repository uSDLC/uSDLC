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

import java.util.regex.Matcher
import java.util.regex.Pattern
import usdlc.Exchange
import usdlc.Store

/**
 * User: paul
 * Date: 5/07/11
 * Time: 10:43 AM
 */
abstract class Actor implements Runnable {
	/** variables to pass between scripts as globals       */
	Map context
	/** Http Exchange data - including request and response       */
	Exchange exchange
	/** Convenience to write to the response/browser       */
	PrintStream out
	/**
	 * Implement by concrete classes to be run as part of the browser/server exchange
	 */
	abstract void run()

	void run(Map binding) {
		context = binding
		exchange = context['exchange']
		out = exchange.response.out
		run()
	}
	/**
	 * Called when an actor is found in the source. It creates an instance of the class.
	 */
	static Actor load(Store store) {
		Matcher match = (store.path =~ ~/\.(\w+)$/)
		if (match) {
			String language = (match[-1] as List)[1]
			def className = "usdlc.actor.${language.capitalize()}Actor"
			try {
				def instance = Class.forName(className).newInstance()
				return instance
			} catch (exception) { null }
		}
		null
	}
	/**
	 * Called when running an actor in-context by using Setup and Teardown.
	 */
	static void wrap(List<Store> actors, Map context) {
		try {
			def base = Store.base(actors[0].parent)
			runFiles(~/^Setup\..*/, base, context)
			actors.each { Store actor -> load(actor)?.run(context) }
			runFiles(~/^Teardown\..*/, base, context)
		} finally { closeBinding(context) }
	}

	private static closeBinding(Map context) {
		context.each { key, value -> value?.close() }
	}

	static void runFiles(Pattern pattern, Store base, Map binding) {
		base.dir(pattern) { String path -> load(Store.base(path))?.run(binding) }
	}
}