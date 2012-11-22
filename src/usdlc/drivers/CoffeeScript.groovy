package usdlc.drivers

import usdlc.Store

class CoffeeScript {
	def bare, session
	/**
	 * Constructor loads and compiles the coffee-script compiler
	 */
	CoffeeScript(boolean bare = false) {
		this.bare = bare
		// Without this, Rhino hits a 64K byte-code limit and fails
		javascript.optimise = false
		javascript.run(Store.base('usdlc/lib/coffeescript.js'))
	}
	/**
	 * Pass in a coffee-script file and the compiler will do it's magic
	 */
	String compile(Store coffeescript, preprocessor = {text->text}) {
		compile(preprocessor(coffeescript.text))
	}
	/**
	 * Pass in a coffee-script file and the compiler will do it's magic
	 */
	String compile(String coffeeScriptSource) {
		String options = bare ? '{bare: true}' : '{}'
		javascript.run("CoffeeScript.compile(coffeeScript, $options);",
				[coffeeScript:coffeeScriptSource, newScope:true])
	}
	private javascript = new JavaScript()
	/**
	 * Retrieve a reference to the javascript file created from a cs compile
	 */
	Store javascript(Store coffeescript, preprocessor = {text->text}) {
		Store javascript = outputStore(coffeescript)

		if (coffeescript.newer(javascript)) {
			javascript.write(compile(coffeescript, preprocessor).bytes)
		}
		javascript
	}

	Store javascript(Collection coffeescripts) {
		Store javascript = outputStore(coffeescripts[0])

		def recompile = false
		for (coffeescript in coffeescripts) {
			if (coffeescript.newer(javascript)) {
				recompile = true
				break
			}
		}
		if (recompile) {
			javascript.delete();
			for (coffeescript in coffeescripts) {
				javascript.append(compile(coffeescript).bytes)
			}
		}
		javascript
	}

	private outputStore(coffeescript) {
		return Store.base('~home/.store/coffeescript/base',
				coffeescript.project).rebase(
					coffeescript.fromProjectHome + '.js')
	}
}
