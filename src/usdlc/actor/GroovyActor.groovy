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
package usdlc.actor

import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack
import usdlc.Environment
import usdlc.Store
import static init.Config.config

/**
 * The Groovy actor calls the named groovy script
 *
 * User: Paul Marrington
 * Date: 14/01/11
 * Time: 7:26 PM
 */
class GroovyActor {
	public run(script) {
		binding = Environment.session().variables
		if (!binding.variables.containsKey('gse')) {
			bind(
					log: { System.err.println it },
					print: { binding.doc.text it },
					gse: new GroovyScriptEngine(config.classPath),
			)
		}
		def root = Store.base(script).parent
		def shell = new GroovyShell(binding)
		bind(
				include: {
					//runScript "$root/$it"
					shell.evaluate(Store.base("$root/$it").text())
				},
				template: { runScript "$root/rt/${it}.html.groovy" }
		).bind()
		runScript script
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
	UsdlcBinding binding
	/**
	 * Any sub-class can assign a delegate instance that is called if the class does not have a matching one. Used for DSL transparency of functionality. Typically set in bind()
	 */
	protected getDelegate() { binding.$delegate }

	protected setDelegate(delegate) { binding.$delegate = delegate }
	/**
	 * Enter Groovy land to build up and execute groovy code as a script.
	 * Could be recursive if a script calls include()
	 */
	def runScript(script) {
		binding.gse.run Store.base(script).absolutePath, binding
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
		Script.metaClass.propertyMissing = { String name ->
			delegate.binding.variables.$delegate."$name"
		}
	}
}

class UsdlcBinding extends Binding {
	public UsdlcBinding() { super() }

	public UsdlcBinding(Map variables) { super(variables) }

	public UsdlcBinding(String[] args) { super(args) }

	public setVariable = {String name, Object value ->
		def $delegate = delegate.binding.variables.$delegate
		if (!$delegate) { throw new MissingPropertyException("") }
		try {
			$delegate."$name" = value
		} catch (e) {
			super.setVariable(name, value)
		}
	}
}
