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
/**
 * User: Paul Marrington
 * Date: 30/04/11
 * Time: 12:55 PM
 */
class HtmlEditorModule extends geb.Module {
	static content = {
		/**
		 * Elements of the editor
		 */
		editor(required: false) { $("table.cke_editor") }
		saveButton(required: false) {$("a.cke_button_save") }
		urlInput(required: false) { $("label", text: "URL").next().find('input') }

		dialog(required: false) { $("table.cke_dialog") }
		cancelButton(required: false) { $("a.cke_dialog_ui_button_cancel") }

		reportTab(required: false) { $("div[name=reportsTab]") }
		reportNameSelect(required: false) { $("label", text: "Report Name").next().find('select') }

		/**
		 * Edit the section in focus. - brings up an instance of CKEdit
		 */
		editSection {
			js."usdlc.editSectionInFocus"()
			waitfor { editor.present }
			return this
		}
		/**
		 * Press a named button on the ckedit button bar
		 */
		pressButton { title ->
			$("a.cke_button_link[title=$title]").click()
			waitfor { dialog.present }
			return this
		}
		/**
		 * Select a tab on a ckeditor dialog box
		 */
		selectTab { tab ->
			$(/a.cke_dialog_tab[title="$tab"]/).click()
			waitfor { $("a.cke_dialog_tab_selected").@title == tab }
			return this
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
	static authority = usdlc.Environment.host
	static url = "/root"
	static content = {
		/**
		 * Highlight the first section on a page
		 */
		firstSection { $("div#s1") }
		htmlEditor { module HtmlEditorModule }
	}
}

class Sandbox extends UsdlcPage {
	static url = "/Sandbox/IDoNotExist"
	/**
	 * Use this to specify which page in the Sandbox to use next. Do this before browse() or to()
	 * @param to Page name
	 */
	static to(url) { Sandbox.url = "/Sandbox/$url" }
}
