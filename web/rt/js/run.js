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
	$.extend(true, window.usdlc, {
		passed: function() {
			usdlc.highlight('green')
		},
		failed: function() {
			usdlc.highlight('red')
		},
		/**
		 * Command to run the whole page - as in from drop-down in header or a link/button
		 */
		runPage: function() {
			runSections($('div.section'))
		},
		linkAction: function(link) {
			var results = link.data('results')
			if (results) {
				results.dialog("open")
			} else {
				runLink(link)
			}
		},
		runLink: runLink,
		/**
		 * Command to run the page section.
		 */
		runSectionInFocus: function() {
			var section = $('.inFocus')
			runSections(section)
		},
		/**
		 * Command to run a section of the page or from the page section to the end of the page.
		 */
		runFromSectionInFocus: function() {
			var section = $('.inFocus')
			runSections(section.nextAll('.section').andSelf())
		},
		/**
		 * When in run mode, hovering over a link will display the results rather than edit or go to the page/runnable.
		 * @param link Link clicked on.
		 */
		displayResults: function(link) {
			var results = link.data('results')
			if (results) {
				results.dialog('show')
			}
		},

		runningLinkClass: function(links, name) {
			$(links).removeClass('runOnClick hasResults running error ok').addClass(name)
		},
		clearRunningLinkClass: function(inside) {
			$('a.usdlc', inside || usdlc.pageContents).removeClass('runOnClick hasResults running error ok')
		}
	})

	function enqueue(link, options, action) {
		var a = link.get(0)
		var search = (a.search || '?') + '&linkId=' + link.attr('id')
		usdlc.queue(action || $.ajax, $.extend({}, {
			cache : false,
			parallel : true,
			processData : false,
			type : 'GET',
			url : a.pathname + search,
			success : function(data) {
				usdlc.runningLinkClass(link, 'hasResults')
				var rdb = results(link).html(data)
				if (rdb.text().length > 5) {
					rdb.dialog((options && options.dialogCommand) || 'open')
				} else {
					usdlc.runningLinkClass(link, 'ok')
				}
			},
			error : function(xmlHttpRequest, textStatus) {
				console.log(textStatus)
				usdlc.alert("noServer.htm")
			}
		}, options || {}))
	}

	function results(link, container, options) {
		var dialog = link.data('results')
		if (! dialog) {
			dialog = usdlc.dialog(container || '<div/>', $.extend({}, {
				title: link.text(),
				autoOpen: false,
				width: '50%',
				open: function() {
					var widget = dialog.dialog("widget")
					widget.position({
						of: link, my: 'bottom', at: 'top', collision: 'flip',
						using: function(position) {
							var from = widget.offset()
							widget.animate({top:position.top + from.top, left:position.left + from.left})
						}
					})
				}
			}, options || {}))
			link.data('results', dialog)
		}
		return dialog
	}

	function runSections(sections) {
		usdlc.clearRunningLinkClass()
		sections.each(function() {
			var section = $(this)
			$("a.usdlc", section).each(runLink($(this)))
		})
	}

	function runLink(link) {
		usdlc.runningLinkClass(link, 'running')
		var a = link.get(0)
		var url = a.pathname + a.search

		//noinspection SwitchStatementWithNoDefaultBranchJS
		switch (link.attr('action')) {
			case 'page':
				if (link.hasClass('page')) {
					var creationOptions = {
						url: url, action: 'client-run', title: link.text(),
						height: "90%", width: "90%", modal: true
					}
					results(link, '<iframe/>', creationOptions).attr('src', url)
				}
				break
			case 'runnable':
				enqueue(link)
				break
			case 'producer':
				enqueue(link)
				break
			case 'consumer':
				var consumerId = link.attr('id')
				var producers = link.closest('th').next('td').find("a.usdlc")
				producers.each(function() {
					enqueue($(this), {
						queueName : consumerId,
						dialogCommand: 'close'
					})
				})

				enqueue(link, {
					queueName : consumerId,
					parallel : false
				}, function(options) {   // called when ready to run
					var input = "<div>"
					producers.each(function() {
						input += results($(this)).html()
					})
					input += '</div>'
					$.ajax($.extend({}, options, {
						type : 'POST',
						url : url,
						data : input
					}))
				})
				break
		}
	}
})
