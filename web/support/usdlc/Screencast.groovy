package usdlc

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import usdlc.actor.Actor
import usdlc.drivers.WebDriver
import static usdlc.config.Config.config

class Screencast {
	boolean client(cmd, params) {
		sleep(stepDelay)
		params = (params as List).flatten().collect {
			if (it) {
				def line = it as String
				def item = line.replaceAll(/'/, /\\'/).replaceAll('\n', /\\n/)
				"'$item'"
			} else {
				"''"
			}
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
			web.load("http://$host?$store.pathFromWebBase")
			web.waitFor(By.cssSelector('div.screencast')) {}
			client('keys', [config.screencast.keys])
			Actor.cache['screencastResponse'] =
				new ScreencastResponseActor(semaphore: semaphore)
		}
		session.screencastBase = store
		page(store, title, subtitle, synopsis)
	}

	def createPage(title, subtitle, synopsis) {
		sleep(stepDelay)
		def store = session.screencastPage.rebase("${title}/index.html")
		page(store, title, subtitle, synopsis)
	}

	def timeout(seconds) {
		semaphore.timeout = seconds
	}

	void check(selector, regex) { web.check(selector, regex) }
	void check(regex) { web.check(regex) }

	def web = null, session = null, stepDelay = 1

	void page(store, title, subtitle, synopsis) {
		session.screencastPage = store
		page = new Page(store)
		page.title = title
		page.subtitle = subtitle
		page.synopsis = synopsis
		page.save()
		script("usdlc.absolutePageContents('$store.pathFromWebBase')")
	}

	def getSections() {
		web.driver.findElements(By.className('section'))
	}

	def findElement(by, message) {
		checkElement web.findElement(by), message
	}

	def checkElement(element, message) {
		if (element) web.currentElement = element
		assert element, "No element with $message"
		element
	}

	def click(targets) {
		web.waitFor(targets) { element ->
			if (element.getAttribute('action') == 'page') {
				def href = element.getAttribute('href').
						replaceAll('http://[^/]*', '')
				session.screencastPage = Store.base(href)
				script("usdlc.relativePageContents('$href')")
			} else {
				element.click()
			}
		}
		sleep(stepDelay)
	}

	def findElementById(id) {
		findElement By.id(id), "an Id '$id'"
	}

	def codeId(String text) {
		findElement By.linkText(text), "a link '$text'"
		def linkId = web.currentElement.getAttribute('id')
		"${linkId}_inclusion"
	}

	def findSection(String regex) {
		checkElement(
				sections.find { it.text =~ regex },
				"Can't find section for '$regex'").getAttribute('id')
	}

	def getFocus() {
		findElement By.className('inFocus'), 'focus'
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
				}, 'a next section').getAttribute('id')
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

	Semaphore semaphore = new Semaphore(1200) // defaults to 20 minutes
	Page page
}

class ScreencastResponseActor extends Actor {
	void run(Store script) {
		semaphore.release()
	}

	Semaphore semaphore
}
