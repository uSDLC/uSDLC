/*
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
	window.usdlc = {
		/**
		 Create a dialog box to wrap an existing item.
		 Options are those for jquery dialog, but height can be a percentage as well as a pixel count
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
				show: "blind",
				hide: "explode"
			}, options))
			// dialog sets iframe width to auto - which does not fill the parent
			contents.css('width', '98%')
			return contents
		},
		/*
		 Modalise for editors
		 */
		modalOn : function(box) {
			usdlc.maskEverything(0.2)
			if (box) {
				box.css('z-index', 9999).show('slow')
			}
		},
		modalOff: function(box, afterwards) {
			$('#modalMask').hide('slow')
			if (box) {
				box.css('z-index', 0).hide('slow', afterwards)
			}
		},
		/**
		 * Call page-sensitive help. Goes from referrer to root looking for a help directory.
		 */
		help: function() {
			usdlc.window("help", "/rt/help/index.html", {})
		},
		cookie: function(key) {
			return $.cookie(key)
		},
		setCookie: function(key, value) {
			return $.cookie(key, value, {expires : 1000})
		},
		toggleShow: function() {
			$.each(arguments, function(index, value) {
				$(value).css('display', 'none')
			})
			$(arguments[0]).css('display', 'inherit')
		}
	}
	$('div[href]').each(function() {
		loadInclude(this)
	})
	setPageLayout()
	$(window).resize(setPageLayout).load(setPageLayout)
	setModalMask()

	var percentRE = /^(\d+)%$/

	function loadInclude(element) {
		div = $(element)
		var href = div.attr('href')
		$.get(href, function(data) {
			div.html(data)
		})
	}

	function setModalMask() {
		var modalMask = $("<div/>").attr('id', 'modalMask').hide()
		$('body').append(modalMask)
	}

	/**
	 Size the main sections of a page.
	 */
	function setPageLayout() {
		var w = $(window)
		var ptt = $('table#pageTitleTable')
		usdlc.pageContents = $('div#pageContents')
		var pad = 25 // damn ie pc.outerHeight() - usdlc.pageContents.height()
		var viewPortHeight = w.height() - ptt.outerHeight() - pad
		var aboveScroll = (viewPortHeight > 500) ? 100 : 50
		var belowScroll = viewPortHeight - aboveScroll
		usdlc.pageContents.outerHeight(viewPortHeight)
		$('#contentTree').outerHeight(viewPortHeight)
		$('#pageContentsTable').css('maxWidth', w.width())
		usdlc.pageContentsSausages = $('td#pageContentsSausages')
		usdlc.pageContentsSausages.sausage({
			container : usdlc.pageContents,
			page : function() {
				return usdlc.pageContents.find('div.section')
			},
			scrollTo : usdlc.scrollTo,
			content: function (i, $page) {
				var title = usdlc.parseSection($page).title
				return '<span class="sausage-span">' + title + '</span>';
			}
		})
		usdlc.scrollFiller = function(on) {
			if (on) {
				if ($('div.scrollFiller').size() === 0) {
					usdlc.pageContents.append($("<div/>").height(usdlc.pageContents.height() * 0.6).addClass('scrollFiller'))
				}
			} else {
				$('div.scrollFiller').remove()
			}
		}
		usdlc.scrollFiller(true)

		usdlc.scrollTo = function(element) {
			lastScrollTop = usdlc.pageContents.parent().scrollTop()
			var elementHeight = element.outerHeight()
			var elementTop = 0
			element.prevAll('.section').each(function() {
				elementTop += $(this).outerHeight()
			})
			var newTop = (elementHeight > belowScroll) ? elementTop : (elementTop - aboveScroll)
			usdlc.pageContents.scrollTop(newTop)
			usdlc.setFocus(element)
		}
		var lastScrollTop = 0

		usdlc.scrollBack = function() {
			usdlc.pageContents.animate({scrollTop: lastScrollTop}, 400)
		}
	}
})
