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
	usdlc.contentTree = $('#contentTree').jstree({
		html_data : { ajax : {
			url : function(li) {
				var path
				if (li == -1) {
					path = '/index.html'
				} else {
					path = $('a', li).get(0).pathname
				}
				this.contentRoot = jQuery.url.setUrl(path).attr("directory")
				return path
			},
			success : function(data) {
				var tree = ''
				var root = this.contentRoot
				$('<ins/>').html(data).find('a.usdlc[action=page]').each(function() {
					var a = $(this)
					var href = a.attr('href')
					if (href[0] == '/') {
						href = href.substring(1)
					}
					href = root + href
					var id = usdlc.camelCase(href.replace(/\/index\.html/g, '').replace(/\//g, ' '))
					tree += "<li id='" + id + "' class='jstree-closed'><a href='" + href + "' class='contentLink'>" + a.text() + "</a></li>"
				})
				return tree || null
			}
		}},
		hotkeys : {
			del : function() {
			},
			f2 : function() {
			}
		},
		plugins : ['html_data', 'ui', 'cookies', 'themeroller', 'hotkeys']
	})
})
