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
/*
 The main context menu of uSDLC will display actions required - based on user authentication.
 Apart from printable characters, there are specials and modifiers. The modifiers can be linked together as in ant+shift+A.
            backspace, tab, return, shift, ctrl, alt, pause, capslock, esc, space, pageup, pagedown, end, home, left, up, right, down, insert, del, f1-f12, numlock, scroll, meta (apple command)
 */
doc.ul {
	li {
		a('Clipboard', onclick: 'return false')
		ul {
			li { a('Cut', onclick: 'usdlc.copySectionInFocus("cut")') { kbd('(^X)') } }
			li { a('Copy', onclick: 'usdlc.copySectionInFocus("copy")') { kbd('(^C)') } }
			li {
				a('Paste', onclick: 'return false')
				ul(id: 'pasteList')
			}
		}
	}
	li {
		a('Edit', onclick: 'return false')
		ul {
			li { a('Edit Section', onclick: 'usdlc.editSectionInFocus()') { kbd('(^E)') } }
			li { a('Insert Above', onclick: 'usdlc.insertSectionAboveFocus()') { kbd('(^I)') } }
			li { a('Append Below', onclick: 'usdlc.insertSectionBelowFocus()') { kbd('(^A)') } }
			li { hr() }
			li { a('Move Section Up', onclick: 'usdlc.moveSectionUp()') {kbd('(^up)') } }
			li { a('Move Section Down', onclick: 'usdlc.moveSectionDown()') {kbd('(^down)') } }
			li { hr() }
			li { a('Join with Above', onclick: 'usdlc.joinSectionAbove()') {kbd('(alt+up)') } }
			li { a('Join with Below', onclick: 'usdlc.joinSectionBelow()') {kbd('(alt+down)') } }
			li { hr() }
			li { a('Split up Sections', onclick: 'usdlc.splitIntoSections()') { kbd('(alt+S)') } }
			li { a('Extract to New Page', onclick: 'usdlc.extractSectionInFocus()') { kbd('(alt+E)') } }
		}
	}
	li {
		a('Navigation', onclick: 'return false')
		ul {
			li { a('Section / Tree Focus', onclick: 'usdlc.toggleFocus()') {kbd('(tab)') } }
			li { hr() }
			li { a('Previous Section', onclick: 'usdlc.upFocus()') {kbd('(up)') } }
			li { a('Next Section', onclick: 'usdlc.downFocus()') {kbd('(down)') } }
		}
	}
	li {
		a('Run', onclick: 'return false')
		ul {
			li { a('Run Page', onclick: 'usdlc.runPage({continuation:false})') { kbd('(^P)') } }
			li { a('Run Page from Last Stop', onclick: 'usdlc.runPage({continuation:true})') { kbd('(^L)') } }
			li { a('Run Section', onclick: 'usdlc.runSectionInFocus({continuation:false})') { kbd('(^R)') } }
			li { a('Continue from Section', onclick: 'usdlc.runSectionInFocus({continuation:true})') { kbd('(^O)') }  }
		}
	}
}
