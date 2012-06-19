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
       /**
        * Save a file to the usdlc.server.servletengine.server and expect a
        * response in javaScript.
        */
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
			while (pageHistory.length) {
				var to = pageHistory.pop()
				if (to != usdlc.pageContentsURL) {
					usdlc.absolutePageContents(to)
					return
				}
			}
		},
		refreshPage: function() {
			usdlc.absolutePageContents(usdlc.pageContentsURL)
		},
		absolutePageContents:function (path, afterwards) {
			usdlc.menuToTop()
			if (path[0] != '/') path = '/' + path
			var to = usdlc.normalizeURL(path)
			$.get(to, function (data) {
				if (usdlc.pageIsLocked(data)) {
					usdlc.highlight('pink')
				} else {
					usdlc.pageContentsURL = to
					window.location.hash = usdlc.reduceUrl(to)
					pageHistory.push(to)
					if (pageHistory.length > 100) {
						pageHistory = pageHistory.slice(50)
					}
					var base = jQuery.url.setUrl(usdlc.pageContentsURL).attr("directory")
					$('base').attr('href', base)
					usdlc.setCookie('currentPage', usdlc.pageContentsURL)

					usdlc.pageContents.html(data)

					usdlc.activateHtml(usdlc.pageContents)
					usdlc.setPageTitle()
					usdlc.synopses()
					usdlc.pageContentsSausages.sausage()
					usdlc.scrollTop()
					setTimeout(function(){usdlc.contentTree.setFocus(to)},500)
					usdlc.clearFocus()
					if (afterwards) afterwards()
					var blind = $('div#blind').first().hide();
					if (blind.size()) {
						setTimeout(function(){
							blind.first().show('blind', 10000)
						}, 2000)
					}
				}
			})
		},

		relativePageContents:function (path) {
			usdlc.absolutePageContents(usdlc.normalizeURL(path || '/'))
		},
		pageContentsURL:     '/home',
		normalizeURL:        function (path) {
			var p = path
//			if (p[0] != '/') {
//				p = '/' + p
//			}
			var dot = p.lastIndexOf('.')
			var slash = p.lastIndexOf('/')
			if (slash == p.length - 1) {
				p += "index.html"
			} else if (dot < slash) {
				p += "/index.html"
			}
			return p
		},
		reduceUrl: function(path) {
			return path.replace(/^.*(~)/, '$1').replace(/\/index\..{3,4}$/, '')
		},
		savePage: function() {
			/*
			 * Give back to the usdlc.server.servletengine.server - after moving
			 * page title back into the body temporarily.
			 */
			var focus = usdlc.inFocus
			usdlc.clearFocus()
			usdlc.cleanSections($('div.editable'))
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
		if (a.attr('target') || href[0] == '#') {
			return true
		}
		var url = ev.currentTarget.pathname
		switch (ev.currentTarget.protocol) {
			case 'http:':
			case 'https:':
				switch (a.attr('action')) {
					case 'page':
						usdlc.relativePageContents(url)
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
					default:
						if (href[0] == '#' || !url) return true
						usdlc.window(href,href,'from-usdlc')
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
