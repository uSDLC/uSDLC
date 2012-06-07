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
			def groovy = context.session.groovy Groovy
			UsdlcBinding usdlcBinding = new UsdlcBinding(context, dslContext)
			def dsl = new DslInclusions(binding: usdlcBinding)
			context << [
					initialised: true,
					script: script,
					usdlcBinding: usdlcBinding,
					log: { String message -> Log.err message },
					gse: groovy.gse,
					groovy: groovy,
					include: dsl.&include,
					write: { text -> out.print text },
					config: config,
					dsl: dsl,
					compile: { String scriptName ->
						def toCompile = script.rebase(scriptName).path
						groovy.gse.loadScriptByName toCompile
					},
			]
		}
	}
	/**
	 * Run a groovy script or DSL. Provides methods for additional delegation,
	 * logging and script includes
	 */
	void run(Store script) {
		if (!context.groovy.run(script, context.usdlcBinding)) {
			throw context.groovy.lastError
		}
	}
}
