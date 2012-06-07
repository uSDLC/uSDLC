$(function () {
	window.usdlc.editSectionInFocus = function () {
		if (usdlc.inFocus) editSection(usdlc.inFocus)
	}
	$.extend(true, window.usdlc, {
		actorDefault:  usdlc.cookie("actorDefault") || 'coffee',
		reportsTabData:{
			id:       'reportsTab',
			label:    'Reports',
			accessKey:'R',
			elements: [
				{
					name:    "Report Name",
					type:    'select',
					label:   'Report Name',
					id:      'reportName',
					items:   [],
					onChange:function () {
						var value = this.getValue()
						if (value) {
							usdlc.htmlEditorLinkUrlField.setValue(value)
						}
					}
				}
			]
		},
		reportItems:   function (items) {
			usdlc.reportsTabData.elements[0].items = items
		}
	})
	// fetch data for report form
	$.ajax({
		url: '/usdlc/rt/reports/support/reportList_js.groovy',
		dataType:'script'
	})
	/*
	 * Triggered by the user when they choose to edit a paragraph. creates a
	 * CKEDITOR, stashes initial contents and prepares for a save on exit. The
	 * save sends a diff string to the usdlc.server.servletengine.server so that
	 * full history is recorded.
	 */
	function editSection($section) {
		usdlc.clearSynopses()
		usdlc.menuToTop()
		usdlc.scrollTo($section)
		usdlc.clearFocus()
		$section.ckeditor(function () {
			usdlc.modalOn()
		}, {
			skin: 'v2',
			bodyClass:   "wayOnTop",
			saveFunction:function (editor) {
				var updateContents = editor.checkDirty()
				usdlc.modalOff()
				// Get rid of the editor so that it does not show up in the
				// saved content.
				editor.destroy()
				usdlc.setFocus($section)
				if (updateContents) {
					usdlc.saveSection($section)
				} else {
					usdlc.synopses()
				}
			},
			on:          {
				instanceReady:function (ev) {
					// Once the editor is ready for action keep a copy of the
					// unchanged contents and sent the focus.
					ev.editor.focus()
				}
			},
			extraPlugins:'autogrow',
			autoGrow_maxHeight : usdlc.pageContents.height() / 2 - 32,

			keystrokes: [
				[ CKEDITOR.CTRL + 81 /*Q*/, 'blockquote' ],
				[ CKEDITOR.CTRL + 66 /*B*/, 'bold' ],
				[ CKEDITOR.CTRL + 56 /*8*/, 'bulletedlist' ],
				[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 56 /*8*/, 'bulletedListStyle' ],
				[ CKEDITOR.CTRL + 77 /*M*/, 'indent' ],
				[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 77 /*M*/, 'outdent' ],
				[ CKEDITOR.CTRL + 73 /*I*/, 'italic' ],
				[ CKEDITOR.CTRL + 74 /*J*/, 'justifyblock' ],
				[ CKEDITOR.CTRL + 69 /*E*/, 'justifycenter' ],
				[ CKEDITOR.CTRL + 76 /*L*/, 'justifyleft' ],
				[ CKEDITOR.CTRL + 82 /*R*/, 'justifyright' ],
				[ CKEDITOR.CTRL + 55 /*7*/, 'numberedlist' ],
				[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 55 /*7*/, 'numberedListStyle' ],
				[ CKEDITOR.CTRL + 89 /*Y*/, 'redo' ],
				[ CKEDITOR.CTRL + 32 /*SPACE*/, 'removeFormat' ],
				[ CKEDITOR.CTRL + 65 /*A*/, 'selectall' ],
				[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 88 /*X*/, 'strike' ],
				[ CKEDITOR.CTRL + 188 /*COMMA*/, 'subscript' ],
				[ CKEDITOR.CTRL + 190 /*PERIOD*/, 'superscript' ],
				[ CKEDITOR.CTRL + 85 /*U*/, 'underline' ],
				[ CKEDITOR.CTRL + 90 /*Z*/, 'undo' ],
				// Insert
				[ CKEDITOR.ALT + 65 /*A*/, 'anchor' ],
				[ CKEDITOR.ALT + 68 /*D*/, 'creatediv' ],
				[ CKEDITOR.ALT + CKEDITOR.SHIFT + 68 /*D*/, 'editdiv' ],
				[ CKEDITOR.ALT + 70 /*F*/, 'flash' ],
				[ CKEDITOR.ALT + 72 /*H*/, 'horizontalrule' ],
				[ CKEDITOR.CTRL + 57 /*9*/, 'image' ],
				[ CKEDITOR.ALT + 73 /*I*/, 'image' ],
				[ CKEDITOR.CTRL + 75 /*K*/, 'link' ],
				[ CKEDITOR.ALT + 76 /*L*/, 'link' ],
				[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 75 /*K*/, 'unlink' ],
				[ CKEDITOR.ALT + CKEDITOR.SHIFT + 76 /*L*/, 'unlink' ],
				[ CKEDITOR.CTRL + 13 /*ENTER*/, 'pagebreak' ],
				[ CKEDITOR.ALT + 13 /*ENTER*/, 'pagebreak' ],
				[ CKEDITOR.ALT + 86 /*V*/, 'pastetext' ],
				[ CKEDITOR.ALT + CKEDITOR.SHIFT + 86 /*V*/, 'pastefromword' ],
				[ CKEDITOR.ALT + 69 /*E*/, 'smiley' ],
				[ CKEDITOR.ALT + 67 /*C*/, 'specialchar' ],
				[ CKEDITOR.ALT + 84 /*T*/, 'table' ],
				[ CKEDITOR.ALT + 79 /*O*/, 'templates' ],
				// Other - dialogs, views, etc.
				[ CKEDITOR.ALT + 8 /*Backspace*/, 'blur' ],
				[ CKEDITOR.ALT + CKEDITOR.SHIFT + 67 /*C*/, 'colordialog' ],
				[ CKEDITOR.ALT + 77 /*M*/, 'contextMenu' ],
				[ CKEDITOR.ALT + 122 /*F11*/, 'elementsPathFocus' ],
				[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 70 /*F*/, 'find' ],
				[ CKEDITOR.ALT + 88 /*X*/, 'maximize' ],
				[ CKEDITOR.CTRL + 113 /*F2*/, 'preview' ],
				[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 80 /*P*/, 'print' ],
				[ CKEDITOR.CTRL + 72 /*H*/, 'replace' ],
				[ CKEDITOR.ALT + 83 /*S*/, 'scaytcheck' ],
				[ CKEDITOR.ALT + 66 /*B*/, 'showblocks' ],
				[ CKEDITOR.ALT + CKEDITOR.SHIFT + 84 /*T*/, 'showborders' ],
				[ CKEDITOR.ALT + 90 /*Z*/, 'source' ],
				[ CKEDITOR.ALT + 48 /*ZERO*/, 'toolbarCollapse' ],
				[ CKEDITOR.ALT + 121 /*F10*/, 'toolbarFocus' ],
				[ CKEDITOR.CTRL + 83 /*S*/, 'save' ]
			]
		})
	}

	CKEDITOR.config.toolbar_Full = [
		[ 'Save', /* 'NewPage','Preview','-', 'Templates', 'Print'],
		[*/ 'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord'],
		[ 'Undo', 'Redo', '-', 'Find', 'Replace'],
		[ 'Image', /*'Flash',*/ 'Table', 'HorizontalRule', 'Smiley', 'SpecialChar', 'PageBreak'/*, 'Iframe'*/ ],
		[ 'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField' ],
		'/',
		[ 'Styles', 'Format'],
		['SelectAll', 'RemoveFormat', 'Source'],
		[ 'NumberedList', 'BulletedList'],
		['Outdent', 'Indent', 'Blockquote', 'CreateDiv' ],
		[ 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock' ],
		/*[ 'BidiLtr', 'BidiRtl' ],*/
		'/',
		['Font', 'FontSize' ],
		[ 'Bold', 'Italic', 'Underline', 'Strike'],
		['Subscript', 'Superscript' ],
		[ 'TextColor', 'BGColor'],
		[ 'Link', 'Unlink', 'Anchor' ],
		[ 'Maximize', 'ShowBlocks', 'SpellChecker', 'Scayt']/*,
		['About']*/
	]
	/*
	 * By default CKEDIT does not have a ready save button unless it is inside a
	 * form element. As we are ajax it, enable save and make sure it does the
	 * job we want.
	 */
	CKEDITOR.plugins.registered['save'] = {
		init:function (editor) {
			editor.addCommand('save', {
				modes:{
					wysiwyg:1,
					source: 1
				},
				exec: editor.config.saveFunction
			});
			editor.ui.addButton('Save', {
				label:  'Save',
				command:'save'
			});
		}
	}
	/*
	 * CKEditor mods to turn it into a mean uSDLC machine
	 */
	CKEDITOR.on('dialogDefinition', function (ev) {
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
			var actorTypes = (usdlc.cookie('actorTypes') || 'coffee').split(',')
			for (var actorType in actorTypes) {
				actorItems.push([ actorTypes[actorType] ])
			}
			// On focus we need to get the actor type from the
			// file extension.
			dialogDefinition.onFocus = function () {
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
						case 'htm':
							url = url.substring(0, url.length - 11)
							urlField.setValue(url)
							ext = ''
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
				linkRadio.setValue(linkRadioDefault)
				linkSelect.setValue(actorDefault)
				initialising = false
			}
			// Add all the new link stuff related to actor and
			// wiki type links.
			infoTab.get('urlOptions').children.push({
				type:    'hbox',
				children:[
					{
						type:   'radio',
						id:     'linkRadio',
						label:  'Link Type',
						items:  [
							[ 'Child', '' ],
							[ 'Sibling', 'htm' ],
							//[ 'Download' ],
							[ 'Actor' ]
						],
						onClick:function () {
							setLinkType()
						}
					},
					{
						type:    'select',
						id:      'linkSelect',
						label:   'Actor Type',
						items:   actorItems,
						onChange:function () {
							if (!initialising) {
								linkRadio.setValue('Actor')
								setLinkType()
							}
						},
						validate:function () {
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
					}
				]
			})
			// ////// Create new reports tab
			dialogDefinition.addContents(usdlc.reportsTabData);
		}
	})
//
//	function onDblclick(ev) {
//		if (!usdlc.inEditMode(ev)) {
//			usdlc.setFocus(ev.currentTarget)
//			usdlc.editSectionInFocus()
//			return false
//		} else {
//			return true
//		}
//	}
//	$('.editable').live('dblclick', onDblclick)
})
