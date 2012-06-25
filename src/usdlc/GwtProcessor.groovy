package usdlc

class GwtProcessor {
	static boolean update(store, unknownStatements) {
		def setup = store.rebase('Setup.coffee')
		def page = null
		if (!setup.exists()) {
			setup.text = "include 'Instrument'"
			page = new Page(store.rebase('index.html'))
			addFooter(page, 'Setup')
		}
		def instrument = store.rebase('Instrument.coffee')
		if (!instrument.exists()) {
			instrument.text = "include 'Instrument'"
			page = page ?: new Page(store.rebase('index.html'))
			addFooter(page, 'Instrument')
			page.forceSave()
			return true
		}
		unknownStatements.each { statement -> instrument.file <<
			"\ngwt /$statement/, (all) ->\n    throw 'Undefined'"
		}
		page?.forceSave()
		return false
	}

	static addFooter(page, name) {
		page.footer.select('a').last().before("<a class='usdlc', action='runnable', href='${name}.coffee'>$name</a> - ")
	}
}
