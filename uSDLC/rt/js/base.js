$(function() {
	window.usdlc = {
		context : 'Section',
		synopses : function(){}, //# we will provide one with teeth after
		loadActorTypes : function() {},
		/*
		 Given a path (from a URL), return the parent path - being the complete
		 directory structure without file name.
		 */
		parentPath : function(path) {
			lastSlash = path.lastIndexOf('/')
			if (lastSlash != -1) path = path.substring(0,lastSlash)
			return path
		}
	}
	window.CKEDITOR_BASEPATH = '/usdlc/lib/ckeditor/'

	$('head').prepend($('<base href="/"/>'))

	$.extend(true, window.usdlc, {
		setContext : function(context) {
			usdlc.context = context
			if (usdlc.setMenuContext) {
				usdlc.setMenuContext(context+'_Menu')
			}
		},
		loadScriptAsync : function(path, onScriptLoaded) {
			script = document.createElement("script")
			script.type = "text/javascript"
			script.async = "async"

			if (script.readyState) { // # IE
				script.onreadystatechange = function() {
					if (script.readyState == "loaded" ||
							script.readyState == "complete") {
						script.onreadystatechange = null;
						onScriptLoaded(path);
					}
				}
			} else { // # Other browsers
				script.onload = function() {
					onScriptLoaded(path);
				}
			}

			script.src = path
			head = document.getElementsByTagName('head')[0]
			head.appendChild(script)
		},
		/*
		 * Modalise for editors
		 */
		modalOn : function(box) {
			usdlc.maskEverything(0.2)
			if (box) {
				box.css('z-index', 9999).show('slow')
			}
		},
		modalOff : function(box, afterwards) {
			$('#modalMask').hide('slow')
			if (box) {
				box.css('z-index', 0).hide('slow', afterwards)
			}
		},
		/**
		 * Call page-sensitive help. Goes from referrer to root looking for a
		 * help directory.
		 */
		help : function() {
			usdlc.window("help", "/usdlc/rt/help/index.html", {})
		},
		cookie : function(key) {
			return $.cookie(key)
		},
		setCookie : function(key, value) {
			return $.cookie(key, value, {
				expires : 1000
			})
		},
//		toggleShow : function() {
//			$.each(arguments, function(index, value) {
//				$(value).css('display', 'none')
//			})
//			$(arguments[0]).css('display', 'inherit')
//		},
		camelCase : function(text) {
			text = text.replace(/[\s:\?\*%\|"<>\-~]+$/, '')
			return text.replace(/([\s:\?\*%\|"<>\-~]+)(\w)/g,
					function(a, s, c) { return c.toUpperCase() })
		},
		decamel : function(text) {
			return text.replace(/(\w)([A-Z])/g,
					function(a, c1, c2) { return c1 + ' ' + c2 })
		},
		inEditMode: function(event) {
			var node = event.target.nodeName.toLowerCase()
			return node == 'textarea' || node == 'input' ||
					$(event.target).children('.CodeMirror').size() > 0
		},
		goHome: function() {
			usdlc.absolutePageContents("/usdlc/home",
					function() {
						usdlc.contentTree.jstree('deselect_all')
						usdlc.contentTree.jstree('refresh')
					})
		},
		setInnerHtml : function(element, data) { element.html(data) },
		globalEval: function(element, script) { $.globalEval(script) },
		/**
		 * Run through all elements asking for action
		 * div[href] actions can be setInnerHTML, globalEval or loadMainMenu
		 * div[activate=xxx] calls usdlc.activation.xxx(element)
		 */
		activateHtml : function(html) {
			$('div[href]', html).each(function() {
				var element = $(this)
				var href = element.attr('href')
				var action = element.attr('action') || "setInnerHtml"
				$.get(href, function(data) {
					usdlc[action](element, data)
				})
			})
			if (usdlc.pageContentsURL.indexOf('/Configuration/Templates/') == -1) {
				$('*[activate]', html).each(function() {
					var element = $(this)
					if (!element.hasClass('template')) {
						var action = element.attr('activate')
						try {
							usdlc[action](element)
						} catch(e) {
							usdlc.log("error:activate "+action+" -- " + e)
						}
					}
				})
			}
		}
	})
	usdlc.persist = function(key, to) {
		var data = ''
		try {
			if (localStorage) {
				data = localStorage.getItem(key)
				if (to != undefined) {
					localStorage.setItem(key, to)
				}
			}
		} catch(e) {}
		return data ? data : ''
	}
})
