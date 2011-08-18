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
import groovy.transform.AutoClone;

import java.io.PrintStream;
import java.util.regex.Matcher
import java.util.regex.Pattern
import usdlc.Exchange
import usdlc.Exchange.Response;
import usdlc.Store

/**
 * User: paul
 * Date: 5/07/11
 * Time: 10:43 AM
 */
@AutoClone abstract class Actor implements Runnable {
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
	static internalExceptions = ~/\.groovy\.|^groovy\.|\.java\.*/
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
}