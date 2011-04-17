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
package net.usdlc.actors

import com.thoughtworks.selenium.CommandProcessor
import com.thoughtworks.selenium.HttpCommandProcessor
import org.openqa.selenium.server.SeleniumServer

class SeleniumActor extends GroovyActor {
	def bind() {
		delegate = new Selenium()
		return this
	}
}

class Selenium {
	static SeleniumServer server
	CommandProcessor commandProcessor
	/**
	 * Open a browser instance and load a domain/page
	 * @param domain
	 * @return
	 */
	def browser(String baseUrl, browser = '*firefox') {
		if (!server) {
			server = new SeleniumServer()
			server.boot()
			server.start()
		}
		commandProcessor = new HttpCommandProcessor('localhost', server.port, browser, baseUrl);
		commandProcessor.start()
	}
	/**
	 * Click on a list of web elements defined by a selector
	 * @param selector type=value where type is id, link, partial, tag, name, class, style or xpath.
	 * @return
	 */
	def click(locator) {
		waitForElementPresent(locator)
		methodMissing('click', locator)
	}
	/**
	 * Webdriver does not do it all - we need to send old fashioned Selenium commands as well.
	 * @param command Selenium command
	 * @param args One argument or an array of arguments
	 * @return Whatever Selenium returns - OK or error message usually
	 */
	def methodMissing(String command, args) {
		def result = commandProcessor.doCommand(command, [args].flatten() as String[])
		System.err.println "$command($args) = $result"
		return result
	}
}
