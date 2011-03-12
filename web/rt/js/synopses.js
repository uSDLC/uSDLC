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
	usdlc.pageContents = $('div#pageContents')

	$.extend(true, window.usdlc, {
		clearSynopses : function(sections) {
			$("div.inclusion", sections || usdlc.pageContents).remove()
		},
		synopses : function() {
			usdlc.clearSynopses()
			$("div.synopsis a[action=page]").each(function() {
				var link = $(this)
				var path = usdlc.normalizeURL(link.get(0).pathname)
				$.get(usdlc.serverActionUrl(path, 'raw'), function(data) {
					if (data.length < 3) {
						data = link.html()
					} else {
						var html = $("<div/>").html(data)
						data = html.children("div#pageTitle").children().
							add(html.children("div.section").first().children())
					}
					var section = link.parents('div.synopsis')
					section.append($('<div/>').addClass('inclusion').append(data))
					usdlc.showSynopsis(section)
				})
			})
		},
		showSynopsis : function(section) {
			section.children().hide()
			$('div.inclusion', section).show()
		},
		hideSynopsis : function(section) {
			section.children().show()
			$('div.inclusion', section).hide()
		}
	})
})
