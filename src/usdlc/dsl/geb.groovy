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

import geb.Browser
import geb.driver.CachingDriverFactory
import geb.driver.PropertyBasedDriverFactory
import static init.Config.config

driverList = config.browserDriverList
browser = null
browseDepth = 0

driver = { list ->
	driverList = list.toLowerCase()
	browser = new Browser(new PropertyBasedDriverFactory(['geb.driver': driverList] as Properties).driver)
}

reset = {
	try { CachingDriverFactory.clearCacheAndQuitDriver() } catch (e) {e.printStackTrace()}
	driver(driverList)
}
finalisers << reset

browse = { Class pageClass ->
	try {
		if (!browser) browser = driver(driverList)
		if (browser.page.class == pageClass) {
			browser.$(0)    // hit the browser and make sure it is still alive
		} else {
			browser.baseUrl = authority
			browser.to(pageClass)
			browseDepth = 0
		}
		return browser
	} catch (e) {
		e.printStackTrace()
		reset()
		return (browseDepth++) ? null : browse(pageClass)
	}
}