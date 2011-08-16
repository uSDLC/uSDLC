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
		/**
		 * Called by the usdlc.server.servletengine.server to highlight the page
		 * selection.
		 */
		highlight : function(colour, element) {
			var section = usdlc.getSection(element)
			if (section) {
				if (colour) {
					var url = 'url(/rt/gradients/' + colour + '-right.png)'
					section.css({
						backgroundImage : url,
						backgroundPosition : -10
					})
				} else {
					section.css({
						backgroundImage : 'none'
					})
				}
			}
		},
		/**
		 * Given an element (null for current focus), retrieve the section containing it.
		 */
		getSection : function(element) {
			var section = $(element || usdlc.inFocus)
			if (!section.hasClass('section'))
				section = section.parents('div.section')
			return section
		},
		/**
		 * Each paragraph is a focus element. Highlight it if clicked on or
		 * otherwise referenced to.
		 */
		setFocus : function(element) {
			var section = usdlc.getSection(element)
			if (!section.hasClass("inFocus")) {
				usdlc.clearFocus()
				usdlc.inFocus = section.addClass('inFocus ui-state-highlight')
				usdlc.contentTree.jstree('disable_hotkeys')
				usdlc.pageContentsSausages.sausage("setFocus", section)
				return true // yup - we changed focus
			}
			return false // was already in focus
		},
		clearFocus : function() {
			if (!usdlc.inFocus) {
				return
			}
			$('.inFocus').removeClass('inFocus ui-state-highlight')
			usdlc.lastFocus = usdlc.inFocus
			usdlc.inFocus = null
			usdlc.contentTree.jstree('enable_hotkeys')
		},
		toggleFocus : function() {
			if (usdlc.inFocus) {
				usdlc.clearFocus()
			} else {
				usdlc.setFocus(usdlc.lastFocus || $('div.section:first'))
			}
		},
		inFocus : null,
		lastFocus : null,
		hasFocus : function() {
			if (usdlc.contextMenu && usdlc.contextMenu.is(":visible")) {
				return false // doesn't have focus of context menu up
			}
			return usdlc.inFocus
		},
		nextSectionId : function() {
			var id = $('.section').length + 1
			while ($('div#s' + id).length > 0) {
				id++;
			}
			return 's' + id
		},
		upFocus : function() {
			if (usdlc.hasFocus()) {
				var focus = usdlc.inFocus.prev()
				if (!focus.length) {
					focus = $('div.section:last')
				}
				usdlc.setFocus(focus)
				return false
			}
			return true
		},
		downFocus : function() {
			if (usdlc.hasFocus()) {
				var focus = usdlc.inFocus.next()
				if (!focus.length) {
					focus = $('div.section:first')
				}
				usdlc.setFocus(focus)
				return false
			}
			return true
		},
		/**
		 * Parse section title, subtitle, content, id and name (camel-case)
		 * 
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
		}
	})

	// Anything that has focus can be edited.
	$('.editable').css('background-image', "none").live('click', function(ev) {
		usdlc.setFocus(ev.currentTarget)
		return false
	}).live('contextmenu', function(ev) {
		usdlc.setFocus(ev.currentTarget)
	})
	// Alt key press and release triggers section menu
	$(document).bind('keydown', function(event) {
		usdlc.inAlt = (event.altKey && event.keyCode == 18 && usdlc.inFocus)
		return true
	}).bind('keyup', function(event) {
		if (event.keyCode == 18 && usdlc.inAlt && usdlc.inFocus) {
			var offset = usdlc.inFocus.offset()
			usdlc.onContextMenu(usdlc.inFocus, offset.top, offset.left + usdlc.inFocus.width() / 2)
		}
		usdlc.inAlt = false
		return true
	})
})
