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

import usdlc.Log
import usdlc.Store
import usdlc.drivers.Groovy
import static usdlc.config.Config.config

/**
 * The Groovy actor calls the named groovy script
 *
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 7:26 PM
 */
class GroovyActor extends Actor {
	void init() {
		if (!context.initialised) {
			def gse = context.session.gse {
				new GroovyScriptEngine(config.srcPath as URL[])
			}
			UsdlcBinding usdlcBinding = new UsdlcBinding(context, dslContext)
			def dsl = new DslInclusions(binding: usdlcBinding)
			context << [
					initialised: true,
					script: script,
					usdlcBinding: usdlcBinding,
					log: { String message -> Log.err message },
					gse: gse,
					include: dsl.&include,
					write: { String text -> out.print text },
					config: config,
					dsl: dsl,
					compile: { String scriptName ->
						gse.loadScriptByName script.
								rebase(scriptName).pathFromWebBase
					},
			]
		}
	}
	/**
	 * Run a groovy script or DSL. Provides methods for additional delegation,
	 * logging and script includes
	 */
	void run(Store script) {
		def scriptClass = Groovy.loadClass(script.parent, script.name) ?:
			context.gse.loadScriptByName(script.pathFromWebBase)
		Groovy.run(scriptClass, context.usdlcBinding)
	}
}
