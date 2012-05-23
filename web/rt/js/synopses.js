$(function () {
	$.extend(true, window.usdlc, {
		newSynopsis: function() {
			var synopsisSection = $('div.section:first')
			usdlc.setFocus(synopsisSection)
			var asaText = $('#asa').html()
			var asa = $('#NewSynopsis input[name="asa"]')[0].value
			var iwantText = $('#iwant').html()
			var iwant = $('#NewSynopsis input[name="iwant"]')[0].value
			var sothatText = $('#sothat').html()
			var sothat = $('#NewSynopsis input[name="sothat"]')[0].value
			var notes = $('#NewSynopsis textarea[name="notes"]')[0].value
			usdlc.closeDialog()
			html = []
			if (asa.length)
				html.push('<b class="blue">'+asaText+'</b> '+asa)
			if (iwant.length)
				html.push('<b class="blue">'+iwantText+'</b> '+iwant)
			if (sothat.length)
				html.push('<b class="blue">'+sothatText+'</b> '+sothat)
			if (notes.length)
				html.push('<br>'+notes)
			if (html.length) {
				synopsisSection.html('<p>'+html.join('<br/>')+'</p>')
				usdlc.savePage()
			} else {
				usdlc.editSectionInFocus()
			}
		},
		clearSynopses:   function (sections) {
			$("div.inclusion", sections || usdlc.pageContents).remove()
			usdlc.deleteOutput()
		},
		doSynopses:      function () {
			usdlc.clearSynopses()
			$("div.synopsis a[action]").each(function () {
				var link = $(this)
				var action = link.attr("action")
				if (action == 'runnable') {
					loadSynopsis(link, usdlc.displaySource)
				} else {
					loadSynopsis(link, function (wrapper, data) {
						var html = $("<div/>").html(data).children("div.section").first()
						if (html.text()) {
							var children = html.children()
							wrapper.append(children.length ? children : html)
						}
					})
				}
			})
		},
		showSynopsis:    function (section) {
			$('div.inclusion', section).show()
		},
		hideSynopsis:    function (section) {
			$('div.inclusion', section).hide()
		},
		checkForSynopsis:function (section) {
			if ($("a[action]", section).size() > 0) {
				section.addClass('synopsis')
			} else {
				section.removeClass('synopsis')
			}
		}
	})

	function loadSynopsis(link, processor) {
		var path = link.attr('href')
//		function isInHeader() { return link.closest('h6,h5,h4,h3,h2,h1').size() > 0 }
//		function isInFooter() {
//			if (link.closest('.footer').size()) {
//				var ext = usdlc.splitUrl(path).ext
//				return "html.gsp.htm".indexOf(ext) == -1
//			}
//			return false
//		}
		function subPage() {
			if (path[0] == '/') return false
			if (path.length < 2) return false
			if (path.substring(0, 2) == '..') return false
			if (path.indexOf(':') != -1) return false
			return true
		}

//		if (isInHeader() || isInFooter()) {
		if (subPage() && !link.hasClass('nosynopsis')) {
			var href = link.get(0).pathname
			path = usdlc.normalizeURL(href)
			var section = link.parents('div.synopsis')
			var iid = link.attr('id') + '_inclusion'
			var inclusion = $('<div/>').addClass('inclusion').attr('id', iid)
			section.append(inclusion)
			$.ajax({
				url: usdlc.serverActionUrl(path, 'raw'),
				data: {},
				dataType: 'text',   // so jquery won't process it
				success: function (data) {
					if (usdlc.pageIsLocked(data)) {
						section.addClass('hidden')
						$('div#contentTree a[href="'+href+'"]').
								parent().remove()
					} else {
						if (data.length < 3) data = ''
						processor(inclusion, data, path)
					}
				}
			});
		}
	}

	// usdlc.contentTree.bind('after_open.jstree after_close.jstree', onResize)
})
