package usdlc.config

import usdlc.Dictionary
import usdlc.Store
import usdlc.actor.GroovyActor

import java.lang.reflect.Method
import java.util.jar.Manifest

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
		config = parseOptions('base')
		config.projects = [:]
		['webDriverConfig', 'languageConfig'].each { String scriptName ->
			config.putAll(parseOptions(scriptName))
		}
		config.baseDirectory = baseDirectory
		config.putAll(Dictionary.commandLine(argList))
		// add home to source path so we can go app.blah
		config.srcPath << new File(config.home).absolutePath
		buildClassPath()
		config.classPathString = config.srcPath.join(';')
		runStartupScripts()
		loaded = true
	}

	private static Map parseOptions(String scriptName) {
		parse("/support/usdlc/config/${scriptName}.groovy")
	}

	private static Map parse(String scriptPath) {
		def file = new File(baseDir, scriptPath)
		if (!file.exists()) return [:]
		def url = file.toURI().toURL()
		return slurper.parse(url)
	}
	/**
	 * Project specific data is collected the first time a
	 * project is accessed - by saving the bindings from running a script
	 * 'projectDir/usdlc/Config.groovy'. You can get the project data here
	 * or from any Store object with 'pd = store.project'.
	 *
	 * pd.name  Project name
	 * pd.home project home
	 */
	static Map project(String name, parent = usdlcProject) {
		if (!config.projects[name]) {
			parent = parent ?: usdlcProject
			if (parent && parent.path[name]) {
				return parent // so ~home points to project home
			}
			def home = "$config.home/$name"
			if (Store.absolute(home).exists()) {
				if (name.indexOf('_') != -1) {
					name = name.replaceAll('_', ' ')
				} else {
					name = Store.decamel(name.capitalize())
				}
				return project(name, home)
			}
			return project('none', config.home)
		}
		return config.projects[name]
	}
	static project(name, home) {
		def configFile = "$home/usdlc/Config.groovy"
		def pc = configFile ? parse(configFile) : [:]
		pc.path = pc.path ?: [:]
		pc.path.home = pc.home = home
		pc.name = name
		return config.projects[name] = pc
	}
	static usdlcProject
	static ConfigSlurper slurper
	static String baseDir

	static private buildClassPath() {
		// Add to the uSDLC classpath - so that compilers behave
		Method method = URLClassLoader.
				getDeclaredMethod('addURL', [URL] as Class[])
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

		System.getProperty('java.class.path').
				split(/${System.getProperty('java.path.separator')}/).each {
			config.classPath << new File(it).toURI().toURL()
		}
	}

	static private toURL(String path) {
		(path.indexOf(':') > 1) ? new URL(path) : Store.base(path).url
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
