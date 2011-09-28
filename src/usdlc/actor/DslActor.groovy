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

import usdlc.Groovy
import usdlc.Store
import static usdlc.Config.config

class DslActor extends GroovyActor {
	DslActor(String dsl) {
		languageScriptClass = config.dslClassPath.findResult { path ->
			try {
						Groovy.loadClass("${path.replaceAll('/', '.')}$dsl") ?:
						gse.loadScriptByName("${dsl}.groovy")
			} catch (ResourceException re) {
				null
			}
		}
		if (!languageScriptClass) exists = false
	}
	/**
	 * The Script sub-class created by the groovy compiler from script source - or null if there is no source.
	 */
	Class languageScriptClass

	void init() {
		super.init()
		def dslBinding = new UsdlcBinding(dslContext, context)
		Script languageScript = InvokerHelper.createScript(languageScriptClass, dslBinding)
		languageScript.run()
	}
	/**
	 * Keep a cache of previous instances - one per language - so we don't have to recompile. The cache includes
	 * instances that failed to find a script.
	 */
	static gse = new GroovyScriptEngine(config.dslPathUrls as URL[])
}
