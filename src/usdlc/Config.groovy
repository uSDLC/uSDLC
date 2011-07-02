/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package usdlc

import java.lang.reflect.Method

/**
 * Wrapper for configSlurper so we can access multiple configuration files easily. You will still need to restart
 * the usdlc.server.servletengine.server to pick up a new config.
 *
 * Config.port
 * Config.defaultScriptEngine
 *
 * User: Paul Marrington
 * Date: 23/11/10
 * Time: 9:35 PM
 */

class Config {
	/**
	 * Called by user visible config class.
	 * @param baseDirectory
	 * @param args
	 * @return
	 */
	protected void init(String baseDirectory, String[] args) {
		this.baseDirectory = baseDirectory
		Dictionary.commandLine(args).each { key, value -> this[key] = value }
		classPath = buildClassPath()
		classPathString = classPath.join(';')
		tableVersions = loadTableVersions()
	}
	/**
	 * http://basedirectory/uSDLCpath
	 */
	String baseDirectory
	/*
	 Path to use for defining Java and Groovy files. Point to the source for static (unchanging) code files and the web directory for files that will change as part of the installation.
	 */
	String[] srcPath
	String[] libPath
	/**
	 * Build up the expected classpath from configuration details.
	 */
	String[] classPath;

	private String[] buildClassPath() {
		// Run through them all and add to the uSDLC classpath - so that compilers behave
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", [URL.class] as Class[])
		method.accessible = true

		def cp = []
		srcPath.each { cp << Store.base(it).path }

		libPath.each { path ->
			Store.base(path).dirs(~/.*\.jar$/).each { String fileName ->
				def params = [Store.base("$path/$fileName").url] as Object[]
				method.invoke(ClassLoader.systemClassLoader, params)
			}
		}
		return cp as String[]
	}
	/**
	 * Retrieve classpath string as expected by Java
	 */
	String classPathString
	/**
	 * Version data for all database tables associated with the project
	 */
	Properties tableVersions

	private Properties loadTableVersions() {
		// auto-generated by and when building application jar
		def properties = new Properties()
		def webInf = Store.base('WEB-INF')
		webInf.dirs(~/.*TableVersions.properties/) { String fileName ->
			webInf.withInputStream(fileName) { stream -> properties.load(stream) }
		}
		return properties
	}
}
