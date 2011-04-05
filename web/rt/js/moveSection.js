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

$(function() {
	$.extend(true, window.usdlc, {
		/**
		 * Parse section title, subtitle, content, id and name (camel-case)
		 * @param section
		 */
		parseSection : function(section) {
			var header = section.children().first()
			var title = header.text()
			section.data = {
				title : title,
				subtitle : '...',
				content : header.nextAll(),
				id : section.attr('id'),
				name : usdlc.camelCase(title),
				header : header
			}
			if (section.data.title.length > 32) {
				section.data.subtitle = section.data.title
				section.data.title = section.data.title.substring(0, 32)
				section.data.name = usdlc.camelCase(section.data.title)
			}
			return section.data
		},
		/**
		 * Used in a menu to insert a section above the page selection then edit it.
		 */
		insertSectionAboveFocus : function() {
			insertSection("insertBefore")
		},
		/**
		 * Used in a menu to insert a section below the page selection then edit it.
		 */
		insertSectionBelowFocus : function() {
			insertSection("insertAfter")
		},
		cleanSections : function(sections) {
			// clean up html of crap that builds up
			sections.removeAttr('style').removeClass('inFocus ui-state-highlight').
				filter('.synopsis').children().removeAttr('style')
			usdlc.clearRunningLinkClass(sections)
			usdlc.clearSynopses(sections)
		},
		moveSectionUp : function() {
			if (usdlc.hasFocus()) {
				var swap = usdlc.inFocus.prev()
				if (swap.length) {
					usdlc.inFocus.insertBefore(swap)
					usdlc.savePage()
				}
				return false
			}
			return true
		},
		moveSectionDown : function() {
			if (usdlc.hasFocus()) {
				var swap = usdlc.inFocus.next()
				if (swap.length) {
					usdlc.inFocus.insertAfter(swap)
					usdlc.savePage()
				}
				return false
			}
			return true
		},
		joinSectionAbove : function() {
			if (usdlc.hasFocus()) {
				var above = usdlc.inFocus.prev()
				if (above.length) {
					usdlc.inFocus.prepend(above.children())
					above.remove()
					usdlc.savePage()
				}
				return false
			}
			return true
		},
		joinSectionBelow : function() {
			if (usdlc.hasFocus()) {
				var below = usdlc.inFocus.next()
				if (below.length) {
					usdlc.inFocus.append(below.children())
					below.remove()
					usdlc.savePage()
				}
				return false
			}
			return true
		},
		splitIntoSections : function() {
			if (! usdlc.inFocus) {
				return true
			}
			var pieces = usdlc.inFocus.find('li')
			if (pieces.length <= 1) {
				pieces = usdlc.inFocus.find('p')
			}
			if (pieces.length <= 1) {
				pieces = usdlc.inFocus.find('div')
			}
			var id = parseInt(usdlc.nextSectionId().substring(1))
			pieces.wrapInner(
				function() {
					return newSection('s' + (id++))
				}).children().insertAfter(usdlc.inFocus)
			pieces.remove()
			usdlc.savePage()
			return false
		},
		/**
		 * Called by the context menu - causing the section in focus to be created as a new page.
		 */
		extractSectionInFocus : function() {
			if (! usdlc.inFocus) {
				return false
			}
			var section = usdlc.parseSection(usdlc.inFocus)
			var newPageName = section.name + '/index.html'
			header = usdlc.createPageTitle(section.title, section.subtitle).replaceAll(section.header).after($('<div/>').attr('id', 's1').addClass('editable section').attr('contextmenu', "section").append(section.content))
			usdlc.save(newPageName, usdlc.inFocus.html())
			usdlc.inFocus.addClass('synopsis').empty().append($('<h1/>').append($('<a/>').attr('href', section.name + '/index.html').attr('id', section.id + 'a0').addClass('usdlc').attr('action', 'page').text(section.title)))
			usdlc.savePageContents()
			return true
		}
	})

	// Make it so we can move sections with drag and drop.
	usdlc.pageContents.sortable({
		axis : 'y',
		containment: 'parent',
		items : 'div.section',
		revert : true,
		placeholder : "ui-state-highlight",
		tolerance : 'pointer',
		opacity : 0.5,
		start : function(event) {
			usdlc.setFocus($(event.srcElement).parents('div.section'))
		},
		update : function() {
			usdlc.savePage()
		}
	})
	/*
	 Insert a new paragraph above or below the one in focus.
	 */
	function insertSection(beforeOrAfter) {
		$('.inFocus').each(function() {
			var ne = newSection()
			ne[beforeOrAfter](this)
			usdlc.setFocus(ne)
			usdlc.editSectionInFocus()
		})
	}

	function newSection(id) {
		return $("<div/>", {
			id: id || usdlc.nextSectionId(),
			'class': 'editable section',
			contextMenu: 'section'
		})
	}
})
