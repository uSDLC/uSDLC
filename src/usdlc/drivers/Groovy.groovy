package usdlc.drivers

import org.codehaus.groovy.runtime.InvokerHelper
import static usdlc.config.Config.config

/**
 * Groovy script support
 */
@SuppressWarnings('GroovyAccessibility')
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
	static void run(Class scriptClass, Binding binding = new Binding()) {
		InvokerHelper.createScript(scriptClass, binding).run()
	}
}
