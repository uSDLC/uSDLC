package usdlc

import groovy.xml.StreamingMarkupBuilder
import org.cyberneko.html.parsers.SAXParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

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
		allSections = dom.select('div.section')
		synopsis = new Section(allSections.first())
		footer = new Section(allSections.last())
		allSections = allSections.not('div.footer, div:lt(1)')
		sections = allSections.collect { new Section(it) }

		if (updated) {
			title.text(Store.decamel(from.parts.name))
		}

		if (!titleDiv.hasAttr('uuid')) {
			titleDiv.attr('uuid', UUID.randomUUID().toString())
			updated = true
		}
	}
	/**
	 * Walk all pages in the uSDLC and connected projects, calling a closure
	 * for each page.
	 */
	static Page walk(Closure pageProcessor) {
		drill(Store.usdlcRoot, pageProcessor)
		Store.projectRoots.each { drill(it, pageProcessor) }
	}

	private static drill(Store store, Closure pageProcessor) {
		Page page = new Page(store)
		pageProcessor(page)
		page.links('a[action=page]').each { link ->
			String href = link.attr('href')
			pageProcessor(new Page(store.rebase(href)))
		}
	}
	/**
	 * To work from scripts, it is easier if we use lists rather
	 * than closures.
	 */
	static Page[] pages() {
		def pages = [new Page(Store.usdlcRoot)]
		Store.projectRoots.each { page -> pages << new Page(page) }
		return pages
	}
	static Page[] pages(parent) {
		def pages = []
		parent.links('a[action=page]') { link ->
			String href = link.attr('href')
			if (href.indexOf('..') == 0)
				pages << new Page(parent.store.rebase(href))
		}
		return pages
	}
	/**
	 * Get page title
	 */
	String getTitle() { title.html() }
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
		if (updated) to.text = dom.outerHtml()
	}

	static slurper = new XmlSlurper(new SAXParser())
	static template = Store.base('rt/template.html').text
	def out = new StreamingMarkupBuilder(), titleDiv, updated
	def store, title, subtitle, sections, allSections, synopsis, footer

	void links(String selector, Closure processor) {
		allSections.select(selector).each { processor(it) }
	}
	/** Wrap the dom section item to add functionality */
	class Section {
		@Delegate Element dom
		Section(dom) {this.dom = dom}
		/** Call a closure for each link that matches the CSS selector */
		void links(String selector, Closure processor) {
			dom?.select(selector)?.each { processor(it) }
		}
		/** Retrieve the unique section id */
		String getId() { dom?.attr('id') ?: '' }
	}
}
