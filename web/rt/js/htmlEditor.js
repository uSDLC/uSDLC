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
	$.extend(true, window.usdlc, {
		/**
		 * Used from a menu to edit the current section.
		 */
		editSectionInFocus : function() {
			$('.inFocus').each(function() {
				editSection(this)
			})
		},
		savePage : function() {
			// clean up html of crap that builds up
			usdlc.cleanSections($('div.editable'))
			usdlc.savePageContents()
		},
		camelCase : function(text) {
			return text.replace(/(\s'"+.)/g, function($1) {
				return $1.toUpperCase().charAt($1.length - 1)
			});
		}
	})
	/*
	 Triggered by the user when they choose to edit a paragraph. creates a CKEDITOR,
	 stashes initial contents and prepares for a save on exit. The save sends a diff string to the server so that
	 full history is recorded.
	 */
	function editSection(section) {
		var $section = $(section)
		usdlc.setFocus($section)
		usdlc.highlight(false) // remove highlighting so save will stand out
		$section.ckeditor(function() {
			usdlc.modalOn()
		}, {
			bodyClass: "wayOnTop",
			saveFunction : function(editor) {
				var updateContents = editor.checkDirty()
				//usdlc.sectionEditor = null
				usdlc.modalOff()
				// Get rid of the editor so that it does not show up in the saved content.
				editor.destroy()
				if (!updateContents) {
					return
				}
				// Special case for the title - move it back where it belongs.
				var baseId = $section.attr('id');
				baseId += 'a'
				var pageLinkCount = 0
				// Process links to see what they should do
				$('a', $section).removeAttr('action').each(function(idx) {
					var self = $(this)
					var targetId = baseId + idx
					if (! self.attr('id')) {
						self.attr('id', targetId)
					}
					var href = self.attr('href')
					self.removeClass()  // removes all classes so we can re-add them by current standards.
					if (href && href.indexOf(':') == -1) {
						pageLinkCount++
						self.addClass('usdlc')
						href = usdlc.camelCase(href)
						if (href.charAt(href.length - 1) == '/') {
							href += "index.html"
						} else if (href.indexOf('.') == -1) {
							href += "/index.html";
						}
						self.attr('href', href)
						if (usdlc.mimeType(href).clientExt == 'html') {
							self.attr('action', 'page')
						} else if (self.attr('action') != 'producer') {
							self.attr('action', 'runnable')
							self.closest('th').each(function() {
								self.attr('action', 'consumer')
								$(this).next('td').find("a.usdlc").attr('action', 'producer')
							})
						}
					}
				})
				// See if we load synopsis from inner page link
				if (pageLinkCount == 1) {
					$section.addClass('synopsis')
				} else {
					$section.removeClass('synopsis')
				}
				usdlc.savePage()
			},
			on : {
				instanceReady : function(ev) {
					// Once the editor is ready for action keep a copy of the unchanged contents and sent the focus.
					ev.editor.focus()
				}
			}
		})
	}

	/*
	 By default CKEDIT does not have a ready save button unless it is inside a form element. As we are ajaxing it,
	 enable save and make sure it does the job we want.
	 */
	CKEDITOR.plugins.registered['save'] = {
		init : function(editor) {
			editor.addCommand('save', {
				modes : { wysiwyg:1, source:1 },
				exec : editor.config.saveFunction
			});
			editor.ui.addButton('Save', {label : 'Save',command : 'save'});
		}
	}
	/*
	 CKEditor mods to turn it into a mean uSDLC machine
	 */
	CKEDITOR.on('dialogDefinition', function(ev) {
		var dialogName = ev.data.name;
		var dialogDefinition = ev.data.definition;
		if (dialogName == 'link') {
			var infoTab = dialogDefinition.getContents('info');
			var protocol = infoTab.get("protocol")
			protocol.items.unshift([
				'local',''
			])
			protocol['default'] = 'local'
		}
	})
})
