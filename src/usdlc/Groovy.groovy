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
package usdlc

import static Config.config

import org.codehaus.groovy.runtime.InvokerHelper;
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
		} catch (ClassNotFoundException cnfe) {
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
	static void run(scriptClass, binding) {
		InvokerHelper.createScript(scriptClass, binding).run()
	}
}
