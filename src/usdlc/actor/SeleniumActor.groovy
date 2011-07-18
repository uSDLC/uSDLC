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
package usdlc.actor

import com.thoughtworks.selenium.HttpCommandProcessor
import com.thoughtworks.selenium.SeleniumException
import org.openqa.selenium.server.SeleniumServer

class SeleniumActor extends GroovyActor {
	def dsl = [
			reset: { reset() },
			open: { selenium('open', it) },
			waitForPageToLoad: { selenium('waitForPageToLoad', it) },
			click: { selenium('click', it) },
			assertElementPresent: { selenium('assertElementPresent', it) },
	]
	/**
	 * Prepare to open a browser instance given the authority (base URL) and browser (firefox, chrome, etc)
	 * @param authority Full base url (as in http://localhost:9000)
	 * @param browser optional browser identifier (*firefox, *chrome, *ie or *htmlunit among others)
	 */
	static class Browser {
		HttpCommandProcessor commandProcessor
		String authority, browser
		long timeStarted
	}

	Browser getBrowser() { context['browser'] }

	void setBrowser(Browser to) { context['browser'] = to }

	def browse(String authority, browserName = '*firefox') {
		if (!browser) {
			long tooOld = System.currentTimeMillis() - 3600000 /* 1 hour */
			browsers.findAll { String key, Browser instance -> instance.timeStarted < tooOld }.
					each { String key, Browser instance -> browsers.remove(key) }

			String key = "$authority::$browserName::${exchange.request.cookies['session']}"
			if (!browsers.containsKey(key)) {
				browsers[key] = new Browser(authority: authority, browser: browser, commandProcessor: null)
			}
			browser = browsers[key]
			browser.timeStarted = System.currentTimeMillis()
		}
	}
	/**
	 * Pass everything else to Selenium for processing.
	 * @param command Selenium command
	 * @param args One argument or an array of arguments
	 * @return Whatever Selenium returns - OK or error message usually
	 */
	def selenium(String command, List args) {
		if (!browser) { throw new Error("No browser instance for Selenuium command $command") }
		browser.with {
			def params = [args].flatten() as String[]
			try {
				return commandProcessor.doCommand(command, params)
			} catch (SeleniumException se) {
				switch (se.message) {
					case ~/ERROR Server Exception:/:
						return reset(command, params)
					case ~/ERROR/:  // some sort of assert
						throw new Error("$command $params // $se.message", se)
					default:    // it is a function that returns a value
						return se.message
				}
			} catch (e) {
				reset()
				commandProcessor.doCommand(command, params)
			}
		}
	}

	private HttpCommandProcessor getCommandProcessor() {
		browser.with {
			if (!commandProcessor) {
				if (!server) {
					server = new SeleniumServer()
					server.boot()
					server.start()
				}
				commandProcessor = new HttpCommandProcessor('localhost', server.port, browser, authority);
				commandProcessor.start()
			}
			commandProcessor
		}
	}
	/**
	 * If there is no property it is probably a Selenium command
	 * @param command Property command synonym
	 * @return Whatever selenium provides
	 */
	def propertyMissing(String command) {
		methodMissing(command, [])
	}
	/**
	 * Reset the connection to the browser - closing the window.
	 */
	def reset() {
		if (browser) {
			browser.commandProcessor?.stop()
			browser.commandProcessor = null
		}
	}

	private static SeleniumServer server
	private static Map browsers = [:]
}
