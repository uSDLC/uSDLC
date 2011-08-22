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
	var syntaxes = {
			basic : 'basic',
			c : 'clike',
			h : 'clike',
			cpp : 'clike',
			hpp : 'clike',
			css : 'css',
			groovy : 'clike',
			html : 'htmlmixed',
			htm : 'htmlmixed',
			java : 'clike',
			js : 'javascript',
			lua : 'lua',
			php : 'php',
			py : 'python',
			st : 'smalltalk',
			sql : 'plsql',
			xml : 'xml',
			yaml : 'yaml'
		}

	usdlc.getSourceEditorModes = function() {
		var set = [], included = {}
		$.each(syntaxes, function(key, value) {
			if (! (value in included)) {
				included[value] = true
				set.push('/lib/CodeMirror/mode/'+value+'/'+value+'.js')
			}
		})
		return set
	}
	usdlc.activeSourceEditor = null
	usdlc.saveSource = function() {
		if (usdlc.inEditor && usdlc.activeSourceEditor.contentChanged) {
			usdlc.deleteOutput()
			var content = usdlc.activeSourceEditor.getValue()
			usdlc.save(usdlc.activeSourceEditor.url, content, '')
			usdlc.activeSourceEditor.contentChanged = false
		}
	}
	/*
	 Links that don't end in .html can be edited with the source editor
	 */
	usdlc.displaySource = function(wrapper, data, url) {
		var serverExt = usdlc.mimeType(url).serverExt
		var syntax = syntaxes[(serverExt in syntaxes) ? serverExt : 'groovy']
		function editorMode(codemirror, inEditor, theme) {
			usdlc.activeSourceEditor = codemirror
			usdlc.inEditor = inEditor
			usdlc.highlight()
			codemirror.setOption('theme', inEditor? 'night' : 'elegant')
			codemirror.setOption('readOnly', ! inEditor)
		}
		var codemirror = CodeMirror(wrapper.get(0), {
			value: data,
			mode: syntax,
			theme: 'elegant',
			indentUnit: 4,
			indentWithTabs: true,
			tabMode: 'classic',
			enterMode: 'indent',
			electricChars: true,
			lineNumbers: true,
			gutter: true,
			readOnly: false,
			onChange: function(editor) { editor.contentChanged = true },
			onCursorActivity: function(editor) {},
			onGutterClick: function(editor) {},
			onFocus: function(editor) { usdlc.setFocus(wrapper); editorMode(editor, true, 'night') },
			onBlur: function(editor) { usdlc.saveSource(); editorMode(editor, false, 'elegant') },
			onScroll: function(editor) {},
			onHighlightComplete: function(editor) {},
			matchBrackets: true,
			onKeyEvent: function(editor, event) {
				wrapper.trigger(event)
				if (event.altKey || event.ctrlKey || event.metaKey) event.stop()
				return false
			}
		})
		codemirror.url = url
		wrapper.data('codemirror', codemirror)
		// CodeMirror stuffs up. It paints then resizes without painting again
		setTimeout(function() {codemirror.refresh()}, 500)
	}
	usdlc.addSourceEditorKey = function(key, action) {
		keyHandlers.push()
	}
	keyHandlers = []
//	usdlc.pageContents.bind('scroll', function() {
//		$("div.inclusion").each(function() {
//			var codemirror = $(this).data('codemirror')
//			if (codemirror) codemirror.refresh()
//		})
//	})
//	$('a.sourceLink').live('click', function(ev) {
//		var link = $(ev.currentTarget)
//		var href = ev.currentTarget.pathname
//		var section = link.parents(".section")
//		usdlc.setFocus(section)
//		usdlc.highlight(false) // remove highlighting so save will stand out
//		if (usdlc.editMode) {
//			// We are going to edit - so get the contents to load into a textarea.
//			$.get(href + '?action=raw', function(data) {
//				var editArea = $('<textarea/>').attr('id', 'editArea')
//				var editAreaContainer = $('<div/>').attr('id', 'editAreaContainer').append(editArea)//.hide()
//				link.after(editAreaContainer)
//
//				editArea.html(data)
//				usdlc.sourceSaveCallback = function(id, content) {
//					usdlc.modalOff(editAreaContainer, function() {
//						usdlc.save(href, content, '&after=usdlc.synopses()')
//						editAreaContainer.remove()
//						// Removing editor from the DOM is not enough if the link that we were editing is inside an inline element. Webkit at least leave a like-break there since the now missing editor was in block mode. I hope to find a better way, but for now the best that can be done is to re-insert the html.
//						var parent = link.parents(".section")
//						parent.html(parent.html())
//						//usdlc.scrollFiller(true)
//						usdlc.scrollBack()
//					})
//				}
//				editAreaLoader.init({
//							id : "editArea"            // textarea id
//							,replace_tab_by_spaces: 4
//							,browsers: "all"
//							,syntax: usdlc.mimeType(href).syntax
//							,start_highlight: true    // to display with highlight mode on start-up
//							,allow_toggle: false    // Don't allow display of textarea under editor
//							, min_width: 700
//							, min_height: 400
//							,toolbar: "save, search, go_to_line, |, undo, redo, |, select_font,|, highlight, " +
//									"reset_highlight, word_wrap, |, help"
//							,save_callback: "usdlc.sourceSaveCallback"
//						})
//				usdlc.scrollTo(link)
//				usdlc.modalOn(editAreaContainer)
//			})
//		} else {    // Run mode - run or display results if the item has been run.
//			usdlc.linkAction(link)
//		}
//		return false
//	})
})
//	var w = $(window)
//	var onResize = function() {
//		width = w.width() - $('#contentTree').width() - 100
//		$('.prettyprint', usdlc.pageContents).css('max-width', width)
//	}
//	w.resize(onResize)
