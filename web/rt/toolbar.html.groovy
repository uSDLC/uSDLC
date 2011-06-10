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
doc.span(class: 'redBox') {
	a(href: 'javascript:usdlc.logIn();usdlc.toggleShow("#LogOut","#LogIn")', id: "LogIn", "Log In")
	a(href: 'javascript:usdlc.logIn();usdlc.toggleShow("#LogIn","#LogOut")', id: "LogOut", "Log Out", style: 'display:none;')
	a(href: 'javascript:usdlc.setEditMode(false);usdlc.toggleShow("#RunButton","#EditButton")', id: "EditButton", "Edit Mode", title: "switch to run mode")
	a(href: 'javascript:usdlc.setEditMode(true);usdlc.toggleShow("#EditButton","#RunButton")', id: "RunButton", "Run Mode", style: 'display:none;', title: "switch to edit mode")
	a(href: 'javascript:usdlc.runPage()', id: "RunPage", "Run Page")
}
