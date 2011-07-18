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

import usdlc.History
import usdlc.Store
import static init.Config.config

/**
 * uSDLC supports actor and filters. This filter provides HTML templating. By default it runs templates/pasteList.html.groovy that provides header, body and scrolling elements.
 *
 * User: Paul Marrington
 * Date: 23/11/10
 * Time: 6:22 PM
 */
class HtmlActor extends Actor {
	/**
	 Use to generate HTML to display on the screen.
	 */
	void run() {
		switch (exchange.request.query['action']) {
			case 'history':
				def end = Integer.parseInt(exchange.request.query['index'].toString() ?: '-1')
				def contents = new History(exchange.store.path, 'updates').restore(end)
				out.println contents
				break
			case 'cut':
				transfer { Store store, String to -> store.move(to) }
				break
			case 'copy':
				transfer { Store store, String to -> store.copy(to) }
				break
			case 'paste':
				paste()
				break
			default:    // Suck the HTML file contents - converting from byte[] to String.
				exchange.response.write exchange.store.read() ?: template()
				break
		}
	}

	private byte[] template() {
		List matcher = (exchange.store.path =~ ~/\.(.+)$/)[0]
		matcher.findResult([] as byte[]) { String ext ->
			String template = config.template[ext]
			template ? Store.base("rt/${template}.$ext").read() : null
		}
	}

	private void transfer(Closure transfer) {
		Store clipboard = Store.base('store/clipboard')
		String targetName = clipboard.uniquePath(exchange.request.query['title'])
		exchange.request.query['dependents'].tokenize(',').each { String dependent ->
			transfer(exchange.store.rebase(dependent), "store/clipboard/$targetName");
		}
		def contents = exchange.request.body().bytes
		clipboard.base("$targetName/Section.html").write(contents)
		String title = exchange.request.query['title'].replaceAll(/'/, /\\'/)
		def action = exchange.request.query['action']
		out.println "usdlc.${action}SectionSuccessful('$title','/$targetName')"
	}

	private void paste() {
		def target = exchange.store.parent
		def from = Store.base("store/clipboard/${exchange.request.query['from']}")
		from.dir(~/[^.]*/) { String dir -> from.rebase(dir).move(target) }
		out.println new String(Store.base("${exchange.request.query['from']}/Section.html").read())
		from.rmdir()
	}
}
