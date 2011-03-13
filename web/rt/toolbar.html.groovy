/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
doc.span(class: 'ui-widget-header ui-corner-all toolbar') {
	if (userId == 'anon') {
		button('Log In', icon: 'unlocked')
	} else {
		button("Log out $userId", icon: 'locked')
	}
	button('Run Page', icon: 'play', onClick: 'usdlc.runPage()')
	span(class: 'buttonset') {
		input(id: 'EditButton', name: 'edit-run', type: 'radio', checked: "checked", onClick: 'usdlc.setEditMode(true)')
		label('Edit', 'for': 'EditButton')
		input(id: 'RunButton', name: 'edit-run', type: 'radio', onClick: 'usdlc.setEditMode(false)')
		label('Run', 'for': 'RunButton')
	}
	button('Options', icon: 'wrench', onclick: 'window.location="/rt/maintenance/index.html"')
	button('Help', icon: 'help', onClick: 'usdlc.help();return false')
}
