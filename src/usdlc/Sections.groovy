package usdlc
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

import groovy.xml.StreamingMarkupBuilder

class Sections {
	static slurper = new XmlSlurper(new org.cyberneko.html.parsers.SAXParser())
	def out = new StreamingMarkupBuilder(), html, namespace, name
	//@Delegate GPathResult inFocus
	/**
	 * Given a uSdlc html content file, provide action to the individual components.
	 * @param file mime-type container for the file to run - gives path, name, etc
	 * @return A run instance ready to use for a specific file.
	 */
	Sections(file) {
		name = file
		html = slurper.parseText(new String(Store.root(name).read()))
		namespace = html[0].namespaceURI()
	}
	/**
	 For inner pages we will run all sections on the page...
	 */
	def each(closure) {
		html.breadthFirst().findAll {it.@contextmenu == 'section'}.each {
			closure(inFocus = it)
		}
	}
}
