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

import groovy.lang.Closure
import groovy.transform.AutoClone

import java.io.PrintStream
import java.util.regex.Matcher
import java.util.regex.Pattern
import usdlc.Exchange
import usdlc.Exchange.Response
import usdlc.Groovy
import usdlc.Log;
import usdlc.Store
import static usdlc.Config.config


@AutoClone class Actor {
	/** variables to pass between scripts as globals   */
	def context = [:]
	def dslContext = [:]
	/** Http Exchange data - including request and response  */
	Exchange exchange
	/** Convenience to write to the response/browser  */
	PrintStream out
	/**
	 * Set to false if you want the actor to be ignored
	 */
	boolean exists = true
	/**
	 * A list of scripts used to build up the DSL
	 */
	def backingScripts = []
	/** 
	 * Implement by concrete classes to be run as part of the browser/server 
	 * exchange
	 */
	void run(Store script) {
	}
	/**
	 * Initialisation before running backing or target scripts
	 */
	void init() {
	}
	/** Actors run with a known binding use by all in the session */
	void run(Map binding) {
		context = binding
		exchange = context.exchange
		out = exchange.response.out
		context.session = exchange.request.session
		context.getters = [:]
		context.setters = [:]
		init()
		backingScripts.each { authorisedRun(it) }
		if (script) authorisedRun(script)
	}
	void authorisedRun(Store script) {
		if (exchange.request.user.authorised(script, 'run')) run(script)
	}
	static internalExceptions = ~/\.groovy\.|^groovy\.|\.java\.*/
	/**
	 * Called to see if a URL refers to an actor/dsl. It creates an instance of 
	 * the class. Null is returned if no actor class or dsl script exists.
	 */
	static Actor load(Store store) {
		String language = language(store)
		if (!cache[language]) {
			def data = [scripts: [], groovyDSL: '', baseLanguage: '']
			retrieveDefinitions(language, data)
			if (data.groovyDSL) {
				cache[language] = data.groovyDSL
			} else {
				def actorClass = Groovy.loadClass("usdlc.actor.${data.baseLanguage.capitalize()}Actor")
				cache[language] = actorClass ? 
					actorClass.newInstance() : 
					new DslActor("${data.baseLanguage.toLowerCase()}DSL")
			}
			cache[language].backingScripts = data.scripts
		}
		if (!cache[language].exists) return null
		def clone = cache[language].clone()
		clone.script = store
		clone
	}
	private static retrieveDefinitions(language, data) {
		def dsl = "${language.toLowerCase()}DSL"
		config.dslSourcePath.each { path ->
			def base = Store.base(path)
			base.dirs(~/${dsl}\..*/) { Store store ->
				def parentLanguage = Actor.language(store).toLowerCase()
				def parentDSL = "${parentLanguage}DSL"
				switch (parentLanguage) {
					case language:
						data.scripts << store
						break
					case 'groovy':
						data.groovyDSL = new DslActor(parentDSL)
						break
					default:
						retrieveDefinitions(parentLanguage, data)
						data.scripts << store
						break
				}
			}
		}
		if (!data.baseLanguage) data.baseLanguage = language
	}
	/**
	 * Given the store to a file, find out what language it is written in - based on extension.
	 */
	static String language(Store store) {
		Matcher match = (store.path =~ ~/\.([\w\-]+)$/)
		match ? match[-1][1].replaceAll(/\-/, '') : ''
	}
	/**
	 * The script we want to run
	 */
	Store script
	/**
	 * Keep a cache of previous instances - one per language - so we don't have to recompile. The cache includes
	 * instances that failed to find a script.
	 */
	static cache = [:]
}