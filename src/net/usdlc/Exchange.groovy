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
package net.usdlc

import java.util.regex.Pattern

/**
 * Core Processor for uSDLC - no matter which web server is in vogue. It is uses as follows for each http request
 * the server receives for it.
 *
 <code>
 Map requestHeader = getHttpRequestHeaderAsMap()
 exchange = new Exchange(requestHeader)
 Map responseHeader = exchange.responseHeader
 writeHttpResponseHeader(responseHeader)
 exchange.talk()
 </code>
 *
 * User: Paul Marrington
 * Date: 31/10/2010
 * Time: 7:52:40 PM
 */

class Exchange {
	/*
	{cookie=[session=1515897161; session=1515897155; session=1515897153; jstree_open=%23%20uSDLC%2C%23%20uSDLC%20Actors; jstree_select=%23%20uSDLC%20Actors%20HtmlUnitActor; currentPage=%2FuSDLC%2FActors%2FHtmlUnitActor%2Findex.html; session=1515897166], host=[127.0.0.1:9000], contenttype=[application/x-www-form-urlencoded], query=action=save, acceptencoding=[gzip,deflate,sdch], acceptcharset=[ISO-8859-1,utf-8;q=0.7,*;q=0.3], contentlength=[35], origin=[http://127.0.0.1:9000], uri=/uSDLC/Actors/HtmlUnitActor/ClickOnSection.htm.groovy, connection=[keep-alive], acceptlanguage=[en-US,en;q=0.8], referer=[http://127.0.0.1:9000/], method=POST, xrequestedwith=[XMLHttpRequest], useragent=[Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.98 Safari/534.13], fragment=null, accept=[]}
	 */
	def requestHeader = [:]
	def responseHeader = [:]
	String path
	private Filer file

	/**
	 * Initialiser is passed the request header as a map. At a minimum it will require a REQUEST_URI entry.
	 *
	 * Environment:
	 * <ul>
	 * <li>in:  request body (set by server)
	 * <li>out: response body (set by server)
	 * <li>header: supplemented request header
	 * <li>script: path to and name of script or page
	 * <li>clientType: Client extension (as in html or jpg)
	 * <li>query: map of name/value items from the command line.
	 * <li>cookies: mape of cookie values sent from browser
	 * <li>userId: User id or anon for none.
	 * </ul>
	 */
	Exchange(Map header) {
		def my = Environment.data()
		// Normalise HTTP request header map - by making lower case and removing bad characters
		my.header = [:]
		header.each { key, value ->
			my.header[key.toLowerCase().replaceAll(badHeaderChars, '')] = value
		}
		// Massage the path so that it will point to the correct place for this server
		path = my.header.uri
		if (path.startsWith(Config.web.urlBase)) {
			// For servers that have usdlc on a sub-path - as in http:myserver.com/myapps/usdlc.
			path = path.substring(Config.web.urlBase.size())
		}
		//Filer is full of magic - including deciding whether a file is client or server.
		file = new Filer(path)
		if (!file.fullExt) {
			// The path does not have a dot that we can use to infer file type. Assume it is a directory and add a trailing slash if there is not already one preset and load it as a new page.
			file = new Filer(path = Config.web.rootFile)
		}
		// Now that we have a definitive script name, save it for use in executing the Actor.
		my.script = path
		my.clientType = file.clientExt
		// Convert internal maps to a more usable form
		my.query = Dictionary.query(my.header.query)
		my.cookies = Dictionary.cookies(my.header.cookies)
		// We might store more permanent information under the user or session ID.
		my.userId = my.cookies.userId ?: 'anon'
		if (!my.cookies.session) {
			my.cookies.session = session as String
			responseHeader['Set-cookie'] = "session=$my.cookies.session"
			session++
		}
		// Update the response header - given the client mime type and tell browser we intend to close the connection when we are done.
		responseHeader['Content-Type'] = file.mimeType()
		responseHeader['Connection'] = 'close'
	}

	static badHeaderChars = Pattern.compile(/[\W_]/)
	static int session = System.currentTimeMillis()

	/**
	 * Talk back to the client. Decide whether the request was dynamic or static and act accordingly. Sorry, you will have to look at Filer to see the difference.
	 * @return
	 */
	def talk() {
		def my = Environment.data()
		try {
			switch (my.query.action) {
				case 'save':    // saves html and actors
					// Contents to write are sent from the browser. Get them and save them to the file
					file.save(my.userId, my.in.text)
					Browser.js().highlight('sky')
					break
				case 'edit':    // so actors are send to browser for editing instead of running
					my.out.write file.rawContents
					break
				default:        // act for active, return content for static content
					if (file.actor) {
						file.actor.run my.script
					} else {
						my.out.write file.contents
					}
			}
		} catch (problem) {
			problem.printStackTrace()
		} finally {
			// No matter what we want to close the connection. Otherwise the browser spins forever (since we are not providing a content length in the response header.
			my.out.close()
		}
	}
	/**
	 * Point Apache Commons logging to a uSDLC proxy.
	 */
	static { Log.apacheCommons() }
}
