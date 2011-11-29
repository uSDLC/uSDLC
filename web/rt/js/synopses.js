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
						var html = $("<div/>").html(data).children("div.section").first()
						if (html.text()) {
							var children = html.children()
							wrapper.append(children.length ? children : html)
						}
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
		if (path.indexOf('..') == -1 && path.indexOf('://') == -1) {
			path = usdlc.normalizeURL(link.get(0).pathname)
			var section = link.parents('div.synopsis')
			var iid = link.attr('id') + '_inclusion'
			var inclusion = $('<div/>').addClass('inclusion').attr('id', iid)
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
