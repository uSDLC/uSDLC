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

/**
 * Given a Groovy DSL (script), first try and load it using the class-loader
 * in case it resides in a jar file. The load it as a script - compiling it
 * if necessary. Note that a special DSL path is used.
 */
class DslActor extends GroovyActor {
	DslActor(String dsl) {
		try {
			languageScriptClass = config.dslClassPath.findResult {
				Groovy.loadClass(it, dsl) ?:
						gse.loadScriptByName("$it${dsl}.groovy")
			}
		} catch (ResourceException re) {
			exists = false
		}
	}
	/**
	 * The Script sub-class created by the groovy compiler from script source - 
	 * or null if there is no source.
	 */
	Class languageScriptClass

	void init() {
		super.init()
		Groovy.run(languageScriptClass, new UsdlcBinding(dslContext, context))
	}
	/**
	 * Keep a cache of previous instances - one per language - so we don't have 
	 * to recompile. The cache includes instances that failed to find a script.
	 */
	static gse = new GroovyScriptEngine(config.dslPathUrls as URL[])
}
