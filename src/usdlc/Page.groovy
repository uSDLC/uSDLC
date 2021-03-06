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
	Page(String fileName, displayName='') {
		load(store(fileName), displayName)
	}
	/**
	 * Load a file if it exists - otherwise load the template.
	 */
	Page(Store from, displayName='') {
		load(store(from), displayName)
	}
	/**
	 * Load a file if it exists - otherwise load the template.
	 */
	private load(Store from, displayName) {
		this.displayName = displayName
		store = from
		updated = false
		dom = Jsoup.parse(from.text)
		titleDiv = dom.select('#pageTitle')
		title = titleDiv.select('h1').first()
		subtitle = titleDiv.select('h2').first()
		allSections = dom.select('div.section')
		synopsis = new Section(allSections.first())
		footer = new Section(allSections.last())
		allSections = allSections.not('div.footer, div:lt(2)')
		sections = allSections.collect { new Section(it) }

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
	static Store store(Store store) {
		if (store.isDirectory ||
				store.path.endsWith('/') ||
				store.path.indexOf('.') == -1) {
			store = store.rebase('index.gsp')
			if (!store.exists()) store = store.rebase('index.html')
		}
		if (store.isHtml && !store.exists()) {
			def page = new Page('usdlc/Environment/Configuration/Templates/Pages/Default/index.html')
			page.store = store
			def title, fn = store.parts
			if (fn.name == 'index') {
				title = fn.path.replaceFirst(~'.*/', '')
			} else {
				title = fn.name
			}
			page.select('div#pageTitle h1').html(Store.decamel(title))
			page.save()
		}
		return store
	}

	static Store store(String path) { store(Store.base(path)) }
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
	static void walk(Closure pageProcessor) {
		//drill(Store.usdlcRoot, pageProcessor)
		Store.projectIndexes.each { drill(it, pageProcessor) }
	}

	private static drill(Store store, Closure pageProcessor) {
		Page page = new Page(store)
		pageProcessor(page)
		page.select('a[action=page]').each { link ->
			String href = link.attr('href')
			if (href.indexOf('..') == -1 && href.indexOf('/') == -1) {
				drill(store.rebase(href), pageProcessor)
			}
		}
	}
	/**
	 * To work from scripts, it is easier if we use lists rather
	 * than closures.
	 */
	static Page[] pages() {
		def pages = new PageCache()
		Store.projectIndexes.each { page -> pages.add page }
		return pages.list
	}
	/**
	 * Always called after pages() to walk the tree
	 */
	static Page[] pages(parent) {
		def pages = new PageCache()
		parent.select('div:not(.deleted) a[action=page]').each { link ->
			String href = link.attr('href')
			if (href.indexOf('..') == -1/* && href.indexOf('/') == -1*/) {
				def child = parent.store.rebase(href)
				def state = child.rebase('pagestate.txt').text.trim()
				pages.add child, link.text(), state
			}
		}
		return pages.list
	}
	/**
	 * Return all child pages of this one.
	 */
	def children() { pages(this) }

	static class PageCache {
		def cache = [] as Set
		def list = []

		def reset() { list = [] }

		def add(Store store, displayName = '', state='') {
			String path = store.path
			if (!cache.contains(path)) {
				cache.add(path)
				def page = new Page(store, displayName)
				if (state) page.state = state
				if (store.isHtml) list << page
			}
		}
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
	boolean save(to = store) {
		if (updated) {
			to.text = (dom.select('body') ?: dom).html()
			updated = false
			return true
		}
		return false
	}
	boolean forceSave() {
		updated = true
		save()
	}

	def rename(from, to) {
		def link = allSections.select("h1 a[href^=$from]").first()
		link.html(to)
		forceSave()
	}

	def createChild(name, id) {
		def href = Store.camelCase(name)
		footer.before("""
			<div id="$id" class="editable section synopsis">
			 <h1><a href="$href" action="page" id="${id}a0"
				class="usdlc">$name</a></h1>
		""")
		forceSave()
	}

	def delete(name) {
		def href = Store.camelCase(name)
		store.rebase(href).delete()
		childSection(href).remove()
		forceSave()
	}

	def childSection(href) {
		def a = allSections.select("a[href^=$href]")
		if (a.size() > 1) {
			a = allSections.find {
				tag = it.parent().tagName().toLowerCase()
				tag in (['h1','h2','h3'] as Set)
			}
		} else {
			a = a.first()
		}
		findSectionFor(a)
	}

	def paste(fromName, toPage, toName, position, cut = 'true') {
		cut = cut.toBoolean()
		def section = childSection(fromName)
		if (position == 'first' || position == 'last') {
			toPage = new Page(toPage.store.rebase(toName))
		}
		toPage.childSection(fromName)?.remove();   // so no duplicates
		def sameParent = store == toPage.store
		if (sameParent) toPage = this
		if (! cut) section = section.clone() // so we get a copy
		toPage.prepareSectionForMove(section)
		switch (position) {
			case 'before':
				toPage.childSection(toName).before(section)
				break
			case 'after':
				toPage.childSection(toName).after(section)
				break
			case 'first':
				toPage.synopsis.after(section)
				break
			case 'last':
				toPage.footer.before(section)
				break
		}
		toPage.forceSave()
		if (!sameParent) {
			if (cut) {
				section.remove()
				forceSave()
				store.rebase(fromName).moveTo("$toPage.store.dir/$fromName")
			} else {
				store.rebase(fromName).copyTo("$toPage.store.dir/$fromName")
			}
		}
	}

	def prepareSectionForMove(section) {
		def newId = nextSectionId(), a = 0
		section.attr('id', "s$newId")
		section.select('a').each {it.attr('id', "s${newId}a${a++}")}
		return section
	}

	def nextSectionId() {
		def id = sections.size()
		def ids = sections.collect {it.attr('id')} as Set
		while (id in ids) { id++ }
		return id
	}

	def sectionFromId(id) { selectSection("#$id") }

	def selectSection(selector) {
		findSectionFor(allSections.select(selector).first())
	}

	def findSectionFor(element) {
		while(element && ! element.hasClass('section')) {
			element = element.parent()
		}
		return element
	}

	String toString() {
		return store?.toString()
	}

	static slurper = new XmlSlurper(new SAXParser())
	def out = new StreamingMarkupBuilder(), titleDiv, updated, displayName
	def store, title, subtitle, sections, allSections, synopsis, footer
	def state = ''
	/** Wrap the dom section item to add functionality */
	class Section {
		@Delegate Element dom

		Section(dom) {this.dom = dom}
		/** Retrieve the unique section id */
		String getId() { dom?.attr('id') ?: '' }
		Page getChild() {
			def child = dom?.select('h1,h2,h3')?.select('a')
			if (!child.size()) return null
			def href = child.attr('href')
			if (href.indexOf('/') != -1) return null
			return new Page(store.rebase(href))
		}
		String getHeading() {
			return dom?.select('h1,h2,h3')?.first()?.text()?.trim() ?: ''
		}
		String getType() {
			return dom?.select('div.sectionType')?.text()?.trim() ?: 'General'
		}
		boolean isDeleted() {dom.hasClass('deleted')}

		class Workflow {
			def map = [:] as TreeMap;
			Workflow(dom) {
				if (dom) {
					dom.text().trim().split(';;').each {
						if (it.indexOf('=') != -1) {
							def (key, value) = it.split('=')
							map[key] = map[key] ? "${map[key]}\n$value" : value
						}
					}
				}
			}
		}
		Workflow getWorkflow() {
			new Workflow(dom?.select('div.workflow'))
		}
	}
	/**
	 * Walk from this page down and where there is a section that is a
	 * reference copy to another, update it with the latest gumph.
	 */
	public updateReferenceCopies() {
		drill(store) { page ->
			page.allSections.select("div.sectionType a").each { a ->
				def copySection = page.findSectionFor(a)
				def href = a.attr('href').split(/@/)
				def sourcePage = new Page(href[0])
				def sourceSection = sourcePage.allSections.
						select("#${href[1]}").first()
				if (copyReferenceContents(sourceSection, copySection)) {
					page.forceSave()
				}
			}
		}
	}
	private referenceContents(section) {
		return section.children().not('div.sectionType')
	}
	boolean copyReferenceContents(sourceSection, copySection) {
		def copyContents = referenceContents(copySection)
		def sourceContents = referenceContents(sourceSection)

		if (copyContents.html() == sourceContents.html()) return false

		copyContents.remove()
		sourceContents.each {copySection.appendChild(it)}
		return true
	}
}
