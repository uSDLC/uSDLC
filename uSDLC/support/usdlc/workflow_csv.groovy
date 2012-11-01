package usdlc

def query = exchange.request.query

headings = ['State','Users','Priority','Estimate','Start','Due','Tags']
write("Title,${headings.join(',')}\n")

def walk(Page page) {
	if (!page) return

	def href = Store.split(page.store.href).path
	page.sections.each { Page.Section section ->
		if (! section.isDeleted()) {
			def sectionId = section.id
			def heading = section.heading
			if (heading) {
				write("<a href='$href@$sectionId' action='page' class='usdlc'>$heading</a>")
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
