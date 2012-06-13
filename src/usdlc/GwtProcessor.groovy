package usdlc

class GwtProcessor {
	static void update(store, unknownStatements) {
		def setup = store.rebase('Setup.coffee')
		if (!setup.exists()) {
			setup.text = "include 'Instrument'"
			addFooter(store, 'Setup')
		}
		def instrument = store.rebase('Instrument.coffee')
		if (!instrument.exists()) {
			instrument.text = "include 'Instrument'"
			addFooter(store, 'Instrument')
		}
		unknownStatements.each { statement ->
			instrument.file << "\ngwt /$statement/, () ->\n    throw 'Undefined'"
		}
	}

	static addFooter(store, name) {
		def page = new Page(store.rebase('index.html'))
		page.footer.select('a').last().before("<a class='usdlc', action='runnable', href='${name}.coffee'>$name</a> - ")
		page.forceSave()
	}
}
