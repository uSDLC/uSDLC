package usdlc

def query = exchange.request.query

headings = ['State','Users','Priority','Estimate','Start','Due','Tags','Order']
//write("Context,Title,${headings.join(',')}\n")

def walk(Page page) {
	if (!page) return

	def href = Store.split(page.store.href).path
	def root = Store.decamel(href).
			replaceAll(~'/~.*?/usdlc/?', '').replaceAll(~'/', ' - ')
	page.sections.each { Page.Section section ->
		if (! section.isDeleted()) {
			def sectionId = section.id
			def heading = section.heading
			if (heading) {
				write("""$root,<a href=\"javascript:openLink('$href@$sectionId')\">$heading</a>""")
				map = section.workflow.map
				headings.each { key ->
					write(",${map[key] ?: ''}")
				}
				write('\n')
				walk(section.child)
			}
		}
	}
}

walk(new Page("~${Store.camelCase(query.project)}/usdlc"))
