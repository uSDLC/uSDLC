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
	var pageTitle = $('td#pageTitleTd')
	$.extend(true, window.usdlc, {
		/*
		 * When the page loads, set the edit mode to the default.
		 */
		editMode : true,

		setEditMode : function(to) {
			var color
			if (usdlc.editMode = to) {
				color = false
				usdlc.clearRunningLinkClass && usdlc.clearRunningLinkClass('')
				$('.usdlc').removeData('results')
			} else {
				color = 'yellow'
				usdlc.runningLinkClass('a.usdlc', 'runOnClick')
			}
			$('.section').each(function() {
				usdlc.highlight(color, $(this))
			})
		},
		log : function(message) {
			if (window.console) {
				// Firefox & Google Chrome
				console.log(message);
			} else {
				$("body").append(
						"<div class='logMessage'>" + message + "</div>");
			}
		},
		/**
		 * Give notice to an error by highlighting the page selection and
		 * displaying a message.
		 *
		 * @param messageFile
		 *            File/script to produce message to display
		 */
		alert : function(messageFile, parent) {
			usdlc.highlight('red')
			var path = messageFile
			if (path.match(/.*\.htm$/)) {
				path = '/rt/alerts/' + path
			}
			if (path.indexOf('.') == -1) {
				path += ".html"
			}
			$.get(path, function(data) {
				var alertBox = null
				alertBox = usdlc.dialog("<div/>", {
					dialogClass : 'ui-state-error ui-corner-all',
					modal : true,
					minHeight : 100,
					height : 'auto',
					close : function() {
						alertBox.dialog("destroy").detach()
					}
				}).append(data)
				var h1 = $("h1", alertBox)
				var title
				if (h1.length) {
					title = h1.text()
					h1.detach()
				} else {
					title = alertBox.text()
				}
				alertBox.dialog("option", "title", title)
				alertBox.dialog("widget").position({
					of : parent || usdlc.inFocus,
					my : 'top',
					at : 'bottom',
					collision : 'flip'
				})
				$('.editable', alertBox).removeClass('editable')
			})
		},
		/**
		 * Save a file to the usdlc.server.servletengine.server and expect a
		 * response in javaScript.
		 *
		 * @param where
		 *            Address to post to
		 * @param what
		 *            Data for the body of the post
		 */
		save : function(where, what, more) {
			$.post(usdlc.serverActionUrl(where, 'save' + (more || '')), what,
					function(code) {
						$.globalEval(code)
					})
		},

		maskEverything : function(dim) {
			$('#modalMask').css({
				width : $(window).width(),
				height : $(document).height()
			}).fadeTo("slow", dim)
		},
		getPageTitle : function() {
			usdlc.pageContents.prepend($('div#pageTitle'))
		},
		setPageTitle : function() {
			pageTitle.children().remove()
			pageTitle.append($('div#pageTitle'))
			document.title = $('h1', pageTitle).text()
		},
		absolutePageContents : function(path, afterwards) {
			usdlc.pageContentsURL = usdlc.normalizeURL(path)
			var base = jQuery.url.setUrl(usdlc.pageContentsURL).attr(
					"directory")
			$('base').attr('href', base)
			usdlc.setCookie('currentPage', usdlc.pageContentsURL)
			$.get(usdlc.pageContentsURL, function(data) {
				usdlc.pageContents.html(data)

				usdlc.setPageTitle()
				usdlc.synopses()
				usdlc.setEditMode(usdlc.editMode)
				usdlc.pageContentsSausages.sausage()
				afterwards && afterwards()
			})
		},
		relativePageContents : function(path) {
			usdlc.absolutePageContents(usdlc.normalizeURL(path || '/'))
		},
		pageContentsURL : '/index.html',
		normalizeURL : function(path) {
			var p = path
			var dot = p.lastIndexOf('.')
			var slash = p.lastIndexOf('/')
			if (slash == p.length - 1) {
				p += "index.html"
			} else if (dot < slash) {
				p += "/index.html"
			}
			return p
		},
		savePageContents : function() {
			/*
			 * Give back to the usdlc.server.servletengine.server - after moving
			 * page title back into the body temporarily.
			 */
			usdlc.cleanSections($('div.editable'))
			usdlc.getPageTitle()
			usdlc.scrollFiller(false)
			usdlc.save(usdlc.pageContentsURL, usdlc.pageContents.html(),
					'&after=usdlc.synopses()')
			usdlc.setPageTitle()
			usdlc.scrollFiller(true)
			usdlc.pageContentsSausages.sausage()
		},
		createPageTitle : function(heading, subtitle) {
			return $('<div/>').attr('id', 'pageTitle').addClass('editable')
					.append($('<h1/>').append(heading)).append(
					$('<h2/>').append(subtitle))
		}
	})

	$('a.usdlc[action=page]').live('click', function(ev) {
		usdlc.relativePageContents(ev.currentTarget.pathname)
		return false
	})

	$('a.contentLink').live('click', function(ev) {
		usdlc.absolutePageContents(ev.currentTarget.pathname)
		return false
	})

	// The last thing we do is make the page visible. Hopefully and the visual
	// work will be done by now.
	$('body').removeAttr('style')
	// .bind('click',
	// function(event) {
	// // we need to avound clearing focus if we are returning from the editor
	// if (!$(event.target).hasClass('cke_icon')) {
	// usdlc.clearFocus()
	// }
	// })

})
