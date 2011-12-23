package usdlc.actor

import usdlc.Grape
import usdlc.Groovy
import static usdlc.config.Config.config

/**
 * Given a Groovy DSL (script), first try and load it using the class-loader
 * in case it resides in a jar file. The load it as a script - compiling it
 * if necessary. Note that a special DSL path is used.
 */
class DslActor extends GroovyActor {
	DslActor(String dsl) {
		languageScriptClass = config.dslClassPath.findResult { String path ->
			try {
				Groovy.loadClass(path, dsl) ?:
					gse.loadScriptByName("$path${dsl}.groovy")
			} catch (ResourceException re) {
				exists = false
				null
			}
		} as Class
	}
	/**
	 * The Script sub-class created by the groovy compiler from script
	 * source -
	 * or null if there is no source.
	 */
	Class languageScriptClass

	void init() {
		super.init()
		context << [
		        grab: { Map dependency -> Grape.grab(dependency) }
		]
		Groovy.run(languageScriptClass, new UsdlcBinding(dslContext, context))
	}
	/**
	 * Keep a cache of previous instances - one per language - so we don't
	 * have
	 * to recompile. The cache includes instances that failed to find a
	 * script.
	 */
	static gse = new GroovyScriptEngine(config.dslPathUrls as URL[])
}
