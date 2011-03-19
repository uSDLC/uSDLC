/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(function() {
	window.usdlc = {
		/**
		 Create a dialog box to wrap an existing item.
		 Options are those for jquery dialog, but height can be a percentage as well as a pixel count
		 */
		dialog : function(contents, options) {
			contents = $(contents)
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
		var pc = $('table#pageContentsTable')
		var pcd = $('div#pageContents')
		var pad = pc.outerHeight() - pcd.height()
		var h = w.height() - ptt.outerHeight() - pad
		pcd.outerHeight(h)
		var pcdl = $('div#pageContentSlider')
		pcdl.outerHeight(h - 20)
		/*
		 What to do when the slider moves up and down - scrolling.
		 */
		usdlc.pageContentSlider = $("div#pageContentSlider").slider({
			orientation: "vertical",
			animate: true,
			value: 100,
			change: function(e, ui) {
				var maxScroll = pcd.attr("scrollHeight") - pcd.height();
				pcd.animate({scrollTop: (100 - ui.value) * (maxScroll / 100) }, 400);
			},
			slide: function(e, ui) {
				var maxScroll = pcd.attr("scrollHeight") - pcd.height();
				pcd.attr({scrollTop: (100 - ui.value) * (maxScroll / 100) });
			}
		})
	}
})
