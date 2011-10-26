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

import be.roam.hue.doj.Doj
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.CollectingAlertHandler
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage


class HtmlunitActor extends GroovyActor {
	Map dsl = [client: new HtmlUnit()]

	static class HtmlUnit implements Closeable {
		@Delegate Doj doj
		HtmlPage page
		WebClient webClient

		/** Creates an instance and loads a page       */
		void load(String url) {
			webClient = new WebClient(BrowserVersion.FIREFOX_3_6)
			List collectedAlerts = []
			webClient.alertHandler = new CollectingAlertHandler(collectedAlerts);
			try {
				doj = Doj.on(page = webClient.getPage(url))
				webClient.waitForBackgroundJavaScript(10000)
			} finally {
				alerts(collectedAlerts)
			}
		}

		private void alerts(List alerts) {
			alerts.each { String alert -> usdlc.Log.err(alert) }
		}
		/**
		 * Run JavaScript as if from the page
		 * @param script valid Javascript to execute in page context
		 */
		void executeJavaScript(String script) {
			page.executeJavaScript(script)
			webClient.waitForBackgroundJavaScript(2000)
		}
		/**
		 * Get a page element based on a selector
		 *
		 * webClient.table.a.span
		 */
		def propertyMissing(String selector) { this[selector] }
		/**
		 * Get a page element based on a selector
		 * @param selector
		 * @return this for chaining
		 *
		 * webClient['img#logo']
		 */
		HtmlUnit getAt(String selector) { new HtmlUnit(doj.get(selector)) }

		HtmlUnit getAt(Integer selector) { new HtmlUnit(doj.get(selector)) }
		/**
		 * Dump element as XML to standard error for review
		 * @return this for chaining
		 */
		HtmlUnit dump() {
			doj.allElements().each {
				System.err.println(it.asXml())
			}
			this
		}
		/** Emulate closing the browser window       */
		void close() { webClient.closeAllWindows() }

		HtmlUnit() {}

		private HtmlUnit(Doj doj) { this.doj = doj }
	}
}
