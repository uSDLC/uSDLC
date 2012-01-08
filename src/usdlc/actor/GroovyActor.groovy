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
