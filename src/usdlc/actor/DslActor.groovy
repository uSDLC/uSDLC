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

import org.codehaus.groovy.runtime.InvokerHelper

import usdlc.actor.GroovyActor.UsdlcBinding
import static usdlc.Config.config

/**
 * User: paul
 * Date: 24/07/11
 * Time: 5:50 PM
 */
class DslActor extends GroovyActor {
	/**
	 * Called by Actor.groovy to create a new instance for this language. Each if these in
	 * turn can be used to create new running instances for dsl work.
	 */
	static DslActor newInstance(String language) {
		def dsl = "${language.toLowerCase()}DSL"
		if (! (dsl in cache)) {
			cache[dsl] = new DslActor()
			def found = config.dslPath.findResult { path ->
				try {
					Class.forName("${path.replaceAll('/', '.')}$dsl")
				} catch (ClassNotFoundException cnfe) {
					null
				}
			}
			if (! found) {
				try {
					GroovyScriptEngine gse = new GroovyScriptEngine(config.dslPathUrls as URL[])
					found = gse.loadScriptByName("${dsl}.groovy")
				} catch (ResourceException re) {
					found = null
				}
			}
			cache[dsl].languageScriptClass = found
		}
		cache[dsl]
	}
	/**
	 * The Script sub-class created by the groovy compiler from script source - or null if there is no source.
	 */
	Class languageScriptClass
	/**
	 * Called to create an instance to run in Actor.load - a clone.
	 * Will return null if the script asked for does not exist.
	 */
	def newInstance() {
		def clone = null
		if (languageScriptClass) {
			clone = new DslActor()
			clone.languageScriptClass = languageScriptClass
		}
		clone
	}
	/**
	 * Here lies the real work. Instantiate and run the dsl definition script, then the script to run. Note that
	 * they both used each other contexts.
	 */
	void run() {
		init()
		def dslBinding = new UsdlcBinding(dslContext, context)
		Script languageScript = InvokerHelper.createScript(languageScriptClass, dslBinding)
		languageScript.run()
		if (script) {
			context.gse.run script.path, context.usdlcBinding
		}
	}
	/**
	 * Keep a cache of previous instances - one per language - so we don't have to recompile. The cache includes
	 * instances that failed to find a script.
	 */
	static cache = [:]
}
