class HtmlEditorModule extends geb.Module {
	static content = {
		/**
		 * Elements of the editor
		 */
		editor(required: false) { $('table.cke_editor') }
		saveButton(required: false) {$('a.cke_button_save') }
		urlInput(required: false) { $('label', text: 'URL').next().find('input') }

		dialog(required: false) { $('table.cke_dialog') }
		cancelButton(required: false) { $('a.cke_dialog_ui_button_cancel') }

		reportTab(required: false) { $('div[name=reportsTab]') }
		reportNameSelect(required: false) { $('label', text: 'Report Name').next().find('select') }

		/**
		 * Edit the section in focus. - brings up an instance of CKEdit
		 */
		editSection {
			js.'usdlc.editSectionInFocus'()
			waitfor { editor.present }
			this
		}
		/**
		 * Press a named button on the ckedit button bar
		 */
		pressButton { title ->
			$('a.cke_button_link[title=$title]').click()
			waitfor { dialog.present }
			this
		}
		/**
		 * Select a tab on a ckeditor dialog box
		 */
		selectTab { tab ->
			$(/a.cke_dialog_tab[title='$tab']/).click()
			waitfor { $('a.cke_dialog_tab_selected').@title == tab }
			this
		}
		/**
		 * Close editor instance if open
		 */
		close {
			if (dialog.present) {
				withConfirm(true) {
					cancelButton.click()
					waitfor { !dialog.present }
				}
			}
			if (saveButton.present) {
				saveButton.click()
				waitfor { !saveButton.present }
			}
		}
	}
}

class UsdlcPage extends geb.Page {
	static authority = ''//exchange.header.host
	static url = '/root'
	static content = {
		/**
		 * Highlight the first section on a page
		 */
		firstSection { $('div#s1') }
		htmlEditor { module HtmlEditorModule }
	}
}

class Sandbox extends UsdlcPage {
	static url = '/Sandbox/IDoNotExist'
	/**
	 * Use this to specify which page in the Sandbox to use next. Do this before browse() or to()
	 * @param to Page name
	 */
	static to(url) { Sandbox.url = "/Sandbox/$url" }
}
