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
			sourceLoads = 0
			$("div.inclusion", sections || usdlc.pageContents).remove()
		},
		synopses : function() {
			usdlc.clearSynopses()
			$("div.synopsis a[action=page]").each(function() {
				loadSynopsis($(this), function(data) {
					var html = $("<div/>").html(data)
					return html.children("div#pageTitle").children().
						add(html.children("div.section").first().children())
				})
			})
			$("div.synopsis a[action=runnable]").each(function() {
				sourceLoads++
				loadSynopsis($(this), function(data) {
					if (! --sourceLoads) {
						done = function() {
							done = function() {
							}
							prettyPrint()
						}
					}
					return $("<pre/>").addClass('prettyprint linenums').html(data)
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
		},
		checkForSynopsis : function(section) {
			if (isSynopsis($("a[action=page]", section), true) ||
				isSynopsis($("a[action=runnable]", section), false)) {
				section.addClass('synopsis')
			} else {
				section.removeClass('synopsis')
			}
		}
	})

	var sourceLoads = 0
	var done = function() {
	}

	function isSynopsis(link, above) {
		var count = link.size()
		if (count) {
			if (count == 1) {
				while (link && !link.hasClass('section')) {
					if ((above ? link.prevAll() : link.nextAll()).size()) {
						return false
					}
					link = link.parent()
				}
				return true
			}
		}
		return false
	}

	function loadSynopsis(link, processor) {
		var path = usdlc.normalizeURL(link.get(0).pathname)
		$.get(usdlc.serverActionUrl(path, 'raw'), function(data) {
			if (data.length < 3) {
				data = link.html()
			} else {
				data = processor(data)
			}
			var section = link.parents('div.synopsis')
			section.append($('<div/>').addClass('inclusion').append(data))
			usdlc.showSynopsis(section)
			done()
		})
	}
})
