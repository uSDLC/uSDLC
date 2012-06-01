package usdlc.actor
import usdlc.Store

class GwtActor extends CoffeeActor {
	def preprocessor = { text ->
		def parsed = []
		text.eachLine {
			parsed.push("gwt.processor '${it.replaceAll(/'/,/\'/)}'")
		}
		return parsed.join('\n')
	}

	public void run(Store me) {
		super.run(compiler.javascript(me, preprocessor))
	}
}
