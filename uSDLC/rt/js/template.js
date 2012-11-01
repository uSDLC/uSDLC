$(function () {
	var pageHistory = []
	var pageTitle = $('td#pageTitleTd')
	$.extend(true, window.usdlc, {
		log:function (message) {
			if (window.console) {
				// Firefox & Google Chrome
				console.log(message);
			} else {
				$("body").append("<div class='logMessage'>" + message + "</div>");
			}
		},
       /** Save a file  and expect a response in javaScript. */
       save:function (where, what, more) {
           var url = usdlc.serverActionUrl(
	               where, 'save' + (more || ''))
           $.ajax({
             type: 'POST',
             url: url,
             data: what,
             contentType: 'text/html',
             dataType: 'script',
             success: function (code) {$.globalEval(code)}
           })
       },

		maskEverything:      function (dim) {
			$('#modalMask').css({
				width: $(window).width(),
				height:$(document).height()
			}).fadeTo("slow", dim)
		},
		getPageTitle:        function () {
			usdlc.pageContents.prepend($('div#pageTitle'))
		},
		setPageTitle:        function () {
			pageTitle.children().remove()
			pageTitle.append($('div#pageTitle'))
			document.title = $('h1', pageTitle).text()
		},
		menuToTop: function() {},
		pageIsLocked: function(data) {return data=='~locked~'},
		pageBack: function() {
			if (pageHistory.length) {
				usdlc.absolutePageContents(pageHistory.pop())
			}
		},
		refreshPage: function() {
			pageHistory.pop()
			var section = usdlc.inFocus ? usdlc.inFocus : usdlc.lastFocus
			var url = usdlc.pageContentsURL+'@'+section.attr('id')
			usdlc.absolutePageContents(url)
		},
		absolutePageContents:function (path, afterwards) {
			usdlc.clearFocus()
			if (path[0] != '/') path = '/' + path
			var to = usdlc.normalizeURL(path)
			if (pageHistory.length > 0 &&
					to == pageHistory[pageHistory.length - 1]) {
				if (afterwards) afterwards()
			} else {
				pageOnly = to.split('@')[0]
				$.get(pageOnly, function (data) {
					if (usdlc.pageIsLocked(data)) {
						usdlc.highlight('pink')
					} else if (to.indexOf("action=") == -1) {
						usdlc.pageContentsURL = pageOnly
						window.location.hash = usdlc.reduceUrl(to)
						pageHistory.push(to)
						if (pageHistory.length > 100) {
							pageHistory = pageHistory.slice(50)
						}
						var base = jQuery.url.
								setUrl(usdlc.pageContentsURL).attr("directory")
						base = "http://" + window.location.host + base
						$('base').attr('href', base)
						usdlc.setCookie('currentPage', to)

						usdlc.pageContents.html(data)
						usdlc.activateHtml(usdlc.pageContents)
						usdlc.setPageTitle()
						usdlc.synopses()
						usdlc.pageContentsSausages.sausage()
						usdlc.scrollTop()
						path = path.split('@')
						usdlc.finalisers.add(
							function(){
								usdlc.contentTree.setFocus(to)
								if (path.length <= 1) path[1] = "s1"
								usdlc.setFocus($('#'+path[1]))
							})
						usdlc.actorStates()
						if (afterwards) afterwards()
					}
				})
			}
		},

		relativePageContents:function (path) {
			usdlc.absolutePageContents(usdlc.normalizeURL(path || '/'))
		},
		pageContentsURL:     '/home',
		normalizeURL:        function (path) {
			var p = path.split('@')
			var sectionId = (p.length > 1) ? p[1] : ''
			p = p[0]
			var dot = p.lastIndexOf('.')
			var slash = p.lastIndexOf('/')
			if (slash == p.length - 1) {
				p += "index.html"
			} else if (dot < slash) {
				p += "/index.html"
			}
			if (sectionId != '') p = p + '@' + sectionId
			return p
		},
		reduceUrl: function(path) {
			path = path.replace(/^.*(~)/, '$1').
					replace(/\/index\..{3,4}(@.*)?$/, '$1')
			if (path[0] != '~') {
				path = '~uSDLC' + path
			}
			return path
		},
		savePage: function() {
			var focus = usdlc.inFocus
			usdlc.clearFocus()
			usdlc.cleanSections($('div.editable'))
			usdlc.destroyTasklists()
			usdlc.packWorkflows()
			usdlc.getPageTitle()
			usdlc.scrollFiller(false)
			usdlc.save(usdlc.pageContentsURL, usdlc.pageContents.html(), '&after=usdlc.synopses()')
			usdlc.setPageTitle()
			usdlc.activateHtml(usdlc.pageContents)
			usdlc.scrollFiller(true)
			usdlc.contentTree.refresh()
			usdlc.pageContentsSausages.sausage()
			usdlc.setFocus(focus)
		},
		createPageTitle:     function (heading, subtitle) {
			return $('<div/>').attr('id', 'pageTitle').addClass('editable').append($('<h1/>').append(heading)).append(
					$('<h2/>').append(subtitle))
		}
	})

	$('a').live('click', function (ev) {
		var a = $(ev.currentTarget)
		var href = a.attr('href')
		if (!href) return true
		var isHash = (href.length > 0 && href[0] == '#')
		if (a.attr('target') || isHash) {
			return true
		}
		var url = ev.currentTarget.pathname
		switch (ev.currentTarget.protocol) {
			case 'http:':
			case 'https:':
				switch (a.attr('action')) {
					case 'page':
						if (ev.shiftKey) {
							var loc = window.location
							var ref = loc.protocol + '//' + loc.host + '/usdlc/home#' + href
							usdlc.window(ref, ref, {width:800})
							// put tree focus back if we changed it
							usdlc.contentTree.setFocus(usdlc.pageContentsURL)
						} else {
							usdlc.relativePageContents(url)
						}
						break
					case 'runnable':
						usdlc.setFocus(a)
						usdlc.runSectionInFocus()
						break
					case 'reference':
						break
					case 'download':
						href = href.substring(0, href.length-1) // drop bang
						window.location.assign(href)
						break
					case 'raw':
						return true;    // let the browser handle it
					default:
						if (isHash || !url) return true
						usdlc.window("from uSDLC", href, {})
						break
				}
				break
			default:
				return true
		}
		return false
	})

	// The last thing we do is make the page visible. Hopefully all the visual
	// work will be done by now.
	usdlc.init.pageLayout()
	usdlc.init.decoratePage()
	$('body').removeAttr('style')
})
