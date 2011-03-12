//  fix: uncomment
///*
// * Copyright 2011 Paul Marrington for http://usdlc.net
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package net.usdlc
//
//import groovy.xml.StreamingMarkupBuilder
//
// /**
// * User: Paul Marrington
// * Date: 29/12/10
// * Time: 9:30 PM
// */
//class Run {
//	static slurper = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())
//	def out = new StreamingMarkupBuilder()
//	def html
//	def printer
//	def namespace
//	/**
//	 * Constructor to create a Run object, given a file mime-type
//	 * @param file mime-type container for the file to run - gives path, name, etc
//	 * @return A run instance ready to use for a specific file.
//	 */
//	static file(File file, printer) {
//		def pageRunner = new Run(printer: printer)
//		pageRunner.html = slurper.parseText(new String(Store.root(file.filePath).read()))
//		pageRunner.namespace = pageRunner.html[0].namespaceURI()
//		return pageRunner;
//	}
//	/**
//	 * Called by the cgi script to run the contents of a usdlc html file.
//	 * @param section Section to run - null for whole file
//	 * @param continuation Run either one section or from here to the end of the file.
//	 */
//	def selection(section, continuation) {
//		if (section) {
//			if (continuation) {
//				def found = false
//				html.breadthFirst().findAll {it.@contextmenu == 'section'}.each {
//					if (it.@id == section) { found = true }
//					if (found) { process(it) }
//				}
//			} else {
//				process(html.breadthFirst().find {it.@id == section})
//			}
//		} else {
//			all()
//		}
//	}
//	/**
//	 For inner pages we will run all sections on the page...
//	 */
//	def all() {
//		html.breadthFirst().findAll {it.@contextmenu == 'section'}.each { process(it) }
//	}
//	/**
//	 * Given an XML fragment, look for all links and process those that are internal to the site.
//	 * @param html HTML fragment - usually for a named section
//	 */
//	private process(html) {
//		if (!html) { return }
//		printer out.bind {
//			mkp.declareNamespace('': namespace)
//			mkp.yield html
//		}
//		html.breadthFirst().findAll { it.name() == 'a' }.each {
//			Filer file = new Filer(it.@href)
//			if (file.clientExt == 'html' && !file.serverExt) {
//				/*
//				 This is a link to another uSDLC page. Follow it.
//				 */
//				net.usdlc.Run.file(file.filePath).all()
//			} else {
//				/*
//				 Process active link here and now.
//				 */
//				printer "Running $file.filePath<br>"
//			}
//		}
//	}
//}
