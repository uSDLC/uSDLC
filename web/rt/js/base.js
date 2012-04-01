$(function() {
	window.usdlc = {
		synopses : function(){}, //# we will provide one with teeth after
		/*
		 Given a path (from a URL), return the parent path - being the complete
		 directory structure without file name.
		 */
		parentPath : function(path) {
			lastSlash = path.lastIndexOf('/')
			if (lastSlash != -1) path = path.substring(0,lastSlash)
			return path
		},

		removeUrlBase : function(path) {
			b = usdlc.urlBase + '/'
			if (path.substring(0, b.length) == b)
				path = path.substring(b.length)
			return path
		},
	}
	usdlc.urlBase = usdlc.parentPath(window.location.pathname)
	window.CKEDITOR_BASEPATH = '/lib/ckeditor/'


	head = document.getElementsByTagName('head')[0]
	base = document.createElement('base')
	base.setAttribute('href', usdlc.urlBase)
	head.appendChild(base)

	var percentRE = /^(\d+)%$/
	$.extend(true, window.usdlc, {
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

			script.src = usdlc.urlBase + path
			head.appendChild(script)
		},
		/**
		 * Create a dialog box to wrap an existing item. Options are those for
		 * jquery dialog, but height can be a percentage as well as a pixel
		 * count
		 */
		dialog : function(contents, options) {
			contents = $(contents)
			options = options || {}
			var match = percentRE(options.height)
			if (match) {
				var percent = parseInt(match[1])
				options.height = $(window).height() * percent / 10.0
			}
			contents.dialog($.extend({
				show : "blind",
				hide : "explode"
			}, options))
			// dialog sets iframe width to auto - which does not fill the parent
			contents.css('width', '98%')
			return contents
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
			usdlc.window("help", "/rt/help/index.html", {})
		},
		cookie : function(key) {
			return $.cookie(key)
		},
		setCookie : function(key, value) {
			return $.cookie(key, value, {
				expires : 1000
			})
		},
		toggleShow : function() {
			$.each(arguments, function(index, value) {
				$(value).css('display', 'none')
			})
			$(arguments[0]).css('display', 'inherit')
		},
		setInnerHtml : function(element, data) {
			element.html(data)
		},
		camelCase : function(text) {
			return text.replace(/([\s:\?\*%\|"<>]+)(\w)/g, function(a, s, c) {
				return c.toUpperCase()
			})
		},
		elementLoader : function(element) {
			var href = element.attr('href')
			var action = element.attr('action') || "setInnerHtml"
			$.get(usdlc.urlBase + '/' + href, function(data) {
				usdlc[action](element, data)
			})
		},
		inEditMode: function(event) {
			var node = event.target.nodeName.toLowerCase()
			return node == 'textarea' || node == 'input' ||
					$(event.target).children('.CodeMirror').size() > 0
		}
	})
})
