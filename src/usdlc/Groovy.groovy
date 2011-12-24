package usdlc

import org.codehaus.groovy.runtime.InvokerHelper
import static usdlc.config.Config.config

/**
 * Groovy script support
 */
class Groovy {
	static GroovyClassLoader gcl
	static {
		gcl = new GroovyClassLoader()
		config.srcPath.each { gcl.addURL(it) }
	}
	/**
	 * GroovyClassLoader will load a class - recompiling from source if needed.
	 */
	static loadClass(String className) {
		try {
			gcl.loadClass(className, true, false, true)
		} catch (Throwable cnfe) {
			null
		}
	}
	/**
	 * Given a path, turn it into a fully qualified class name - dropping .groovy
	 */
	static loadClass(String path, String className) {
		className = className.replaceFirst(~/\.[^\.]*$/, '')
		loadClass("${path.replaceAll('/', '.')}$className")
	}
	/**
	 * load from a given classpath
	 */
	static loadClass(String className, List path) {
		path.findResult { Groovy.loadClass(it, className) }
	}
	/**
	 * Run a previously loaded script.
	 */
	static void run(Class scriptClass, binding = new Binding()) {
		InvokerHelper.createScript(scriptClass, binding).run()
	}
}
