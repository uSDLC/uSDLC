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
import static init.Config.config

/**
 * User: paul
 * Date: 24/07/11
 * Time: 5:50 PM
 */
class DslActor extends GroovyActor {
	Class languageScriptClass
	String language
	/**
	 * Specially created in Actor as a DSL definition script. Used if there is no actor.
	 */
	DslActor(String language) {
		this.language = language
		GroovyScriptEngine gse = new GroovyScriptEngine(config.dslPath as URL[])
		languageScriptClass = gse.loadScriptByName("${language}.groovy")
	}
	/**
	 * Cloning constructor to making an operational instance.
	 */
	private DslActor(DslActor clone) { languageScriptClass = clone.languageScriptClass }
	/**
	 * Called to create an instance to run - creates a clone.
	 * @return
	 */
	def newInstance() { new DslActor(this) }
	/**
	 * Here lies the real work. Instantiate and run the dsl definition script, then the script to run. Note that
	 * they both used each other contexts.
	 */
	void run() {
		init()
		Script languageScript = InvokerHelper.createScript(languageScriptClass, new UsdlcBinding(dslContext, context))
		languageScript.run()
		context.gse.run script.path, context.usdlcBinding // then we run the script
	}
}
