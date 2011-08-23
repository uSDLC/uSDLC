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
window.usdlc = {
	synopses : function() { // we will provide one with teeth after
	},
	/**
	 * Given a path (from a URL), return the parent path - being the complete
	 * directory structure without file name.
	 */
	parentPath : function(path) {
		var lastSlash = path.lastIndexOf('/')
		if (lastSlash != -1)
			path = path.substring(0, lastSlash)
		return path
	},
	removeUrlBase : function(path) {
		var b = usdlc.urlBase + '/'
		if (path.substring(0, b.length) == b) {
			path = path.substring(b.length)
		}
		return path
	}
}

window.onload = function() {
	usdlc.urlBase = usdlc.parentPath(window.location.pathname)

	var head = document.getElementsByTagName('head')[0]

	var base = document.createElement('base')
	base.setAttribute('href', usdlc.urlBase)
	head.appendChild(base)

	function loadScriptAsync(path, onScriptLoaded) {
		var script = document.createElement("script")
		script.type = "text/javascript"
		script.async = "async"
		if (script.readyState) { // IE
			script.onreadystatechange = function() {
				if (script.readyState == "loaded" || script.readyState == "complete") {
					script.onreadystatechange = null;
					onScriptLoaded(path);
				}
			}
		} else { // Others
			script.onload = function() {
				onScriptLoaded(path)
			}
		}
		script.src = usdlc.urlBase + path
		head.appendChild(script)
	}

	usdlc.loadScript = loadScriptAsync

	function loadSetInParallel(scripts, onSetAllLoaded) {
		var countdown = scripts.length
		while (scripts && scripts.length) {
			loadScriptAsync(scripts.shift(), function(path) {
				if (!--countdown)
					onSetAllLoaded()
			})
		}
	}

	function loader(sets, onSetsAllLoaded) {
		if (sets.length) {
			loadSetInParallel(sets.shift(), function() {
				loader(sets, onSetsAllLoaded)
			})
		} else {
			onSetsAllLoaded()
		}
	}

	var preload = [
			[ '/lib/jquery/js/jquery.js' ],
			[ '/lib/jquery/js/jquery-ui-1.8.13.custom.js' ],
			[ '/lib/jquery/js/jquery.cookie.js', '/lib/jquery/js/jquery.sausage.js',
					'/lib/jquery/js/jquery.hotkeys.js', '/lib/jquery/js/jquery.url.js', '/rt/js/base.js' ],
			[ '/rt/js/init.js' ], [ '/rt/js/section.js', '/rt/js/template.js' ], ]
	var postload = [
			[ '/lib/jquery/js/fg.menu.js', '/lib/jquery/js/fg.menu.js', '/lib/ckeditor/ckeditor.js',
					'/lib/jquery/js/jquery.scrollTo.js', '/lib/jquery/js/jquery.jstree.js', '/rt/js/server.js',
					'/lib/CodeMirror/lib/codemirror.js' ],
			[ '/rt/js/contentTree.js' ],
			[ '/rt/js/synopses.js', '/lib/ckeditor/adapters/jquery.js', '/rt/js/menu.js', '/rt/js/moveSection.js',
					'/rt/js/clipboard.js', '/rt/js/run.js', '/rt/js/htmlEditor.js', '/rt/js/sourceEditor.js' ] ]

	loader(preload, function() {
		usdlc.init.pageLayout()
		usdlc.init.decoratePage()
		usdlc.init.loadPage(function() {
			setTimeout(function() {
				loader(postload, function() {
					loadSetInParallel(usdlc.getSourceEditorModes(), usdlc.init.finalise)
				})
			}, 500)
		})
	})
}
