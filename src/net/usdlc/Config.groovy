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
package net.usdlc

import java.lang.reflect.Method

/**
 * Wrapper for configSlurper so we can access multiple configuration files easily. You will still need to restart
 * the server to pick up a new config.
 *
 * Config.web.port
 * Config.web.defaultScriptEngine
 *
 * User: Paul Marrington
 * Date: 23/11/10
 * Time: 9:35 PM
 */

class Config {
	@Lazy static web = new ConfigSlurper().parse(new File("WEB-INF/web.groovy").toURL())

	@Lazy static def classPath = {
		def cp = []
		Config.web.srcPath.each {
			cp << it
		}
		Config.web.libPath.each {
			cp += Store.base(it).dirs(~/.*\.jar$/)
		}
		// Run through them all and add to the uSDLC classpath - so that compilers behave
		cp.each {
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", [URL.class] as Class[])
			method.accessible = true
			method.invoke(ClassLoader.systemClassLoader, [Store.base(it).toURL()] as Object[])
		}
		return cp += System.getProperty('java.class.path').split(';')
	}()
	@Lazy static def classPathString = Config.classPath.join(';')
}
