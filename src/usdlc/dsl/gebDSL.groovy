package usdlc.dsl

import usdlc.drivers.Geb

geb = session.instance Geb
browse = { pageClass ->
	browser = geb.browse(pageClass, authority)
	this.$ = geb.browser.&$
}
driver = { driverList -> geb.driver(driverList) }
reset = { -> geb.reset() }

waitFor = { condition -> browser.waitFor(condition) }

getters.title = { geb.browser.title }

startsWith = { text -> geb.browser.startsWith(text) }
iStartsWith = { text -> geb.browser.iStartsWith(text) }
endsWith = { text -> geb.browser.endsWith(text) }
iEndsWith = { text -> geb.browser.iEndsWith(text) }
contains = { pattern -> geb.browser.contains(text) }
iContains = { pattern -> geb.browser.iContains(text) }
containsWord = { text -> geb.browser.containsWord(text) }
iContainsWord = { text -> geb.browser.iContainsWord(text) }
