/*
 Copyright 2011 the Authors for http://usdlc.net

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

$(function() {
	$.extend(true, window.usdlc, {
		cleanSections : function(sections) {
			// clean up html of crap that builds up
			sections.removeAttr('style')
			.removeClass('inFocus hidden ui-state-highlight ui-widget-content')
			.filter('.synopsis').children().removeAttr('style')
			$('div[href]', sections).html('')
			$('*[activate]', sections).html('')
			$('div#myEventWatcherDiv').remove()
			usdlc.clearSynopses(sections)
			usdlc.screencast.close()
		},
		moveSectionUp : function() {
			if (usdlc.inFocus) {
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
			if (usdlc.inFocus) {
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
			if (usdlc.inFocus) {
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
			if (usdlc.inFocus) {
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
			if (!usdlc.inFocus) {
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
			pieces.wrapInner(function() {
				return usdlc.newSection('s' + (id++))
			}).children().insertAfter(usdlc.inFocus)
			pieces.remove()
			usdlc.savePage()
			return false
		},
		/**
		 * Called by the context menu - causing the section in focus to be
		 * created as a new page.
		 */
		extractSectionInFocus : function() {
			if (!usdlc.inFocus) {
				return false
			}
			var section = usdlc.parseSection(usdlc.inFocus)
			var newPageName = section.name + '/index.html'
			header = usdlc.createPageTitle(section.title, section.subtitle).replaceAll(section.header).after(
					$('<div/>').attr('id', 's1').addClass('editable section')
							.append(section.content))
			usdlc.save(newPageName, usdlc.inFocus.html())
			usdlc.inFocus.addClass('synopsis').empty().append(
					$('<h1/>').append(
							$('<a/>').attr('href', section.name + '/index.html').attr('id', section.id + 'a0')
									.addClass('usdlc').attr('action', 'page').text(section.title)))
			usdlc.savePage()
			return true
		}
	})

	// todo: reinstate with special key down only - stops editor from blurring
	// Make it so we can move sections with drag and drop.
	// usdlc.pageContents.sortable({
	// axis : 'y',
	// containment: 'parent',
	// items : 'div.section',
	// revert : true,
	// placeholder : "ui-state-highlight",
	// tolerance : 'pointer',
	// opacity : 0.5,
	// start : function(event) {
	// usdlc.setFocus($(event.srcElement))
	// },
	// update : function() {
	// usdlc.savePage()
	// }
	// })
})
