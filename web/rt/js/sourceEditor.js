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
		//basic : 'basic',
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
			if (!(value in included)) {
				included[value] = true
				set.push('/lib/CodeMirror/mode/' + value + '/' + value + '.js')
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
	 * Links that don't end in .html can be edited with the source editor
	 */
	usdlc.displaySource = function(wrapper, data, url) {
		var serverExt = usdlc.mimeType(url).serverExt
		var syntax = syntaxes[(serverExt in syntaxes) ? serverExt : 'groovy']
		function editorMode(codemirror, inEditor, theme) {
			usdlc.activeSourceEditor = codemirror
			usdlc.inEditor = inEditor
			usdlc.highlight()
			codemirror.setOption('theme', inEditor ? 'night' : 'elegant')
			codemirror.setOption('readOnly', !inEditor)
		}
		var codemirror = CodeMirror(wrapper.get(0), {
			value : data,
			mode : syntax,
			theme : 'elegant',
			indentUnit : 4,
			indentWithTabs : true,
			tabMode : 'classic',
			enterMode : 'indent',
			electricChars : true,
			lineNumbers : true,
			gutter : true,
			readOnly : false,
			onChange : function(editor) {
				editor.contentChanged = true
			},
			onCursorActivity : function(editor) {
			},
			onGutterClick : function(editor) {
			},
			onFocus : function(editor) {
				usdlc.setFocus(wrapper);
				editorMode(editor, true, 'night')
			},
			onBlur : function(editor) {
				usdlc.saveSource();
				editorMode(editor, false, 'elegant')
			},
			onScroll : function(editor) {
			},
			onHighlightComplete : function(editor) {
			},
			matchBrackets : true,
			onKeyEvent : function(editor, event) {
				if (event.altKey || event.ctrlKey || event.metaKey) {
					if (event.type == 'keydown') {
						event.srcElement.usdlcKeyEvent = false
					}
					wrapper.trigger(event)
					if (event.srcElement.usdlcKeyEvent) {
						event.stop()
					}
				}
				return false
			}
		})
		codemirror.url = url
		wrapper.data('codemirror', codemirror)
		// CodeMirror stuffs up. It paints then resizes without painting again
		setTimeout(function() {
			codemirror.refresh()
		}, 500)
	}
})
