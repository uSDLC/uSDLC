/*
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

import usdlc.Store

/**
 Filter used to wrap html page with decorations and necessary functions.
 *
 * User: Paul Marrington
 * Date: 24/11/10
 * Time: 9:25 PM
 */
doc.html {
	head {
		// Title is filled by script from the pageTitle id section in page contents.
		title('')
		// Core CSS file loads other required CSS files.
		link(type: "text/css", rel: "stylesheet", href: "/rt/base.css")
		link(type: "text/css", rel: "stylesheet", href: "/lib/google-code-prettify/prettify.css")
		base(href: "/")
	}
	body(style: 'display:none') {
		// Tables still provide a good layout structure. Here the top section has a title and logo. The centre is a notification bar on the left, content in the centre and scroll region on the right.
		table(id: 'pageTitleTable') {
			tr {
				td(id: 'pageTitleTd', contextMenu: 'title')
				td(rowspan: '2') {
					a(href: "/root") {
						img(id: 'pageTitle', src: '/rt/base.logo.png',
								alt: 'Unified Software Development Life-Cycle')
					}
				}
				tr {
					td { span(id: 'toolbar', class: 'toolbar') }
				}
			}
		}
		table(id: 'pageContentsTable') {
			tr {
				td(id: 'contentTree') {
					ul {
						li('class': 'jstree-closed') {
							a(href: '/', 'class': 'contentLink', ' ')
						}
					}
				}
				td(id: 'pageContentsTd') {
					div(id: 'pageContents')
				}
				td(id: 'pageContentSliderTd') {
					div(id: 'pageContentSlider')
				}
			}
		}
		// Most interaction is through a pop-up menu that is included here from a separate file. It is loaded as a secondary request.
		div('', class: 'toolbar', id: 'toolbar')
		div('', class: 'contextMenu', id: 'menuSection')
		div('', class: 'contextMenu', id: 'menuTitle')
		div('', id: 'pasteList') {
			def now = new Date().time
			Store.base('clipboard').dir().sort().reverse().eachWithIndex { clip, idx ->
				if (clip) {
					def item = Store.parseUnique(clip)
					long age = now - item.date.time
					int shade = 0x0000FF - (age / 60000)   // blue to black over 4 hours
					def colour = Integer.toHexString((shade < 0) ? 0 : shade)

					a(item.title, href: "/$item.path", style: "color:$colour;")
				}
			}
		}
		// No such luck for javascript. Eventually we should consider using a compressed single file for release. We need to put an empty tag content so that the builder creates an empty element rather than reducing to a single self-closing element.
		jsType = 'text/javascript'
		span(id: 'scripts', class: 'hidden') {
			script('', type: jsType, src: '/lib/jquery/js/jquery-1.5.js')
			script('', type: jsType, src: '/lib/jquery/js/jquery-ui-1.8.6.custom.js')
			script('', type: jsType, src: '/lib/jquery/js/jquery.cookie.js')
			script('', type: jsType, src: '/lib/jquery/js/jquery.hotkeys.js')
			script('', type: jsType, src: '/lib/jquery/js/jquery.jstree.js')
			script('', type: jsType, src: '/rt/js/base.js')
			script('', type: jsType, src: '/rt/js/contentTree.js')
			script('', type: jsType, src: '/rt/js/section.js')
			script('', type: jsType, src: '/rt/js/synopses.js')
			script('', type: jsType, src: '/rt/js/template.js')

			script('', type: jsType, src: '/lib/jquery/js/jquery.url.js')
			script('', type: jsType, src: '/lib/jquery/js/fg.menu.js')
			script('', type: jsType, src: '/lib/ckeditor/ckeditor.js')
			script('', type: jsType, src: '/lib/ckeditor/adapters/jquery.js')
			script('', type: jsType, src: '/lib/edit_area/edit_area_full.js')
			script('', type: jsType, src: '/rt/js/menu.js')
			script('', type: jsType, src: '/lib/jquery/js/jquery.scrollTo.js')
			script('', type: jsType, src: '/rt/js/htmlEditor.js')
			script('', type: jsType, src: '/lib/google-code-prettify/prettify.js')
			script('', type: jsType, src: '/rt/js/sourceEditor.js')

			script('', type: jsType, src: '/rt/js/clipboard.js')
			script('', type: jsType, src: '/rt/js/moveSection.js')
			script('', type: jsType, src: '/rt/js/server.js')
			script('', type: jsType, src: '/rt/js/run.js')
		}
	}
}
