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

import usdlc.Store;
import groovy.xml.StreamingMarkupBuilder;

class SectionRunnerActor extends Actor {
	void run() {
		run(Store.base(exchange.request.query.page),
				exchange.request.query.sections.split(',') as Set)
	}

	void run(Store page, Set sections = []) {
		def html = htmlSlurper.parseText(page.text())
		if (html) {
			out.println "<html><body>"
			def namespace = html[0].namespaceURI()
			def sectionsHtml = html.breadthFirst().findAll{it.@contextmenu == 'section'}
			if (sections) {
				sectionsHtml = sectionsHtml.findAll{
					(it.@id as String) in sections
				}
			}
			def actors = []
			// Walk through each section specified and run linked pages (saving actors)
			sectionsHtml.each{ section ->
				section.breadthFirst().findAll{it.name() == 'A'}.each { link ->
					def linkStore = page.rebase(link.@href as String)
					switch (link.@action) {
						case 'page':
							run(linkStore)
							break
						case 'runnable':
							actors.push(linkStore)
							break
					}
				}
			}
			// afterwards run any actors in the referenced sections
			if (actors) Actor.wrap(actors, [exchange : exchange])
			out.println "</body></html>"
		}
	}
	static htmlSlurper = new XmlSlurper(new org.cyberneko.html.parsers.SAXParser())
}
