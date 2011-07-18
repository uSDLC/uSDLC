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
usdlc.init = {
	queue : [],
	pageLayout: function() {
		setPageLayout()
		$(window).resize(setPageLayout).load(setPageLayout)
		setModalMask()

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
	},
	decoratePage : function() {
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
				if ($('div.scrollFiller').size() === 0)
					usdlc.pageContents.append($("<div/>").height(usdlc.pageContents.height() * 0.6).addClass('scrollFiller'))
			} else
				$('div.scrollFiller').remove()
		}
		usdlc.scrollFiller(true)
	},
	loadPage : function(callback) {
		/**
		 * When you open uSDLC without asking for a page, the last page displayed
		 * will return. /root is a special case so you can go to the uSDLC D3 root.
		 */
		var path = window.location.pathname
		if (path == '/') {
			path = usdlc.cookie('currentPage') || '/frontPage.html'
		} else if (path == '/root') {
			path = '/frontPage.html'
		}
		usdlc.absolutePageContents(path, callback)
	},
	finalise :	function() {
		$('div[href]').each(function() {
			usdlc.elementLoader($(this))
		})
		$(usdlc.init.queue).each(function() {
			this()
		})
	}
}

usdlc.synopses = function() {
	usdlc.init.queue.push(function() {
		usdlc.synopses()
	})
}