/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
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
package usdlc.actors

import com.thoughtworks.selenium.HttpCommandProcessor
import com.thoughtworks.selenium.SeleniumException
import org.openqa.selenium.server.SeleniumServer
import usdlc.Environment

class SeleniumActor extends GroovyActor {
	def bind() {
		ensure.selenium = SeleniumProcessor
		delegate = binding.selenium
		return this
	}
}

class SeleniumProcessor {
	def env = Environment.data()
	static class Browser {
		HttpCommandProcessor commandProcessor
		String authority, browser
		long timeStarted = System.currentTimeMillis()
	}
	/**
	 * Prepare to open a browser instance given the authority (base URL) and browser (firefox, chrome, etc)
	 * @param authority Full base url (as in http://localhost:9000)
	 * @param browser optional browser identifier (*firefox, *chrome, *ie or *htmlunit among others)
	 */
	def browse(String authority, browser = '*firefox') {
		if (!env?.browser) {
			long yesterday = System.currentTimeMillis() - 86400000
			def kill = []
			browsers.each { key, instance -> if (instance.timeStarted < yesterday) { kill << key } }
			kill.each { browsers.remove(it) }

			String key = "$authority::$browser::$env.query.session"
			if (!browsers.containsKey(key)) {
				browsers[key] = new Browser(authority: authority, browser: browser, commandProcessor: null)
			}
			env.browser = browsers[key]
		}
	}
	/**
	 * Pass everything else to Selenium for processing.
	 * @param command Selenium command
	 * @param args One argument or an array of arguments
	 * @return Whatever Selenium returns - OK or error message usually
	 */
	def methodMissing(String command, args) {
		if (!env?.browser) { project.Selenium.browse() }
		env.browser.with {
			if (!commandProcessor) { commandProcessor() }
			def params = [args].flatten() as String[]
			try {
				return commandProcessor.doCommand(command, params)
			} catch (SeleniumException se) {
				switch (se.message) {
					case ~/ERROR Server Exception:/:
						return reset(command, params)
					case ~/ERROR/:  // some sort of assert
						throw new Error("$command $params // $se.message", se)
						break
					default:    // it is a function that returns a value
						return se.message
				}
			} catch (e) {
				return reset(command, params)
			}
		}
	}

	private reset(command, params) {
		reset()
		return commandProcessor().doCommand(command, params)
	}

	private commandProcessor() {
		env.browser.with {
			if (!commandProcessor) {
				if (!server) {
					server = new SeleniumServer()
					server.boot()
					server.start()
				}
				commandProcessor = new HttpCommandProcessor('localhost', server.port, browser, authority);
				commandProcessor.start()
			}
		}
	}
	/**
	 * If there is no property it is probably a Selenium command
	 * @param command Property command synonym
	 * @return Whatever selenium provides
	 */
	def propertyMissing(String command) {
		return methodMissing(command, [])
	}
	/**
	 * Reset the connection to the browser - closing the window.
	 */
	def reset() {
		if (env?.browser) {
			env.browser?.commandProcessor?.stop()
			env.browser.commandProcessor = null
		}
	}

	private static SeleniumServer server
	private static browsers = [:]
}
