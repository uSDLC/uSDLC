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

import groovy.lang.Binding

import java.util.Map

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation

import usdlc.CSV
import usdlc.Groovy;
import usdlc.Log
import usdlc.Store
import static usdlc.Config.config

/**
 * The Groovy actor calls the named groovy script
 *
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 7:26 PM
 */
class GroovyActor extends Actor {
	void init() {
		if (!context.gse) {
			GroovyScriptEngine gse = new GroovyScriptEngine(config.srcPath as URL[])
			UsdlcBinding usdlcBinding = new UsdlcBinding(context, dslContext)
			context << [
						usdlcBinding: usdlcBinding,
						log: { Log.err it },
						gse: gse,
						include: { String include ->
							def path = Store.base("$script.parent/$include").path
							gse.run(path, usdlcBinding)
						},
						out: { out.println it },
						config: config,
						dsl: new DslInclusions(binding: usdlcBinding)
					]
		}
	}
	/**
	 * Run a groovy script or DSL. Provides methods for additional delegation, 
	 * logging and script includes
	 */
	void run(Store script) {
		def scriptClass = Groovy.loadClass(script.parent, script.name) ?:
				context.gse.loadScriptByName(script.path)
		Groovy.run(scriptClass, context.usdlcBinding)
	}
}
