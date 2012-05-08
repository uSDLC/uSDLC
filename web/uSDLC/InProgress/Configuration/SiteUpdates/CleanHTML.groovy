reports = [:]
changedPages = 0

def report(type) {
	if (!reports[type]) reports[type] = 0
	reports[type] += 1
}

/** remove default index.html from links */
def removeIndex(page, link, href) {
	def changed = href.replaceFirst(~/\/index.(html?|gsp)$/, '')
	if (changed != href) {
		link.attr('href', changed)
		page.updated = true
		report 'default index.html link text removed'
	}
}

usdlc.Page.walk { page ->
println page
	page.select('a').each { link ->
		def action = link.attr('action')
		def href = link.attr('href')
		def isLocal = (href.indexOf(':') == -1)
		if (href.endsWith('..')) href = "$href/"
		def dot = href.endsWith('../') ? -1 : href.lastIndexOf('.')
		def type = (dot == -1) ? 'html' : href[dot+1..-1].toLowerCase()

		def setAction = { replacement, css ->
			if (action != replacement) {
				link.attr('action', replacement)
				link.attr('class', css)
				page.updated = true
				report 'link actions corrected'
			}
		}

		if (isLocal) {
			if (type ==~ /(html?)|(gsp)/) {
				setAction('page', 'usdlc')
			} else {
				setAction('runnable', 'usdlc sourceLink')
			}
			switch (action) {
				case 'page':
					removeIndex(page, link, href)
					break;
				case 'runnable':
					break;
				default:
					break;
			}
		} else {    // ! isLocal
			if (action) setAction('', '')
		}
	}
	if (page.save()) changedPages += 1
}

write "$changedPages pages updated\n"
reports.each { type, count ->
	write "$count $type\n"
}
