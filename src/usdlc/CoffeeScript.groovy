package usdlc

import com.google.common.base.Charsets
import com.google.common.io.InputSupplier
import com.google.common.io.Resources
import org.mozilla.javascript.Context
import org.mozilla.javascript.JavaScriptException
import org.mozilla.javascript.NativeJavaPackage
import org.mozilla.javascript.Scriptable

import java.io.IOException
import java.io.InputStreamReader

class CoffeeScript {
	def bare
	/**
	 * Constructor loads and compiles the coffee-script compiler
	 */
	CoffeeScript(boolean bare = false) {
		this.bare = bare
		javascript.optimise = false // Without this, Rhino hits a 64K byte-code limit and fails
		javascript.run(Store.base('lib/coffee-script.js'))
	}
	/**
	 * Pass in a coffee-script file and the coffee-script compiler will do it's magic
	 */
	String compile(String coffeeScriptSource) {
		String options = bare ? "{bare: true}" : "{}"
		javascript.run("CoffeeScript.compile(coffeeScript, $options);",
				[coffeeScript:coffeeScriptSource, newScope:true])
	}
	private javascript = new JavaScript()
	/**
	 * Retrieve a reference to the javascript file created from a coffee-script compile
	 */
	Store javascript(Store coffeescript) {
		Store javascript = Store.base("store/coffee-script/base").rebase(coffeescript.path + '.js')

		if (coffeescript.newer(javascript)) {
			javascript.write(compile(coffeescript.text()))
		}
		javascript
	}

	static class Delegate {
		def commands = [:], defaults = []
		Exchange exchange
		/**
		 * Add another delegate if needed.
		 */
		void add(Class newDelegate) {
			Delegate instance = exchange.request.session.instance(newDelegate)
			instance.exchange = exchange
			commands += instance.commands
			defaults << instance.&command
			instance.init()
		}
		void init() {
		}
		/**
		 * Domain Specific Command definition
		 */
		def dsc(cmd, params) {
			if (cmd in commands) {
				commands[cmd](*params)
			} else {
				defaults.find { it(cmd, params) }
			}
		}
		/**
		 * Default command for general execution if command is not in supplied list. Used to
		 * separate server from client implementations.
		 */
		boolean command(cmd, params) {
			false
		}
	}
}
