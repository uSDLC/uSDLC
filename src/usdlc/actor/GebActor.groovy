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

import geb.Browser
import geb.driver.CachingDriverFactory
import geb.driver.PropertyBasedDriverFactory

class GebActor extends GroovyActor {
	Map dsl = [
			driver: { String choice -> driverList = choice.toLowerCase() },
			browse: { GroovyObject pc -> browse(pc) },
	]

	Browser getBrowser() { context['geb.driver'] }

	private driverList = config.browserDriverList
	/**
	 * Called by lazy load of geb.Browser to create a copy based on the default or pre-specified driver. Note that it is saved in a static so that subsequent actor in will use the same driver until it is reset.
	 * @return geb.Browser instance.
	 */
	private driver() { new Browser(new PropertyBasedDriverFactory(['geb.driver': driverList] as Properties).driver) }

	// Keep a static copy we can use between scripts on a page to use the same browser.
	/**
	 * Close down browser instance and quit the driver.
	 */
	void reset() {
		try { CachingDriverFactory.clearCacheAndQuitDriver() } catch (e) {}
		context['geb.driver'] = driver()
	}
	/**
	 * Geb needs to know the base url (domain) to work with - and your test needs to know which page to start
	 * @param pc - geb.Page class - with a url and an optional authority
	 */
	Browser browse(pc) {
		try {
			if (!browser)
				context['geb.driver'] = driver()
			if (browser.page.class == pc) {
				browser.$(0)    // hit the browser and make sure it is still alive
			} else {
				if (pc.hasProperty('authority')) browser.baseUrl = pc.getProperty('authority')
				browser.to(pc as Class)
				browseDepth = 0
			}
			return browser
		} catch (e) {
			reset()
			return (browseDepth++) ? null : browse(pc)
		}
	}

	int browseDepth = 0
	/**
	 * For some reason geb expects a fully defined list instead of duck typing. Here we do a conversion.
	 * @param list Groovy list of classes [MyPage, YourPage]
	 */
	//void page(ArrayList list) { page(list as List<Class<? extends geb.Page>>) }
	/**
	 * Override runScript to catch and process geb exceptions when talking to the browser.
	 * @param scriptName
	 * @return
	 */
	void run() {
		try {
			super.run()
		} catch (IllegalStateException ise) {
			ise.printStackTrace()
			reset()
		}
	}
}
