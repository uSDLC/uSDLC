/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
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
package net.usdlc.actors

import net.usdlc.Config
import net.usdlc.Environment
import net.usdlc.Store
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack

/**
 * The Groovy actor calls the named groovy script
 *
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 7:26 PM
 */
class GroovyActor {
	GroovyActor() {
		binding = Environment.data()
		def root = Store.root(binding.script).parent.replaceAll('\\\\', '/')
		bind(
				print: { binding.doc.text it },
				gse: new GroovyScriptEngine(Config.classPath as String[]),
				include: { runScript "$root/$it" },
				template: { runScript "$root/rt/${it}.html.groovy" }
		).bind()
		runScript binding.script
		binding.doc.close()
	}
	/**
	 * Add to the binding that called scripts see.
	 * @param moreBinding more binding data
	 * @return actor for chaining
	 */
	protected bind(Map moreBinding) {
		binding.variables << moreBinding
		return this
	}
	/**
	 * Overridden to add actor specific binding.
	 * @return actor for chaining
	 */
	protected bind() { return this }
	/**
	 * Data that comes out as global scope to the groovy script as in /uSDLC/TechnicalArchitecture/Actors/Groovy
	 */
	Binding binding
	/**
	 * Any sub-class can assign a delegate instance that is called if the class does not have a matching one. Used for DSL transparency of functionality. Typically set in bind()
	 */
	protected getDelegate() { binding.$delegate }

	protected setDelegate(delegate) { binding.$delegate = delegate }
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

	static {
		Script.metaClass.methodMissing = { method, args ->
			def $delegate = delegate.binding.variables.$delegate
			if ($delegate) {
				return $delegate.invokeMethod(method, args)
			} else {
				throw new MissingMethodExceptionNoStack(method, delegate.class, args, false);
			}
		}
	}
}
