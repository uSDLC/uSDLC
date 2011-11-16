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
	pageLayout : function() {
		setPageLayout()
		$(window).load(setPageLayout).resize(setPageLayout)
		setModalMask()

		function setModalMask() {
			var modalMask = $("<div/>").attr('id', 'modalMask').hide()
			$('body').append(modalMask)
		}

		/**
		 * Size the main sections of a page.
		 */
		function setPageLayout() {
			var w = $(window)
			var ptt = $('table#pageTitleTable')
			usdlc.pageContents = $('div#pageContents')
			var ct = $('#contentTree')
			usdlc.titleVisible = function() {
				return ptt.css('margin-top') == '0px'
			}
			var treeHider = $('img#treeHider').fadeTo(1, 0.3)
			usdlc.treeVisible = function() {
				return treeHider.offset().left > 0
			}
			var lastScrollTop = 0
			function setViewportHeight() {
				var pad = usdlc.pageContents.outerHeight() - usdlc.pageContents.height()
				if (pad < 0 || pad > 50)
					pad = 25
				var viewPortHeight = w.height() - ptt.outerHeight() - pad
				viewPortHeight += usdlc.titleVisible() ? 0 : 50
				var aboveScroll = (viewPortHeight > 800) ? 100 : 0
				var belowScroll = viewPortHeight - aboveScroll
				usdlc.pageContents.outerHeight(viewPortHeight)
				ct.outerHeight(viewPortHeight)
				usdlc.init.decoratePage && usdlc.init.decoratePage()
				usdlc.scrollTo = function(element) {
					lastScrollTop = usdlc.pageContents.parent().scrollTop()
					var elementHeight = element.outerHeight()
					var elementTop = 0
					element.prevAll('.section').each(function() {
						elementTop += $(this).outerHeight()
					})
					var newTop = (elementHeight > belowScroll) ?
							elementTop : (elementTop - aboveScroll)
					usdlc.pageContents.scrollTop(newTop)
					usdlc.setFocus(element)
				}
				usdlc.scrollTop = function() {
					usdlc.pageContents.scrollTop(0)
				}
			}
			setViewportHeight()
			$('#pageContentsTable').css('maxWidth', w.width())
			usdlc.toggleHideTitle = function() {
				ptt.animate({
					'margin-top' : usdlc.titleVisible() ? -50 : 0
				}, setPageLayout)
			}
			$('img#pageTitleImage').unbind().bind('click', usdlc.toggleHideTitle)
			var pct = $('table#pageContentsTable')
			var ctt = $('div#contentTree')
			usdlc.toggleHideTree = function() {
				var left = treeHider.offset().left
				left = left <= 0 ? 0 : -left
				pct.animate({
					'margin-left' : left
				})
			}
			treeHider.unbind().bind('click', usdlc.toggleHideTree)
			$('body').delegate('.hideOnHover', 'mouseenter', function() {
				var element = $(this)
				element.hide()
				setTimeout(function(){element.show()}, 5000)
			})

			usdlc.scrollBack = function() {
				usdlc.pageContents.scrollTop(lastScrollTop)
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
			content : function(i, $page) {
				var title = usdlc.parseSection($page).title
				return '<span class="red-box rounded menu sausage-span">' + title + '</span>';
			}
		})

		usdlc.scrollFiller = function(on) {
			if (on) {
				if ($('div.scrollFiller').size() === 0)
					usdlc.pageContents.append($("<div/>").height(
							usdlc.pageContents.height() * 0.6).addClass(
							'scrollFiller'))
			} else
				$('div.scrollFiller').remove()
		}
		usdlc.scrollFiller(true)
	},
	loadPage : function(callback) {
		/**
		 * When you open uSDLC without asking for a page, the last page
		 * displayed will return. Use /frontPage.html to get to the true root.
		 */
		var path = window.location.pathname
		if (path == usdlc.urlBase + '/') {
			path = window.location.search || usdlc.cookie('currentPage') || '/frontPage.html'
			if (path[0] == '?')
				path = path.substring(1)
		}
		usdlc.absolutePageContents(path, callback)
	},
	finalise : function() {
		$('div[href]').each(function() {
			usdlc.elementLoader($(this))
		})
		// move from do-nothing to do-it-all and then generate for already
		// loaded first page.
		usdlc.synopses = usdlc.doSynopses
		usdlc.synopses()
	},
	postLoader: function() {
		usdlc.loadScriptAsync('/rt/js/usdlc_postload_js.groovy', usdlc.init.finalise)
	}
}
