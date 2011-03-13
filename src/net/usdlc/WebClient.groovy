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
package net.usdlc

/**
 * User: Paul
 * Date: 25/02/11
 */
class WebClient {
	com.gargoylesoftware.htmlunit.WebClient webClient = new com.gargoylesoftware.htmlunit.WebClient()
	def current
	def elements
	/**
	 * Constructor - creates an instance and loads a page
	 * @param url Page to load
	 */
	WebClient(String url) {
		current = webClient.getPage(url)
	}
	/**
	 * Builder - creates an instance and loads a page
	 * @param url Page to load
	 * @return Instance of WebTest already primed
	 */
	static WebClient load(url) {
		return new WebClient(url)
	}

	def xpath(String xpath) {
		elements = current.getByXPath(xpath)
		if (size() != 0) {
			current = elements[0]
		}
		return this
	}

	def size() {
		return elements.size()
	}

	def click() {
		return current.click()
	}

	def hasClass(name) {
		return classes.find(name)
	}

	def getClasses() {
		return current.getAttribute('class').split(' ')
	}

	def getAt(idx) {
		return elements[idx]
	}
}
