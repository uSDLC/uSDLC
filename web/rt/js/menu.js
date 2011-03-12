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

/**
 * User: Paul
 * Date: 5/01/11
 * Time: 8:28 AM
 */
$(function() {
	setContextMenus()
	setButtonBars()

	/**
	 Generate context menus. We trap the right-click and if any element clicked on or one of it's parents has
	 a contextMenu attribute, use it's value to decide which context menu to display at the mouse cursor.
	 */
	function setContextMenus() {
		var contextMenus = {}

		usdlc.contextMenu = null
		usdlc.onContextMenu = function(target, top, left) {
			var contextMenuName = target.attr('contextMenu')
			if (! contextMenuName) {
				target = target.parents('[contextMenu]')
				if (target.length) {
					contextMenuName = $(target[0]).attr('contextMenu')
				}
			}
			contextMenuName = 'menu' + contextMenuName
			if (contextMenus[contextMenuName]) {
				var div = contextMenus[contextMenuName].css({ top : top, left : left })
				var menuData = div.contextMenuData.clone()

				function hotkey(index) {
					if (index < 10) {
						$('<kbd>(^' + (index || 'V') + ')</kbd>')
					}
				}

				var pasteList = $('ul#pasteList', menuData)

				function clickEvent(event) {
					usdlc.paste(pasteList.index(event.target))
					return false
				}

				pasteList.append($('div#pasteList').children().clone().bind('click', clickEvent).append(hotkey).wrap('<li/>'))

				div.contextMenu = new Menu(div, {
					content: menuData,
					maxHeight: 400,
					flyOut: false,
					backLink: false
				});
				div.contextMenu.menuExists = false
				div.contextMenu.currentTarget = target
				div.contextMenu.showMenu()
				usdlc.contextMenu = div
			}
		}

		function onContextMenu(ev) {
			usdlc.onContextMenu($(ev.currentTarget), ev.clientY, ev.clientX)
			return false
		}

		function onDblclick(ev) {
			usdlc.setFocus(ev.currentTarget)
			if (usdlc.editMode) {
				usdlc.editSectionInFocus()
			} else {
				usdlc.runSectionInFocus()
			}
			return false
		}

		$('.editable').live('contextmenu', onContextMenu).live('dblclick', onDblclick)
		/*
		 Look for elements with a class of contextMenu and create menus to be called on demand.
		 */
		$('div.contextMenu').each(function() {
			var div = $(this)
			var contextMenuName = div.attr('id')
			contextMenus[contextMenuName.toLowerCase()] = div

			$.get('/rt/' + contextMenuName + '.html.groovy', function(data) {
				div.contextMenuData = $(data)
				var doc = $(document)
				$('a kbd', div.contextMenuData).each(function() {
					var kbd = $(this)
					var key = kbd.text()
					key = key.substring(1, key.length - 1)
					if (key[0] == '^') {
						key = 'ctrl+' + key.substring(1)
					}
					var a = kbd.parents('a')
					doc.bind('keydown', key, function(event) {
						a.attr('onclick')(event)
						return false
					})
				})
			})
		})
	}

	function setButtonBars() {
		$('span.toolbar').each(function() {
			var toolbar = $(this)
			$.get('/rt/' + toolbar.attr('id') + '.html.groovy', function(data) {
				toolbar.html(data)
				$('span.buttonset', toolbar).buttonset()
				$('button', toolbar).each(function() {
					var button = $(this)
					var icons = button.attr('icon').split(',')
					var init = {text : !!$.trim(button.innerHTML)}
					if (icons) {
						init.icons = {}
						init.icons.primary = 'ui-icon-' + icons[0]
						if (icons.length > 1) {
							init.icons.secondary = 'ui-icon-' + icons[1]
						}
					}
					button.button(init)
				})
			})
		})
	}
})
