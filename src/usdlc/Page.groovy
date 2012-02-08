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
		load(store(fileName))
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
		// todo: change store.read() and store.text to Page method
		// todo: page reads to change store to point to index.html/gsp if  needed
		store = from
		updated = false
		dom = Jsoup.parse(from.text)
		titleDiv = dom.select('#pageTitle')
		title = titleDiv.select('h1').first()
		subtitle = titleDiv.select('h2').first()
		allSections = dom.select('div.section')
		synopsis = new Section(allSections.first())
		footer = new Section(allSections.last())
		allSections = allSectionds.not('div.footer, div:lt(2)')
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
	 * Use Page.store() instead of Store.base() to open html pages. It is a
	 * good samaritan and decides whether you want index.html or index.gsp
	 * when you just give a directory.
	 */
	static Store store(String path) {
		store(Store.base(path))
	}
	static Store store(Store store) {
		if (store.file.isDirectory() ||
				path.endsWith('/') || path.indexOf('.') == -1) {
			store = store.rebase('index.gsp')
			if (!store.exists()) store = store.rebase('index.html')
		}
		if (!store.exists()) {
			def page = new Page('rt/template.html')
			page.store = store
			def title = Store.decamel(path.replaceFirst(~'.*/', ''))
			page.select('div#pageTitle h1').html(title)
			page.save()
		}
		return store
	}
	/**
	 * Replace contents of elements with HTML from text provided.
	 */
	def html(String innerHTML) {
		super.html(innerHTML)
		updated = true
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
		page.select('a[action=page]').each { link ->
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
		parent.select('a[action=page]').each { link ->
			String href = link.attr('href')
			if (href.indexOf('..') == -1)
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
	def out = new StreamingMarkupBuilder(), titleDiv, updated
	def store, title, subtitle, sections, allSections, synopsis, footer
	/** Wrap the dom section item to add functionality */
	class Section {
		@Delegate Element dom
		Section(dom) {this.dom = dom}
		/** Retrieve the unique section id */
		String getId() { dom?.attr('id') ?: '' }
	}
}
