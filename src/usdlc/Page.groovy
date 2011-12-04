package usdlc

import groovy.xml.StreamingMarkupBuilder
import org.cyberneko.html.parsers.SAXParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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

	static slurper = new XmlSlurper(new SAXParser())
	static template = Store.base("rt/template.html").text
	def out = new StreamingMarkupBuilder(), titleDiv, updated
	def store, title, subtitle, sections, synopsis, footer
}
