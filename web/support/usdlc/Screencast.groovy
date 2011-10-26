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
package usdlc

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import usdlc.actor.Actor
import static usdlc.Config.config

class Screencast {
	boolean client(cmd, params) {
		sleep(stepDelay)
		params = params.flatten().collect {
			it ? "'"+it.replaceAll(/'/,/\\'/).replaceAll("\n", /\\n/)+"'" : "''"
		}.join(',')
		script("usdlc.screencast.$cmd($params)")
		true
	}
	def waitForResponse() {
		semaphore.wait {}
	}
	def createScreencast(title, subtitle, synopsis) {
		def store = Store.tmp("${Store.camelCase(title)}/index.html")
		if (!web) {
			web = new WebDriver()
			def host = session.exchange.request.header.host
			web.load("http://$host?$store.path")
			web.waitFor(By.cssSelector('div.screencast')) {}
			client('keys', [config.screencast.keys])
			Actor.cache['usdlc/screencast/response'] =
					new ScreencastResponseActor(semaphore: semaphore)
		}
		page(store, title, subtitle, synopsis)
	}
	def createPage(title, subtitle, synopsis) {
		sleep(stepDelay)
		def store = session.screencastBase.rebase("${title}/index.html")
		page(store, title, subtitle, synopsis)
	}
	def timeout(seconds) {
		semaphore.timeout = seconds
	}
	void check(selector, regex) {
		web.waitFor(selector) { element ->
			currentElement = element
			assert element.text =~ regex
		}
	}
	void check(regex) {
		assert currentElement.text =~ regex
	}
	def web, currentElement, session, stepDelay = 1

	void page(store, title, subtitle, synopsis) {
		session.screencastBase = store
		page = new Page(store)
		page.title = title
		page.subtitle = subtitle
		page.synopsis = synopsis
		page.save()
		script("usdlc.absolutePageContents('$store.path')")
	}

	def getSections() {
		web.driver.findElements(By.className('section'))
	}

	def findElement(by, message) {
		checkElement web.findElement(by), message
	}

	def checkElement(element, message) {
		if (element) currentElement = element
		assert element, "No element with $message"
		element
	}

	def findElementById(id) {
		findElement By.id(id), "an Id '$id'"
	}

	def code(String text) {
		findElement By.linkText(text), "a link '$text'"
		def linkId = currentElement.getAttribute('id')
		findElementById "${linkId}_inclusion"
		sleep(stepDelay)
	}

	def findSection(String regex) {
		checkElement(
				sections.find { it.text =~ regex },
				"Can't find section for '$regex'")
	}

	def getFocus() {
		findElement By.className('inFocus'), 'focus'
	}

	void setFocus(section) {
		section.click()
		sleep(stepDelay)
	}

	def nextSection() {
		def focusId = focus.getAttribute('id')
		def lastId = ''
		checkElement(
				sections.find {
					if (lastId == focusId) {
						true
					} else {
						lastId = it.getAttribute('id')
						false
					}
				}, 'a next section')
	}
	
	void sleep(Double ms) {
		sleep(ms as long)
	}

	void async(String script, Object... args) {
		((JavascriptExecutor) web.driver).executeAsyncScript(script, args)
	}

	void script(String script, Object... args) {
		((JavascriptExecutor) web.driver).executeScript(script, args)
	}
	Semaphore semaphore = new Semaphore(120) // defaults to 2 minutes
	Page page
}

class ScreencastResponseActor extends Actor {
	void run(Store script) {
		semaphore.release()
	}
	Semaphore semaphore
}
