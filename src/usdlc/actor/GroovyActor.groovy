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
import static init.Config.config

/**
 * The Groovy actor calls the named groovy script
 *
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 7:26 PM
 */
class GroovyActor extends Actor {
	def init() {
		if (!context.gse) {
			GroovyScriptEngine gse = new GroovyScriptEngine(config.srcPath as URL[])
			context << [
					usdlcBinding: new UsdlcBinding(context, dslContext),
					log: { Log.err it },
					gse: gse,
					include: { String include -> gse.run(Store.base("$script.parent/$include").path, context.usdlcBinding) },
					finalisers: [],
					out: { out.println it },
			]
		}
	}
	/**
	 * Run a groovy script or DSL. Provides methods for additional delegation, logging and script includes
	 */
	void run() {
		init()
		context.gse.run script.path, context.usdlcBinding
	}

	static class UsdlcBinding extends Binding {
		def dslContext

		UsdlcBinding(binding, dslBinding) {
			super(binding as Map)
			dslContext = dslBinding
		}

		def getVariable(String name) {
			if (variables.containsKey(name))
				variables[name]
			else if (dslContext.containsKey(name)) {
				dslContext[name]
			} else {
				Log.err("No context '$name'")
			}
		}
	}
}
