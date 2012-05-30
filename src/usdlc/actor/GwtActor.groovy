package usdlc.actor

class GwtActor extends CoffeeActor {
	void init() {
		super.init()
		compiler.preprocessor = { text ->
			def parsed = []
			text.eachLine {
				parsed.push("gwt.processor '$it'")
			}
			return parsed.join('\n')
		}
	}
}
