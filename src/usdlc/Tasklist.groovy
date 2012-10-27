package usdlc

import org.jsoup.nodes.Element
import org.jsoup.parser.Tag

class Tasklist {
	def tasklistPage, tasklistDiv, sourcePage = null
	def tasklistSection, tasklistSectionId, taskIndex = 2
	def tasklistDetailDefault, tasklistDetails

	Tasklist(Page page, String sectionId) {
		tasklistPage = page
		tasklistSection = tasklistPage.sectionFromId(sectionId)
		tasklistSectionId = tasklistSection.attr('id')
		tasklistDiv = tasklistSection?.select('div.tasklist[source]')?.first()
		tasklistDetailDefault = tasklistSection.select(
				"#${tasklistSectionId}t0d").first()
		tasklistDetails = tasklistDetailDefault.parent()

		def sourceURL = tasklistDiv?.attr('source')
		if (sourceURL) sourcePage = new Page(sourceURL)
	}

	public refresh() {
		if (!sourcePage) return
		def baseTask = tasklistDiv.select('li').first()
		walk(baseTask, sourcePage)
		tasklistPage.forceSave()
	}

	private walk(Element taskList, Page page) {
		def tasksMap = [:] as SortedMap
		taskList.select('ul>li').inject(tasksMap) { map, li ->
			map << [li.attr('section'), li]
		}
		taskList.empty()
		def href = page.store.href
		page.sections.each { Page.Section section ->
			if (! section.isDeleted()) {
				def sectionId = section.id
				def li = tasksMap[sectionId]
				if (li) {
					tasksMap.remove(sectionId)
				} else {
					li = new Element(new Tag('li'), '').attr('section', section.id)
				}
				def heading = section.heading
				if (heading) {
					li.html("<a href='#'>$heading</a>")
					def taskId = "${tasklistSectionId}t${taskIndex++}"
					li.attr('id', taskId)
					def detailId = "${taskId}d"
					def detail = tasklistSection.select("#$detailId").first()
					if (!detail) {
						detail = newDetail(detailId, heading, "$href@$sectionId")
						addHistory( detail, "Created")
						addTag( detail, section.type)
						tasklistDetails.appendChild(detail)
					}
					def child = section.child
					if (child) {
						def childTasklist = new Element(new Tag('ul'), '')
						walk(childTasklist, child)
						li.appendChild(childTasklist)
					}
					taskList.appendChild(li)
				}
				// add any manual items to the end of the list
				tasksMap.each { key, value -> taskList.append(value) }
			}
		}
	}

	private newDetail(detailId, heading, href) {
		def detail = tasklistDetailDefault.clone()
		detail.attr('id', detailId)
		def taskPageLinks = detail.select("fieldset.taskPageLinks").first()
		if (taskPageLinks) {
			taskPageLinks.html(
					"<a href='$href' action='page' class='usdlc'>$heading</a>")
		}
		return detail
	}

	private addHistory(detailDiv, message) {
		def taskHistory = detailDiv.select("fieldset.taskHistory").first()
		if (taskHistory) {
			def div = taskHistory.appendElement('div')
			def date = new Date().format('D, dd-MM-yyyy')
			div.addClass('taskHistoryItem').html(
				"<span class='taskHistoryDate>$date</span> $message"
			)
		}
	}

	private addTag(detailDiv, tag) {
		def tagInput = detailDiv.select('input[name=Tags]').first()
		if (tagInput) {
			def value = tagInput.attr('value')
			if (value.indexOf("$tag,") == -1) {
				tagInput.attr('value', "${value}tag, ")
			}
		}
	}
}
