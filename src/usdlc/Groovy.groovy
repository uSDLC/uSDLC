package usdlc

import static Config.config

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
		} catch (ClassNotFoundException cnfe) {
			null
		}
	}
}
