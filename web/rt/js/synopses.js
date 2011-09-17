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
	$.extend(true, window.usdlc, {
		clearSynopses : function(sections) {
			$("div.inclusion", sections || usdlc.pageContents).remove()
			usdlc.deleteOutput()
		},
		doSynopses : function() {
			usdlc.clearSynopses()
			$("div.synopsis a[action]").each(function() {
				var link = $(this)
				var action = link.attr("action")
				if (action == 'runnable') {
					loadSynopsis(link, usdlc.displaySource)
				} else {
					loadSynopsis(link, function(wrapper, data) {
						var html = $("<div/>").html(data).children("div.section").first().children()
						wrapper.append(html)
					})
				}
			})
		},
		showSynopsis : function(section) {
			$('div.inclusion', section).show()
		},
		hideSynopsis : function(section) {
			$('div.inclusion', section).hide()
		},
		checkForSynopsis : function(section) {
			if ($("a[action]", section).size() > 0) {
				section.addClass('synopsis')
			} else {
				section.removeClass('synopsis')
			}
		}
	})

	function loadSynopsis(link, processor) {
		var path = link.attr('href')
		if (path.indexOf('..') == -1  && path.indexOf('://') == -1) {
			path = usdlc.normalizeURL(link.get(0).pathname)
			var section = link.parents('div.synopsis')
			var inclusion = $('<div/>').addClass('inclusion')
			section.append(inclusion)
			$.get(usdlc.serverActionUrl(path, 'raw'), function(data) {
				if (data.length < 3) {
					data = ''
				}
				processor(inclusion, data, path)
			})
		}
	}
	// usdlc.contentTree.bind('after_open.jstree after_close.jstree', onResize)
})
