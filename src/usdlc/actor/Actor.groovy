package usdlc.actor

import groovy.transform.AutoClone
import usdlc.Exchange
import usdlc.Groovy
import usdlc.Store
import static usdlc.config.Config.config

@AutoClone class Actor {
	/** variables to pass between scripts as globals        */
	def context = [:]
	def dslContext = [:]
	/** Http Exchange data - including request and response       */
	Exchange exchange
	/** Convenience to write to the response/browser       */
	PrintStream out
	/**
	 * Set to false if you want the actor to be ignored
	 */
	boolean exists = true
	/**
	 * A list of scripts used to build up the DSL
	 */
	def backingScripts = []
	/**
	 * Implement by concrete classes
	 */
	void run(Store script) {
	}
	/**
	 * Initialisation before running backing or target scripts
	 */
	void init() {
	}
	/** Actors run with a known binding use by all in the session      */
	void run(Map binding) {
		context = binding
		exchange = context.exchange
		out = exchange?.response?.out
		context.session = exchange?.request?.session ?: [:]
		context.getters = [:]
		context.setters = [:]
		if (!dslContext.getters) dslContext.getters = [:]
		if (!dslContext.setters) dslContext.setters = [:]
		init()
		backingScripts.each { Store backingScript -> run(backingScript) }
		if (script && !dslContext.dataSource) run(script)
	}

	static internalExceptions = ~/\.groovy\.|^groovy\.|\.java\.*/
	/**
	 * Called to see if a URL refers to an actor/dsl. It creates an
	 * instance of
	 * the class. Null is returned if no actor class or dsl script exists.
	 */
	static Actor load(Store store) {
		String language = store.parts.ext
		if (! language) return null
		if (!cache[language]) {
			def data = [scripts: [], groovyDSL: '', baseLanguage: '']
			retrieveDefinitions(language, data)
			if (data.groovyDSL) {
				cache[language] = data.groovyDSL
			} else {
				def actorClass = Groovy.loadClass(
						"usdlc.actor.${data.baseLanguage.capitalize()}Actor")
				cache[language] = actorClass ?
					actorClass.newInstance() :
					new DslActor("${data.baseLanguage.toLowerCase()}DSL")
			}
			cache[language].backingScripts = data.scripts
		}
		if (!cache[language].exists) return null
		def clone = cache[language].clone()
		clone.script = store
		clone as Actor
	}

	private static retrieveDefinitions(String language, Map data) {
		def dsl = "${language.toLowerCase()}DSL"
		config.dslSourcePath.each { String path ->
			def base = Store.base(path)
			base.dirs(~/${dsl}\..*/) { Store store ->
				String parentLanguage = store.parts.ext.toLowerCase()
				def parentDSL = "${parentLanguage}DSL"
				switch (parentLanguage) {
					case language:
						data.scripts << store
						break
					case 'groovy':
						data.groovyDSL = new DslActor(parentDSL)
						break
					default:
						retrieveDefinitions(parentLanguage, data)
						data.scripts << store
						break
				}
			}
		}
		if (!data.baseLanguage) data.baseLanguage = language
	}
	/**
	 * The script we want to run
	 */
	Store script
	/**
	 * Keep a cache of previous instances - one per language - so we don't
	 * have to recompile. The cache includes
	 * instances that failed to find a script.
	 */
	static cache = [:]
}
