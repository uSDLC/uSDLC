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
	usdlc.pageContents = $('div#pageContents')

	$.extend(true, window.usdlc, {
		/**
		 * Called by the usdlc.server.servletengine.server to highlight the page selection.
		 * @param colour Colour to highlight the section with.
		 */
		highlight : function (colour, section) {
			if (! section) {
				section = usdlc.inFocus
			}

			function moveBackground(to, onComplete) {
				if (section) {
					section.animate(
					{ backgroundPosition: to + "px 0"}, {
						duration: 1000,
						complete: onComplete
					})
				}
			}

			moveBackground(-20, function() {
				var url = colour ? 'url(/rt/gradients/' + colour + '-right.png)' : "none"
				section.css('background-image', url)
				moveBackground(-10, null)
			})
		},
		/*
		 Each paragraph is a focus element. Highlight it if clicked on or otherwise referenced to.
		 */
		setFocus : function(element) {
			var section = $(element || usdlc.inFocus)
			if (!section.hasClass("inFocus")) {
				usdlc.clearFocus()
				usdlc.inFocus = section.addClass('inFocus ui-state-highlight')
				usdlc.contentTree.jstree('disable_hotkeys')
				return true //yup - we changed focus
			}
			return false    // was already in focus
		},
		clearFocus : function() {
			if (! usdlc.inFocus) {
				return
			}
			var lostFocus = $('.inFocus').removeClass('inFocus ui-state-highlight')
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
				return false    // doesn't have focus of context menu up
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
				if (! focus.length) {
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
				if (! focus.length) {
					focus = $('div.section:first')
				}
				usdlc.setFocus(focus)
				return false
			}
			return true
		}
	})

	//Anything that has focus can be edited.
	$('.editable').
		css('background-image', "none").
		live('click',
		function(ev) {
			usdlc.setFocus(ev.currentTarget)
			return false
		}).
		live('contextmenu',
		function(ev) {
			usdlc.setFocus(ev.currentTarget)
		})
	// Alt key press and release triggers section menu
	$(document).bind('keydown',
		function(event) {
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
