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
package usdlc.actor

import be.roam.hue.doj.Doj
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.CollectingAlertHandler
import com.gargoylesoftware.htmlunit.html.HtmlPage

/**
 * User: Paul Marrington
 * Date: 27/03/11
 * Time: 4:22 PM
 */
class HtmlunitActor extends GroovyActor {
	def bind() { return bind([client: new HtmlUnit()]) }

	static class HtmlUnit {
		@Delegate Doj doj
		HtmlPage page

		/**
		 * Constructor - creates an instance and loads a page
		 * @param url Page to load
		 */
		public load(String url) {
			webClient = new com.gargoylesoftware.htmlunit.WebClient(BrowserVersion.FIREFOX_3_6)
			def collectedAlerts = [];
			webClient.alertHandler = new CollectingAlertHandler(collectedAlerts);
			try {
				doj = Doj.on(page = webClient.getPage(url))
				webClient.waitForBackgroundJavaScript(10000)
			} finally {
				collectedAlerts.each { System.err.println(it); }
			}
		}
		/**
		 * Run JavaScript as if from the page
		 * @param script valid Javascript to execute in page context
		 */
		public executeJavaScript(script) {
			page.executeJavaScript(script)
			webClient.waitForBackgroundJavaScript(2000)
		}
		/**
		 * Get a page element based on a selector
		 * @param selector
		 * @return this for chaining
		 *
		 * webClient.table.a.span
		 */
		def propertyMissing(String selector) {
			return getAt(selector)
		}
		/**
		 * Get a page element based on a selector
		 * @param selector
		 * @return this for chaining
		 *
		 * webClient['img#logo']
		 */
		def getAt(String selector) {
			return new HtmlUnit(doj.get(selector))
		}

		def getAt(Integer selector) {
			return new HtmlUnit(doj.get(selector))
		}
		/**
		 * Dump element as XML to standard error for review
		 * @return this for chaining
		 */
		def dump() {
			doj.allElements().each {
				System.err.println(it.asXml())
			}
			return this
		}
		/**
		 * Emulate closing the browser window
		 */
		def close() {
			webClient.closeAllWindows()
		}

		public HtmlUnit() {}

		private HtmlUnit(Doj doj) { this.doj = doj }

		def webClient
	}
}
