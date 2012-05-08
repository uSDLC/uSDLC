package usdlc.drivers

import org.codehaus.groovy.runtime.InvokerHelper
import static usdlc.config.Config.config
import usdlc.Store

/**
 * Groovy script support
 */
class Groovy {
	static GroovyClassLoader gcl
	static {
		gcl = new GroovyClassLoader()
		config.srcPath.each { URL path -> gcl.addURL(path) }
	}
	/**
	 * GroovyClassLoader will load a class - recompiling from source if
	 * needed.
	 */
	static Class loadClass(String className) {
		try {
			return gcl.loadClass(className, true, false, true)
		} catch (e) {
			return null
		}
	}
	/**
	 * Given a path, create fully qualified class name - dropping .groovy
	 */
	static Class loadClass(String path, String className) {
		className = className.replaceFirst(~/\.[^\.]*$/, '')
		return loadClass("${path.replaceAll('/', '.')}$className")
	}
	/**
	 * load from a given classpath
	 */
	static void loadClass(String className, List paths) {
		paths.findResult { String path -> Groovy.loadClass(path, className) }
	}
	/**
	 * Run a previously loaded script.
	 */
	static void runClass(Class scriptClass, Binding binding = new Binding()) {
		InvokerHelper.createScript(scriptClass, binding).run()
	}
	/**
	 * Instantiate if you want to run Groovy scripts with a common binding.
	 */
	Groovy(Object[] binding) { baseBinding = binding }
	/**
	 * Run a groovy script or DSL. Provides methods for additional delegation,
	 * logging and script includes
	 */
	def run(Store script, binding = baseBinding) {
		def scriptClass = loadClass(script.parent, script.name) ?:
			loadScriptByName(script)
		if (scriptClass) {
			runClass(scriptClass, binding)
			return true
		}
		return false
	}

	def loadScriptByName(Store script) {
		try {return gse.loadScriptByName(script.pathFromWebBase)}
		catch(e) {return null}
	}
	/**
	 * Run a list of scripts
	 */
	def scripts(Object[] list) {
		def ran = 0
		list.flatten().each { if (run(Store.base(it))) ran++ }
		return ran
	}

	def gse = new GroovyScriptEngine(config.srcPath as URL[])
	Binding baseBinding
}
