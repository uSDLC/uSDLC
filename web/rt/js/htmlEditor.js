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
			usdlc.savePageContents()
		},
		actorDefault : usdlc.cookie("actorDefault") || 'groovy',
		reportsTabData : {
			id : 'reportsTab',
			label : 'Reports',
			accessKey : 'R',
			elements : [ {
				name : "Report Name",
				type : 'select',
				label : 'Report Name',
				id : 'reportName',
				items : [],
				onChange : function() {
					var value = this.getValue()
					if (value) {
						usdlc.htmlEditorLinkUrlField.setValue(value)
					}
				}
			} ]
		},
		reportItems : function(items) {
			usdlc.reportsTabData.elements[0].items = items
		}
	})
	// fetch data for report form
	$.ajax({
		url : usdlc.urlBase + "/rt/reports/support/reportList_js.groovy",
		dataType : 'script'
	})
	/*
	 * Triggered by the user when they choose to edit a paragraph. creates a
	 * CKEDITOR, stashes initial contents and prepares for a save on exit. The
	 * save sends a diff string to the usdlc.server.servletengine.server so that
	 * full history is recorded.
	 */
	function editSection(section) {
		var $section = $(section)
		usdlc.setFocus($section)
		usdlc.highlight(false) // remove highlighting so save will stand out
		usdlc.clearSynopses()
		usdlc.scrollTo($section)
		$section.ckeditor(function() {
			usdlc.modalOn()
		}, {
			bodyClass : "wayOnTop",
			saveFunction : function(editor) {
				var updateContents = editor.checkDirty()
				usdlc.modalOff()
				// Get rid of the editor so that it does not show up in the
				// saved content.
				editor.destroy()
				if (!updateContents) {
					usdlc.scrollBack()
					usdlc.synopses()
					return
				}
				usdlc.saveSection($section)
				usdlc.scrollBack()
			},
			on : {
				instanceReady : function(ev) {
					// Once the editor is ready for action keep a copy of the
					// unchanged contents and sent the focus.
					ev.editor.focus()
				}
			},
			extraPlugins : 'autogrow'
		// autoGrow_maxHeight : 800
		})
	}

	CKEDITOR.config.toolbar_Full = [ [ 'Source', '-', 'Save',/* 'NewPage','Preview','-', */'Templates' ],
			[ 'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Print', 'SpellChecker', 'Scayt' ],
			[ 'Undo', 'Redo', '-', 'Find', 'Replace', '-', 'SelectAll', 'RemoveFormat' ],
			[ 'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField' ],
			'/', [ 'Bold', 'Italic', 'Underline', 'Strike', '-', 'Subscript', 'Superscript' ],
			[ 'NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', 'Blockquote', 'CreateDiv' ],
			[ 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock' ], [ 'BidiLtr', 'BidiRtl' ],
			[ 'Link', 'Unlink', 'Anchor' ],
			[ 'Image', 'Flash', 'Table', 'HorizontalRule', 'Smiley', 'SpecialChar', 'PageBreak', 'Iframe' ], '/',
			[ 'Styles', 'Format', 'Font', 'FontSize' ], [ 'TextColor', 'BGColor' ],
			[ 'Maximize', 'ShowBlocks', '-', 'About' ] ]
	/*
	 * By default CKEDIT does not have a ready save button unless it is inside a
	 * form element. As we are ajax it, enable save and make sure it does the
	 * job we want.
	 */
	CKEDITOR.plugins.registered['save'] = {
		init : function(editor) {
			editor.addCommand('save', {
				modes : {
					wysiwyg : 1,
					source : 1
				},
				exec : editor.config.saveFunction
			});
			editor.ui.addButton('Save', {
				label : 'Save',
				command : 'save'
			});
		}
	}
	/*
	 * CKEditor mods to turn it into a mean uSDLC machine
	 */
	CKEDITOR.on('dialogDefinition', function(ev) {
		var dialogName = ev.data.name;
		var dialogDefinition = ev.data.definition;
		if (dialogName == 'link') {
			// ////// Update the Info Tab
			// Set the protocol to local as the most common
			// type.
			var infoTab = dialogDefinition.getContents('info')
			var protocol = infoTab.get("protocol")
			protocol.items.unshift([ 'local', '' ])
			protocol['default'] = 'local'

			var urlField = null, linkSelect = null, linkRadio = null, initialising = false
			// Called when user selects a type of link (java,
			// groovy, geb, cs, etc)
			function setLinkType() {
				var type = linkRadio.getValue()
				$.cookie('linkRadioDefault', type)
				if (type == 'Actor') {
					type = usdlc.actorDefault = linkSelect.getValue()
					usdlc.setCookie('actorDefault', type)
				}

				var name = usdlc.splitUrl(urlField.getValue()).name
				if (type) {
					name += '.' + type
				}
				urlField.setValue(name)
			}

			// We save the actor type in a cookie for default -
			// using the extension for existing
			var actorItems = []
			var linkRadioDefault = usdlc.cookie('linkRadioDefault') || ''
			var actorTypes = (usdlc.cookie('actorTypes') || 'groovy').split(',')
			for (actorType in actorTypes) {
				actorItems.push([ actorTypes[actorType] ])
			}
			// On focus we need to get the actor type from the
			// file extension.
			dialogDefinition.onFocus = function() {
				var actorDefault = usdlc.actorDefault
				urlField = usdlc.htmlEditorLinkUrlField = this.getContentElement('info', 'url');
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
					linkRadioDefault = ''
				}
				initialising = true
				linkRadio.setValue(linkRadioDefault)
				linkSelect.setValue(actorDefault)
				initialising = false
			}
			// Add all the new link stuff related to actor and
			// wiki type links.
			infoTab.get('urlOptions').children.push({
				type : 'hbox',
				children : [ {
					type : 'radio',
					id : 'linkRadio',
					label : 'Link Type',
					items : [ [ 'Child Page', '' ], [ 'Sibling Page', 'html' ], [ 'Actor' ] ],
					onClick : function() {
						setLinkType()
					}
				}, {
					type : 'select',
					id : 'linkSelect',
					label : 'Actor Type',
					items : actorItems,
					onChange : function() {
						if (!initialising) {
							linkRadio.setValue('Actor')
							setLinkType()
						}
					},
					validate : function() {
						// get the extension and
						// update the cookie for
						// actor types & default
						var ext = usdlc.splitUrl(urlField.getValue()).ext
						if (ext && ext != 'html' && actorTypes.toString().indexOf(ext) == -1) {
							actorTypes.push(ext)
							actorTypes.sort()
							this.add(ext)
							usdlc.setCookie('actorTypes', actorTypes.toString())
						}
						return true
					}
				} ]
			})
			// ////// Create new reports tab
			dialogDefinition.addContents(usdlc.reportsTabData);
		}
	})
})
