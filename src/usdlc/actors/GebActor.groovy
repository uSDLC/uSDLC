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

import geb.Browser
import geb.driver.CachingDriverFactory
import geb.driver.PropertyBasedDriverFactory
import org.openqa.selenium.WebDriverException
import usdlc.Environment

class GebActor extends GroovyActor {
	def bind() {
		def env = Environment.data()
		delegate = env?.geb ?: (env.geb = new Geb())
		return this
	}
}
class Geb {
	@Delegate Browser browser
	/**
	 * Open browser with a specific driver
	 * @param driver Colon separated list of drivers. Take the first that works.
	 * @return browser instance
	 */
	void driver(String driverList) {
		this.driverList = driverList.toLowerCase()
	}
	/**
	 * Called by lazy load of geb.Browser to create a copy based on the default or pre-specified driver. Note that it is saved in a static so that subsequent actors in will use the same driver until it is reset.
	 * @return geb.Browser instance.
	 */
	private driver() {
		if (browser) reset()
		browserInstance = new Browser(new PropertyBasedDriverFactory(['geb.driver': driverList] as Properties).driver)
		return browserInstance
	}

	private driverList = 'firefox:chrome:ie:htmlunit'
	// Keep a static copy we can use between scripts on a page to use the same browser.
	static Browser browserInstance
	/**
	 * Close down browser instance and quit the driver.
	 */
	void reset() {
		if (!CachingDriverFactory.clearCacheAndQuitDriver()) {
			browserInstance?.driver?.quit()
		}
		browserInstance = null
		browser = null
		browser = browserInstance ?: driver()
	}
	/**
	 * Geb needs to know the base url (domain) to work with - and your test needs to know which page to start
	 * @param pc - geb.Page class - with a url and an optional authority
	 */
	Browser browse(Class pc, driverList = this.driverList) {
		try {
			if (!browser) {
				this.driverList = driverList
				browser = browserInstance ?: driver()
			}
			if (page.class != pc) {
				if (pc?.authority) { browser.baseUrl = pc.authority }
				browser.to(pc)
				browseDepth = 0
			}
			return browser
		} catch (WebDriverException e) {
			reset()
			if (!browseDepth++) { return browse(pc) }
			return null
		}
	}

	int browseDepth = 0
	/**
	 * For some reason geb expects a fully defined list instead of duck typing. Here we do a conversion.
	 * @param list Groovy list of classes [MyPage, YourPage]
	 */
	void page(ArrayList list) { page(list as List<Class<? extends geb.Page>>) }
}
