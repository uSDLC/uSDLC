package usdlc.drivers

import usdlc.Store

class CoffeeScript {
	def bare, session
	/**
	 * We can preprocess the text before compiling. Used to extend the
	 * CoffeeActor. See GwtActor for an example.
	 */
	def preprocessor = { text -> text }
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
	String compile(Store coffeescript) {
		try {
			compile(coffeescript.text)
		} catch (exception) {
			throw new RuntimeException(
			"Error in $coffeescript.pathFromWebBase ($exception.message)",
					exception)
		}
	}
	/**
	 * Pass in a coffee-script file and the compiler will do it's magic
	 */
	String compile(String coffeeScriptSource) {
		coffeeScriptSource = preprocessor coffeeScriptSource
		String options = bare ? '{bare: true}' : '{}'
		javascript.run("CoffeeScript.compile(coffeeScript, $options);",
				[coffeeScript:coffeeScriptSource, newScope:true])
	}
	private javascript = new JavaScript()
	/**
	 * Retrieve a reference to the javascript file created from a cs compile
	 */
	Store javascript(Store coffeescript) {
		Store javascript = Store.base('~home/.store/coffeescript/base',
				coffeescript.project).rebase(
					coffeescript.fromProjectHome + '.js')

		if (coffeescript.newer(javascript)) {
			javascript.write(compile(coffeescript).bytes)
		}
		javascript
	}
}
