package usdlc

import com.google.common.base.Charsets
import com.google.common.io.InputSupplier
import com.google.common.io.Resources
import org.mozilla.javascript.Context
import org.mozilla.javascript.JavaScriptException
import org.mozilla.javascript.Scriptable

import java.io.IOException
import java.io.InputStreamReader

class CoffeeScript {

	private Scriptable globalScope
	private boolean bare

	CoffeeScript(boolean bare = false) {
		this.bare = bare
		Context context = Context.enter()
		Reader reader = null
		try {
			context.setOptimizationLevel(-1) // Without this, Rhino hits a 64K byte-code limit and fails
			globalScope = context.initStandardObjects()
			def inputStream = Store.base('lib/coffee-script.js').file.newInputStream()
			reader = new InputStreamReader(inputStream, "UTF-8");
			context.evaluateReader(globalScope, reader, "coffee-script.js", 0, null)
		} finally {
			reader?.close()
			Context.exit()
		}
	}
	String compile(String coffeeScriptSource) {
		Context context = Context.enter()
		try {
			Scriptable compileScope = context.newObject(globalScope)
			compileScope.parentScope = globalScope
			compileScope.put("coffeeScript", compileScope, coffeeScriptSource)
			String options = bare ? "{bare: true}" : "{}"

			return (String) context.evaluateString(compileScope,
			"CoffeeScript.compile(coffeeScript, $options);", "source", 0, null)
		} finally {
			Context.exit()
		}
	}
	Store javascript(Store coffeescript) {
		Store javascript = Store.base("store/coffee-script/base").rebase(coffeescript.path + '.js')

		if (coffeescript.newer(javascript)) {
			javascript.write(compile(coffeescript.text()))
		}
		javascript
	}
}