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

/**
 * User: paul
 * Date: 7/07/11
 * Time: 5:01 PM
 */
window.onload = function() {
	var head = document.getElementsByTagName('head')[0]

	function load(path, callback) {
		var script = document.createElement("script")
		script.type = "text/javascript"
		script.async = "async"
		if (script.readyState)  //IE
			script.onreadystatechange = function() {
				if (script.readyState == "loaded" ||
						script.readyState == "complete") {
					script.onreadystatechange = null;
					callback();
				}
			}
		else  //Others
			script.onload = function() {
				callback()
			}
		script.src = path
		head.appendChild(script)
	}

	function loader(scripts, callback) {
		return function() {
			var lastScript = scripts.length - 1
			for (var i = 0; i < lastScript; i++) {
				load(scripts[i], function() {
				})
			}
			load(scripts[lastScript], callback)
		}
	}

	loader([
		'/lib/jquery/js/jquery.js'],
			loader([
				'/lib/jquery/js/jquery-ui-1.8.13.custom.js'],
					loader([
						'/lib/jquery/js/jquery.cookie.js',
						'/lib/jquery/js/jquery.sausage.js',
						'/lib/jquery/js/jquery.hotkeys.js',
						'/lib/jquery/js/jquery.url.js',
						'/rt/js/base.js'],
							loader([
								'/rt/js/init.js'],
									loader([
										'/rt/js/section.js',
										'/rt/js/template.js'],
											function() {
												usdlc.init.pageLayout()
												usdlc.init.decoratePage()
												usdlc.init.loadPage(function() {
													// give it time to render, etc before the less immediate code
													setTimeout(loader([
														'/lib/jquery/js/fg.menu.js',
														'/lib/jquery/js/fg.menu.js',
														'/lib/ckeditor/ckeditor.js',
														'/lib/edit_area/edit_area_full.js',
														'/lib/jquery/js/jquery.scrollTo.js',
														'/lib/jquery/js/jquery.jstree.js',
														'/lib/google-code-prettify/prettify.js'],
															loader([
																'/rt/js/contentTree.js'],
																	loader([
																		'/rt/js/synopses.js',
																		'/lib/ckeditor/adapters/jquery.js',
																		'/rt/js/menu.js',
																		'/rt/js/moveSection.js',
																		'/rt/js/clipboard.js',
																		'/rt/js/server.js',
																		'/rt/js/run.js',
																		'/rt/js/htmlEditor.js',
																		'/rt/js/sourceEditor.js'
																	],
																			usdlc.init.finalise
																	)
															)
													), 500)
												})
											})
							)
					)
			)
	)()
}
