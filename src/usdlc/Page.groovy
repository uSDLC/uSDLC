package usdlc

import groovy.xml.StreamingMarkupBuilder

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
/**
 * Here we read in a uSDLC page and provide access to the internals:
 *
 * title: html element for the title element.
 * title.uuid: UUID for this page - unique across instances and uSDLC projects
 * html: the html document
 * updated: whether to write changes on update.
 */
class Page {
	/**
	 * Given a uSdlc html content file, provide action to the individual components.
	 * @param sourceFile mime-type container for the file to run - gives path, name, etc
	 * @return A run instance ready to use for a specific file.
	 */
	Page(contents) {
		html = slurper.parseText(contents)
		namespace = html[0].namespaceURI()

		title = html.find {it.@id == 'pageTitle'}
		if (!title?.@uuid) {
			title.@uuid = UUID.randomUUID()
			updated = true
		}
	}
	/**
	 Action all sections on the page...
	 */
	def sections(closure) {
		html.breadthFirst().findAll {it.@contextmenu == 'section'}.each(closure)
	}

	String toString() {
		def outputBuilder = new StreamingMarkupBuilder()
		String xml = outputBuilder.bind { mkp.yield html }
		return xml
	}

	static slurper = new XmlSlurper(new org.cyberneko.html.parsers.SAXParser())
	def out = new StreamingMarkupBuilder(), html, namespace, title, updated
}
