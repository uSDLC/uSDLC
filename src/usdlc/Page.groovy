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
package usdlc

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder

/**
 * Here we read in a uSDLC page and provide access to the internals:
 */
class Page {
	@Delegate Document dom
	/**
	 * Load a file if it exists - otherwise load the template.
	 */
	Page(String fileName) {
		load(Store.base(fileName))
	}
	/**
	 * Load a file if it exists - otherwise load the template.
	 */
	Page(Store from) {
		load(from)
	}
	/**
	 * Load a file if it exists - otherwise load the template.
	 */
	private load(Store from) {
		store = from
		updated = !from.exists()
		dom = Jsoup.parse(updated ? template : from.text)
		titleDiv = dom.select('#pageTitle')
		title = titleDiv.select('h1').first()
		subtitle = titleDiv.select('h2').first()
		sections = dom.select('div.section')
		synopsis = sections.first()
		footer = sections.last()
		sections = sections.not('div.footer, div:lt(1)')

		if (updated) {
			title.text(Store.decamel(from.parts.name))
		}

		if (!titleDiv.hasAttr('uuid')) {
			titleDiv.attr('uuid', UUID.randomUUID().toString())
			updated = true
		}
	}
	/**
	 * Get page title
	 */
	String getTitle() {
		return title.html()
	}
	/**
	 * Set page title
	 */
	void setTitle(newTitle) {
		title.html(newTitle)
		updated = true
	}
	/**
	 * Get page subtitle
	 */
	String getSubtitle() {
		subtitle.html()
	}
	/**
	 * Set page subtitle
	 */
	void setSubtitle(newTitle) {
		subtitle.html(newTitle)
		updated = true
	}
	/**
	 * Retrieve the Synopsis
	 */
	String getSynopsis() {
		sections.get(0).html()
	}
	/**
	 * Fill in the Synopsis (first section)
	 */
	void setSynopsis(text) {
		synopsis.html(text)
		updated = true
	}
	/**
	 * Save the modified html file
	 */
	void save(to = store) {
		if (updated) store.text = dom.outerHtml()
	}

	static slurper = new XmlSlurper(new org.cyberneko.html.parsers.SAXParser())
	static template = Store.base("rt/template.html").text
	def out = new StreamingMarkupBuilder(), titleDiv, updated
	def store, title, subtitle, sections, synopsis, footer
}
