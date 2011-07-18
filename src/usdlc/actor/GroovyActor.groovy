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
	/**
	 * Run a groovy script or DSL. Provides methods for additional delegation, logging and script includes
	 */
	void run() {
		GroovyScriptEngine gse = context['gse']
		UsdlcBinding usdlcBinding = new UsdlcBinding(context)
		def root = exchange.store.parent
		if (!gse) {
			gse = new GroovyScriptEngine(config.classPath)
			context << [
					log: { System.err.println it },
					gse: gse,
					include: { String include -> gse.run(Store.base("$root/$include").absolutePath, usdlcBinding) }
			]
		}
		gse.run exchange.store.absolutePath, usdlcBinding
	}

	def dsl = [:]
	/**
	 * Some DSLs need to do special work to retrieve a method or data. They will over-ride this method.
	 */
	def delegate(name) { Log.err("No context '$name'") }

	class UsdlcBinding extends Binding {
		UsdlcBinding(Map binding) { super(binding) }

		Object getVariable(String name) {
			switch (name) {
				case variables: return variables[name]
				case dsl: return dsl[name]
				default: return delegate(name)
			}
		}
	}
}
