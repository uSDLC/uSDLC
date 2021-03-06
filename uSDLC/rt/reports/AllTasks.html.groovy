import usdlc.Page
import usdlc.Store

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
doc.div(id: "pageTitle", 'class': "editable") {
	h1("")
	h2("")
	def sections = []
	Store.base('usdlc').dirs(~/.*\.html$/) { String path ->
		def page = new Page(Store.base(path).text())
		page.each { html ->
			def section = [page: page.name]
			['id', 'state', 'pred', 'depend', 'owner', 'assignee', 'date', 'notes'].each { String sectionId ->
				section[it] = html["@$sectionId"]
			}
			sections << section
		}
	}
	doc.div(id: "s1", 'class': "editable section") {
	}
}
