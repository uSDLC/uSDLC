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
	var percentRE = /^(\d+)%$/
	$.extend(true, window.usdlc, {
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
		}
	})
})
