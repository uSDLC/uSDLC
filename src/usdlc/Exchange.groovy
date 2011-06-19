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
package usdlc

import java.util.regex.Pattern

/**
 * Core Processor for uSDLC - no matter which web usdlc.server.servletengine.server is in vogue. It is uses as follows for each http request
 * the usdlc.server.servletengine.server receives for it.
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
		 {cookie=[jstree_open=%23%20uSDLC%2C%23%20uSDLC%20Actors; jstree_select=%23%20uSDLC%20Actors%20HtmlUnitActor; currentPage=%2FuSDLC%2FActors%2FHtmlUnitActor%2Findex.html; session=1515897166], host=[127.0.0.1:9000], contenttype=[application/x-www-form-urlencoded], query=action=save, acceptencoding=[gzip,deflate,sdch], acceptcharset=[ISO-8859-1,utf-8;q=0.7,*;q=0.3], contentlength=[35], origin=[http://127.0.0.1:9000], uri=/uSDLC/Actors/HtmlunitActor/ClickOnSection.htmlunit, connection=[keep-alive], acceptlanguage=[en-US,en;q=0.8], referer=[http://127.0.0.1:9000/], method=POST, xrequestedwith=[XMLHttpRequest], useragent=[Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.98 Safari/534.13], fragment=null, accept=[]}
		  */
	public requestHeader = [:]
	public responseHeader = [:]
	String path
	private Filer file
	/**
	 * Initialiser is passed the request header as a map. At a minimum it will require a REQUEST_URI entry.
	 *
	 * Environment:
	 * <ul>
	 * <li>in:  request body (set by usdlc.server.servletengine.server)
	 * <li>out: response body (set by usdlc.server.servletengine.server)
	 * <li>header: supplemented request header
	 * <li>script: path to and name of script or page
	 * <li>clientType: Client extension (as in html or jpg)
	 * <li>query: map of name/value items from the command line.
	 * <li>cookies: mape of cookie values sent from browser
	 * <li>userId: User id or anon for none.
	 * </ul>
	 */
	Exchange(Map header) {
		def env = Environment.session()
		// Normalise HTTP request header map - by making lower case and removing bad characters
		env.finaliser.exchange = []
		env.header = [:]
		header.each { key, value ->
			env.header[key.toLowerCase().replaceAll(badHeaderChars, '')] = value
		}
		// Massage the path so that it will point to the correct place for this usdlc.server.servletengine.server
		path = env.header.uri
		if (path.startsWith(Config.urlBase)) {
			// For servers that have usdlc on a sub-path - as in http:myserver.com/myapps/usdlc.
			path = path.substring(Config.urlBase.size())
		}
		//Filer is full of magic - including deciding whether a file is client or server.
		file = new Filer(path)
		if (!file.fullExt) {
			// The path does not have a dot that we can use to infer file type. Assume it is a directory and add a trailing slash if there is not already one preset and load it as a new page.
			file = new Filer(path = Config.rootFile)
		}
		env.clientType = file.clientExt
		// Now that we have a definitive script name, save it for use in executing the Actor.
		env.script = file.store.path    // full script/file name
		// Convert internal maps to a more usable form
		env.query = Dictionary.query(env.header.query)
		env.cookies = Dictionary.cookies(env.header.cookie)
		// We might store more permanent information under the user or session ID.
		env.userId = env.cookies.userId ?: 'anon'
		// We keen a session reference as a cookie.
		if (!env.cookies.session) {
			env.cookies.session = session as String
			responseHeader['Set-cookie'] = "session=$env.cookies.session"
			session++
		}
		// Output is mostly by env.doc - be it HTML, XML, Javascript or text (based on mime-type)
		env.doc = BrowserBuilder.newInstance(env.query.mimeType ?: file.mimeType())
		// Update the response header - given the client mime type and tell browser we intend to close the connection when we are done.
		responseHeader['Content-Type'] = env.mimeType
		responseHeader['Connection'] = 'close'
	}

	static badHeaderChars = Pattern.compile(/[\W_]/)
	static long session = System.currentTimeMillis()

	/**
	 * Talk back to the client. Decide whether the request was dynamic or static and act accordingly. Sorry, you will have to look at Filer to see the difference.
	 * @return
	 */
	def talk() {
		def env = Environment.session()
		try {
			switch (env.query.action) {
				case 'save':    // saves html and actor
					// Contents to write are sent from the browser. Get them and save them to the file
					file.save(env.in.text)
					env.out.write "usdlc.highlight('sky');"
					if (env.query?.after) {
						env.out.write env.query.after
					}
					break
				case 'raw':    // so actor are send to browser for editing instead of running
					env.out.write file.rawContents
					break
				case 'run':
					file.pageRunner { file.actorRunner() }
					break
				default:        // act for active, return content for static content
					if (file.actor) {
						file.actorRunner()
					} else {
						env.out.write file.contents
					}
			}
		} catch (Throwable problem) {
			env.doc.error problem.message ?: problem.class.name
			problem.printStackTrace()
		} finally {
			// No matter what we want to close the connection. Otherwise the browser spins forever (since we are not providing a content length in the response header.
			//noinspection GroovyEmptyCatchBlock
			try { env.finaliser.exchange.each { it() } } catch (wasNotOpen) {}
		}

	}
	/**
	 * Point Apache Commons logging to a uSDLC proxy.
	 */
	static { Log.apacheCommons() }
}
