$(function () {
	setContextMenus()
	setButtonBars()

	/**
	 * Generate context menus. We trap the right-click and if any element
	 * clicked on or one of it's parents has a contextMenu attribute, use it's
	 * value to decide which context menu to display at the mouse cursor.
	 */
	function setContextMenus() {
		var contextMenus = {}

		usdlc.contextMenu = null
		usdlc.getContextMenu = function (target) {
			var contextMenuName = target.attr('contextMenu')
			if (usdlc.inEditor) {
				contextMenuName = 'sourceeditor'
			} else if (!contextMenuName) {
				target = target.parents('[contextMenu]')
				if (target.length) {
					contextMenuName = $(target[0]).attr('contextMenu')
				}
			}
			contextMenuName = 'menu' + contextMenuName
			return contextMenus[contextMenuName]
		}
		usdlc.onContextMenu = function (target, position) {
			var contextMenu = usdlc.getContextMenu(target)
			if (contextMenu) {
				if (position.left < 0) {
					position.left = contextMenu.width() - position.left
				}
				var div = contextMenu.css(position)
				var menuData = div.contextMenuData.clone()

				function hotkey(index) {
					if (index < 10) {
						var key = index || 'V'
						$('<kbd>(^' + key + ' meta-' + key + ')</kbd>')
					}
				}

				var pasteList = $('ul#pasteList', menuData)

				function clickEvent(event) {
					usdlc.paste(pasteList.index(event.target))
					return false
				}

				pasteList.append($('div#pasteList').children().clone().bind('click', clickEvent).append(hotkey).wrap(
						'<li/>'))

				div.contextMenu = new Menu(div, {
					content:  menuData,
					maxHeight:400,
					flyOut:   false,
					backLink: false
				});
				div.contextMenu.menuExists = false
				div.contextMenu.currentTarget = target
				div.contextMenu.showMenu()
				usdlc.contextMenu = div
			}
		}

		function onContextMenu(ev) {
			usdlc.onContextMenu($(ev.currentTarget),
					{top:ev.clientY, left:ev.clientX, right:'auto'})
			return false
		}

		usdlc.contextMenuOverIcon = function () {
			offset = usdlc.inFocus.offset()
			iconWidth = usdlc.menuIcon.width()
			position = {
				top: offset.top - iconWidth / 2,
				left:usdlc.menuIcon.offset().left
			}
			usdlc.onContextMenu(usdlc.inFocus, position)
			return false
		}

		function onDblclick(ev) {
			if (!usdlc.inEditor) {
				usdlc.setFocus(ev.currentTarget)
				usdlc.editSectionInFocus()
				return false
			} else {
				return true
			}
		}

		$('.editable').live('contextmenu', onContextMenu).live('dblclick', onDblclick)

		usdlc.loadContextMenu = function (element, data) {
			element.contextMenuData = $(data)
			var contextMenuName = element.attr('id').toLowerCase()
			var forSourceEditor = (contextMenuName == 'menusourceeditor')
			contextMenus[contextMenuName] = element
			var doc = $(document)
			$('a kbd', element.contextMenuData).each(function () {
				var kbd = $(this)
				var key = kbd.text()
				key = key.substring(1, key.length - 1)
				if (key[0] == '^') {
					key = 'ctrl+' + key.substring(1)
				}
				var a = kbd.parents('a')
				doc.bind('keydown', key, function (event) {
					if (!usdlc.inEditor == !forSourceEditor) {
						event.srcElement.usdlcKeyEvent = true
						a.click()
						event.preventDefault()
					}
				})
			})
		}
	}

	function setButtonBars() {
		$('span.toolbar').each(function () {
			var tb = $(this)
			$.get(usdlc.urlBase + '/rt/' + tb.attr('id'), function (data) {
				tb.html(data)
			})
		})
	}
})
