$(function() {
	window.usdlc = {
		synopses : function(){}, //# we will provide one with teeth after
		loadActorTypes : function() {},
		/*
		 Given a path (from a URL), return the parent path - being the complete
		 directory structure without file name.
		 */
		parentPath : function(path) {
			lastSlash = path.lastIndexOf('/')
			if (lastSlash != -1) path = path.substring(0,lastSlash)
			return path
		},

		removeUrlBase : function(path) {
			b = usdlc.urlBase + '/'
			if (path.substring(0, b.length) == b)
				path = path.substring(b.length)
			return path
		}
	}
	usdlc.urlBase = usdlc.parentPath(window.location.pathname)
	window.CKEDITOR_BASEPATH = '/lib/ckeditor/'


	head = document.getElementsByTagName('head')[0]
	base = document.createElement('base')
	base.setAttribute('href', usdlc.urlBase)
	head.appendChild(base)

	$.extend(true, window.usdlc, {
		loadScriptAsync : function(path, onScriptLoaded) {
			script = document.createElement("script")
			script.type = "text/javascript"
			script.async = "async"

			if (script.readyState) { // # IE
				script.onreadystatechange = function() {
					if (script.readyState == "loaded" ||
							script.readyState == "complete") {
						script.onreadystatechange = null;
						onScriptLoaded(path);
					}
				}
			} else { // # Other browsers
				script.onload = function() {
					onScriptLoaded(path);
				}
			}

			script.src = usdlc.urlBase + path
			head.appendChild(script)
		},
		/*
		 * Modalise for editors
		 */
		modalOn : function(box) {
			usdlc.maskEverything(0.2)
			if (box) {
				box.css('z-index', 9999).show('slow')
			}
		},
		modalOff : function(box, afterwards) {
			$('#modalMask').hide('slow')
			if (box) {
				box.css('z-index', 0).hide('slow', afterwards)
			}
		},
		/**
		 * Call page-sensitive help. Goes from referrer to root looking for a
		 * help directory.
		 */
		help : function() {
			usdlc.window("help", "/rt/help/index.html", {})
		},
		cookie : function(key) {
			return $.cookie(key)
		},
		setCookie : function(key, value) {
			return $.cookie(key, value, {
				expires : 1000
			})
		},
//		toggleShow : function() {
//			$.each(arguments, function(index, value) {
//				$(value).css('display', 'none')
//			})
//			$(arguments[0]).css('display', 'inherit')
//		},
		camelCase : function(text) {
			return text.replace(/([\s:\?\*%\|"<>\-~]+)(\w)/g,
					function(a, s, c) { return c.toUpperCase() })
		},
		decamel : function(text) {
			return text.replace(/\B([A-Z])/g,
					function(a, c) { return ' ' + c })
		},
		inEditMode: function(event) {
			var node = event.target.nodeName.toLowerCase()
			return node == 'textarea' || node == 'input' ||
					$(event.target).children('.CodeMirror').size() > 0
		},
		logOut: function() {    // used in top.menu
			$.get(usdlc.urlBase + '/support/usdlc/logOut.groovy',
				function() {
					$('#pageTitleImage').attr('title', '')
					usdlc.goHome()
				})
		},
		logIn: function() {    // used in top.menu
			var userName = $('#loginform input[name="user"]')[0].value
			var password = $('#loginform input[name="password"]')[0].value
			$.ajax({
				type : "POST",
				url : usdlc.urlBase + '/support/usdlc/logIn.groovy',
				contentType: 'application/x-www-form-urlencoded',
				data : {name:userName,password:password},
				success : function(data) {
					if (data == 'ok') {
						$('#pageTitleImage').attr('title', userName)
						usdlc.closeDialog()
						usdlc.goHome()
					} else {
						$('#pageTitleImage').attr('title', '')
						usdlc.alert('Login Failed')
					}
				}
			})
		},
		changePassword: function() {    // used in top.menu
			var url = usdlc.urlBase + '/support/usdlc/changePassword.groovy'
			var oldpwd = $('#changepassword input[name="oldpwd"]')[0].value
			var pwd1 = $('#changepassword input[name="pwd1"]')[0].value
			var pwd2 = $('#changepassword input[name="pwd1"]')[0].value
			if (pwd1 != pwd2) {
				usdlc.alert('Password Mismatch')
			} else {
				$.post(url, {was:oldpwd,to:pwd1}, function(data) {
					if (data == 'ok') {
						usdlc.closeDialog()
					} else {
						usdlc.alert('Password Change Failure')
					}
				})
			}
		},
		goHome: function() {
			usdlc.absolutePageContents("/frontPage/",
					function() {
						usdlc.contentTree.jstree('deselect_all')
						usdlc.contentTree.jstree('refresh')
					})
		},
		setInnerHtml : function(element, data) { element.html(data) },
		globalEval: function(element, script) { $.globalEval(script) },
		/**
		 * Run through all elements asking for action
		 * div[href] actions can be setInnerHTML, globalEval or loadMainMenu
		 * div[activate=xxx] calls usdlc.activation.xxx(element)
		 */
		activateHtml : function(html) {
			$('div[href]', html).each(function() {
				var element = $(this)
				var href = element.attr('href')
				var action = element.attr('action') || "setInnerHtml"
				$.get(href, function(data) {
					usdlc[action](element, data)
				})
			})
			$('*[activate]', html).each(function() {
				var element = $(this)
				var action = element.attr('activate')
				try {
					usdlc[action](element)
				} catch(e) {
					usdlc.log("No action '"+action+"' when activating "+element.tagName())
				}
			})
		}
	})
})
