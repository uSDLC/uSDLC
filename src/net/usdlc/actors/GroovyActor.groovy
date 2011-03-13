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
package net.usdlc.actors

import net.usdlc.Browser
import net.usdlc.Config
import net.usdlc.Environment
import net.usdlc.Store

/**
 * The Groovy actor calls the named groovy script
 *
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 7:26 PM
 */
class GroovyActor {
	static run(script) {
		def actor = new GroovyActor()
		def root = Store.root(script).parent.replaceAll('\\\\', '/')
		def browser = new Browser()
		def print = { browser.text it }
		actor.binding = Environment.data() + [
				browser: browser,
				doc: browser.html,
				print: print,
				gse: new GroovyScriptEngine(Config.classPath as String[]),
				include: { actor.runScript "$root/$it" },
				template: { actor.runScript "$root/rt/${it}.html.groovy" }
		]
		actor.runScript(script)
		browser.close()
	}
	/**
	 * Data that comes out as global scope to the groovy script as in /uSDLC/TechnicalArchitecture/Actors/Groovy
	 */
	Binding binding
	/**
	 * Could be recursive if a script calls include()
	 * @param scriptName Path and name of script relative to uSDLC root.
	 */
	def runScript(scriptName) {
		def script = scriptName
		// Drop the leading slash - always part of the URL, but we don't want to go from the root of the drive.
		if (script[0] == '/') { script = script[1..-1] }
		// Enter Groovy land to build up and execute groovy code as a script.
		binding.gse.run script, binding
	}
}
