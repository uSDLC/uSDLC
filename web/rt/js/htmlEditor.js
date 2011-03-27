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
		 * Used from a menu to edit the page section.
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
			return text.replace(/(^|[\s'"\-]+)(\w)/g,
				function(a, s, c) {
					return c.toUpperCase()
				}).replace(/\s*/, '');
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
					self.removeClass()  // removes all classes so we can re-add them by page standards.
					if (href && href.indexOf(':') == -1) {
						pageLinkCount++
						self.addClass('usdlc')
						href = usdlc.camelCase(href)
						if (self.text() == '*') {
							self.addClass('star')
						}
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
			},
			extraPlugins : 'autogrow'
			//autoGrow_maxHeight : 800
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
			protocol.items.unshift(['local',''])
			protocol['default'] = 'local'
			var urlField, linkSelect, linkRadio, initialising = false
			// Called when user selects a type of link
			function setLinkType() {
				var type = linkRadio.getValue()
				$.cookie('linkRadioDefault', type)
				if (type == 'Actor') {
					type = linkSelect.getValue()
					$.cookie('actorDefault', type)
				}

				var name = usdlc.splitUrl(urlField.getValue()).name
				if (type) {
					name += '.' + type
				}
				urlField.setValue(name)
			}

			// Actors can have various extensions defining client and server languags
			var actorItems = []
			var actorDefault = $.cookie('actorDefault') || 'groovy'
			var linkRadioDefault = $.cookie('linkRadioDefault') || ''
			var actorTypes = ($.cookie('actorTypes') || 'groovy').split(',')
			for (actorType in actorTypes) {
				actorItems.push([actorTypes[actorType]])
			}

			dialogDefinition.onFocus = function() {
				urlField = this.getContentElement('info', 'url');
				linkSelect = this.getContentElement('info', 'linkSelect');
				linkRadio = this.getContentElement('info', 'linkRadio');
				var url = urlField.getValue()
				if (url) {
					var ext = usdlc.splitUrl(urlField.getValue()).ext
					switch (ext) {
						case '':
							linkRadioDefault = ext
							break
						case 'html':
							if (url.indexOf("/index.html") != -1) {
								url = url.substring(0, url.length - 11)
								urlField.setValue(url)
								ext = ''
							}
							linkRadioDefault = ext
							break
						default:
							linkRadioDefault = 'Actor'
							actorDefault = ext
							break
					}
				} else {
					var highlightedText = ev.editor.getSelection().getNative().toString()
					urlField.setValue(usdlc.camelCase(highlightedText))
				}
				initialising = true
				linkRadio.setValue(linkRadioDefault)
				linkSelect.setValue(actorDefault)
				initialising = false
			}

			var urlOptions = infoTab.get('urlOptions').children.push({
				type : 'hbox',
				children : [
					{
						type : 'radio',
						id : 'linkRadio',
						label : 'Link Type',
						items : [
							['Child Page',''],
							['Sibling Page','html'],
							['Actor']
						],
						onClick : function() {
							setLinkType()
						}
					},
					{
						type : 'select',
						id : 'linkSelect',
						label : 'Actor Type',
						items : actorItems,
						onChange : function() {
							if (! initialising) {
								linkRadio.setValue('Actor')
								setLinkType()
							}
						},
						validate : function() {
							var ext = usdlc.splitUrl(urlField.getValue()).ext
							if (ext && ext != 'html' && actorTypes.toString().indexOf(ext) == -1) {
								actorTypes.push(ext)
								actorTypes.sort()
								this.add(ext)
								$.cookie('actorTypes', actorTypes.toString(), {expires : 1000})
							}
							return true
						}
					}
				]
			})
		}
	})
})
