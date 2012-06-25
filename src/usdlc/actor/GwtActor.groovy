package usdlc.actor

class GwtActor extends CoffeeActor {
	GwtActor() {
		preprocessor('gwt') { text ->
			def parsed = ['gwt.setup()']
			text.eachLine {
				if (it[0] != '#') {
					parsed.push("gwt.processor '${it.replaceAll(/'/,/\'/)}'")
				}
			}
			parsed.push('gwt.cleanup()')
			return parsed.join('\n')
		}
	}
}
