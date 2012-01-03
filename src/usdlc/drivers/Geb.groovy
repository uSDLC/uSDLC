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
package usdlc.drivers

//import geb.driver.PropertyBasedDriverFactory


import geb.Browser
import static usdlc.config.Config.config

class Geb {
	Browser browser

	def driverList = config.browserDriverList
	def browseDepth = 0

	def driver(String list) {
		close()
		driverList = list.toLowerCase()
		Properties drivers = ['geb.driver': driverList]
//		PropertyBasedDriverFactory factory = new PropertyBasedDriverFactory
// (drivers)
		browser = new Browser(factory.driver)
	}

	def close() { browser?.driver?.quit() }

	def reset() { close(); driver(driverList) }

	def browse(Class pageClass, String authority) {
		try {
			if (!browser) driver(driverList)
			if (browser.page.class == pageClass) {
				browser.$(0)    // hit the browser and make sure it is still alive
			} else {
				browser.baseUrl = authority
				browser.to(pageClass)
				browseDepth = 0
				browser
			}
		} catch (e) {
			e.printStackTrace()
			reset()
			return (browseDepth++) ? null : browse(pageClass)
		}
	}
}
