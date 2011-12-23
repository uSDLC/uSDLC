package usdlc

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
				rebase(coffeescript.pathFromWebBase + '.js')

		if (coffeescript.newer(javascript)) {
			javascript.write(compile(coffeescript).bytes)
		}
		javascript
	}
}
