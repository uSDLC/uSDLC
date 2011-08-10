/*
 * Copyright 2011 the Authors for http://usdlc.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package usdlc.dsl

geb = session.instance usdlc.Geb
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
