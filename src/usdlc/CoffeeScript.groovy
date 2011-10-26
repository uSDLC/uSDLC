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
	def bare, session
	/**
	 * Constructor loads and compiles the coffee-script compiler
	 */
	CoffeeScript(boolean bare = false) {
		this.bare = bare
		// Without this, Rhino hits a 64K byte-code limit and fails
		javascript.optimise = false
		javascript.run(Store.base('lib/coffeescript.js'))
	}
	/**
	 * Pass in a coffee-script file and the compiler will do it's magic
	 */
	String compile(String coffeeScriptSource) {
		String options = bare ? "{bare: true}" : "{}"
		javascript.run("CoffeeScript.compile(coffeeScript, $options);",
				[coffeeScript:coffeeScriptSource, newScope:true])
	}
	private javascript = new JavaScript()
	/**
	 * Retrieve a reference to the javascript file created from a cs compile
	 */
	Store javascript(Store coffeescript) {
		Store javascript = Store.base("store/coffeescript/base").
				rebase(coffeescript.path + '.js')

		if (coffeescript.newer(javascript)) {
			try {
				javascript.write(compile(coffeescript.text))
			} catch (exception) {
				throw new RuntimeException(
				"Error in $coffeescript.path ($exception.message)", exception)
			}
		}
		javascript
	}
}
