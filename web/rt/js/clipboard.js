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
		copySectionInFocus : function(cutOrCopy) {
			var toCut = usdlc.inFocus
			if (toCut) {
				var toMove = {}, toMoveList = [
				]
				toCut.find('a.usdlc').each(function() {
					var link = $(this)
					if (link.parents('div.synopsis').length === 0) {
						var href = link.attr('href')
						var relative = (href[0] != '/')
						if (relative) {
							var dir = href.substring(0, href.indexOf('/'))
							var hrefMatchedLinkSections = $('a[href^=' + dir).parents('div.section')
							if (hrefMatchedLinkSections.not(toCut.get(0)).size() === 0) {
								if (! (dir in toMove)) {
									toMove[dir] = dir
									toMoveList.push(dir)
								}
							}
						}
					}
				})
				usdlc.modalOn()
				usdlc.cleanSections(toCut)
				var focus = toCut.next()
				var html = $('<div/>').html(toCut.clone()).html()
				$.post(usdlc.serverActionUrl(usdlc.pageContentsURL, cutOrCopy + '&dependents=' + toMoveList.join(',') + "&title=" + usdlc.parseSection(toCut).name + "&mimeType=application/javascript"), html, $.globalEval)
				usdlc.sectionBeingCut = toCut
				usdlc.modalOff()
				usdlc.setFocus(focus)
				return true
			}
			return false
		},
		copySectionSuccessful : function(title, href) {
			var toCut = usdlc.sectionBeingCut
			if (toCut) {
				usdlc.sectionBeingCut = null
				// update paste list and keys
				$('div#pasteList').prepend($('<a/>', { html : title, href : href }))
				usdlc.setFocus()
			}
			return toCut
		},
		cutSectionSuccessful : function(title, href) {
			var toCut = usdlc.copySectionSuccessful(title, href)
			if (toCut) {
				toCut.remove()
				usdlc.savePage()
			}
		},
		paste : function(idx) {
			var clip = $('div#pasteList a').eq(idx).remove()
			if (clip.length == 1) {
				$.post(usdlc.serverActionUrl(usdlc.pageContentsURL, 'paste&from=' + clip[0].pathname), function(data) {
					var id = usdlc.nextSectionId()
					var section = $(data).attr('id', id)
					id += 'a'
					var idx = 0
					$('a.usdlc', section).attr('id', function() {
						return id + (idx++)
					})
					section.insertAfter(usdlc.inFocus || $('div.section:last'))
					usdlc.savePage()
					usdlc.setFocus(section.prev())
				})
			}
		}
	})

	// bind paste keys
	var doc = $(document)
	doc.bind('keydown', 'ctrl+V', function() {
		usdlc.paste(0)
	})
	for (digit = 1; digit < 10; digit++) {
		doc.bind('keydown', 'ctrl+' + digit, function() {
			usdlc.paste(digit)
		})
	}
})
