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
package usdlc.config

import java.lang.reflect.Method
import java.util.jar.Manifest
import usdlc.Dictionary
import usdlc.Store
import usdlc.actor.GroovyActor

/**
 * Wrapper for configSlurper so we can access multiple configuration files
 * easily. You will still need to restart the usdlc.server.servletengine.server
 * to pick up a new config.
 */

class Config {
	public static Map config
	static loaded
	/**
	 * Must be called early on by the server main program to initialise the
	 * configuration.
	 */
	static load(String environment, String baseDirectory, argList) {
		baseDir = baseDirectory
		slurper = new ConfigSlurper(environment)
		config = parse('base')
		['webDriver', 'languages'].each { String scriptName ->
			config.merge(parse(scriptName))
		}
		config.baseDirectory = baseDirectory
		Dictionary.commandLine(argList).each { String k,
				String v ->
			config[k] = v
		}
		buildClassPath()
		config.classPathString = config.srcPath.join(';')
		config.tableVersions = loadTableVersions()
		runStartupScripts()
		loaded = true
	}

	static Map parse(String scriptName) {
		def url = new File(baseDir,
				"/Options/Configuration/${scriptName}.groovy").toURI().toURL()
		slurper.parse(url)
	}

	static ConfigSlurper slurper
	static String baseDir

	static private buildClassPath() {
		// Add to the uSDLC classpath - so that compilers behave
		Method method = URLClassLoader.class.
				getDeclaredMethod('addURL', [URL.class] as Class[])
		method.accessible = true

		def systemClassLoader = ClassLoader.systemClassLoader
		config.classPath = []
		config.libPath.each { String path ->
			Store.base(path).dirs(~/.*\.jar/) { Store store ->
				method.invoke(systemClassLoader, [store.url] as Object[])
				config.classPath << store.url
			}
		}
		config.srcPath = config.srcPath.collect { String path -> toURL(path) }
		config.srcPath.each { URL url -> config.classPath << url }
		//config.dslClassPath = config.dslClassPath?.collect { toURL(it) }
		// ?: []

		System.getProperty("java.class.path").
				split(/${System.getProperty("java.path.separator")}/).each {
			config.classPath << new File(it).toURI().toURL()
		}
	}

	static private toURL(String path) {
		(path.indexOf(':') > 1) ? new URL(path) : Store.base(path).url
	}

	private static Properties loadTableVersions() {
		// auto-generated by and when building application jar
		def properties = new Properties()
		Store.base('rt').dirs(~/.*TableVersions.properties/) { Store store ->
			store.withInputStream() { stream -> properties.load(stream) }
		}
		properties
	}

	static private runStartupScripts() {
		def actor = new GroovyActor()
		actor.backingScripts = config.startupScripts.collect { String name ->
			Store.base(name)
		}
		actor.run([:])
	}

	@Lazy static manifest = {
		def attributes = null
		try {
			Store store = Store.base('../META-INF/MANIFEST.MF')
			Manifest mf = new Manifest(store.url.openStream());
			attributes = mf.mainAttributes
		} catch (Exception e) { }
		attributes
	}()
	@Lazy static version = {
		Config.manifest?.getValue('Specification-Version') ?: 'Development'
	}()
}
