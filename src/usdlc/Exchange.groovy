/*
 * Copyright 2011 the Authors for http://usdlc.net
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

import usdlc.actor.Actor
import static init.Config.config
import static usdlc.Log.apacheCommons
import static usdlc.MimeTypes.mimeType

/**
 * Core Processor for uSDLC - no matter which web usdlc.server.servletengine.server is in vogue. It is uses as follows
 * for each http request the usdlc.server.servletengine.server receives for it.
 *
 *
 * User: Paul Marrington
 * Date: 31/10/2010
 * Time: 7:52:40 PM
 */
class Exchange {

	static class Header {
		String host, method, query, uri, fragment, cookie
	}
	Header header

	class Request {
		InputStream inputStream
		Header header
		Map<String, String> query, cookies
		String userId, session

		String body() { inputStream.text }
	}
	Request request

	Exchange request(InputStream inputStream, Header header) {
		request = new Request()
		request.inputStream = inputStream
		request.header = header
		request.with {
			query = Dictionary.query(header.query)
			cookies = Dictionary.cookies(header.cookie)
			userId = cookies['userId'] ?: 'anon'
			session = cookies['session'] ?: {
				long before = lastSessionKey
				lastSessionKey = System.currentTimeMillis()
				if (lastSessionKey == before) lastSessionKey++
				lastSessionKey
			}()
		}
		setStore(header.uri)
		this
	}

	Response response
	class Response {
		PrintStream out
		Map header = ['Connection': 'close']

		void setSession(String to) { header['Set-Cookie'] = "session=$to" }

		void setContentType(String mimeType) { header['Content-Type'] = mimeType }

		void write(Object text) { out.print text.toString() }

		void write(byte[] bytes) { out.write bytes }

		void complete() { out.close() }
	}

	void response(OutputStream outputStream, Closure prepare) {
		try {
			response = new Response()
			response.out = new PrintStream(outputStream, true)
			response.contentType = request.query.mimeType ?: mimeType(store.path)
			response.session = request.session
			prepare()
			switch (request.query['action']) {
				case 'save':    // saves html and actor
					// Contents to write are sent from the browser. Get them and save them to the file
					save()
					break
				case 'raw':    // so actors are send to browser for editing instead of running
					response.write store.read()
					break
				case 'run':
					Actor.wrap([store] as List, [exchange: this])
					break
				default:        // act for active, return content for static content
					Actor actor = Actor.load(store)
					if (actor) {
						actor.run([exchange: this])
					} else {
						response.write store.read()
					}
			}
		} catch (problem) {
			problem.printStackTrace()
		}
		response.out.close()
	}

	static long lastSessionKey = 0
	Store store

	void setStore(String path) {
		if (path.startsWith(config.urlBase)) {
			// For servers that have usdlc on a sub-path - as in http:myServer.com/myApps/usdlc.
			path = path.substring(config.urlBase.size())
		}
		store = Store.base(path)
		if (store.path.indexOf('.') == -1) store = store.rebase('index.html')
	}

	private void save() {
		def history = new History(store.path, 'updates')
		def newContents = request.body()
		// If we don't have a history file for any reason, then we should save the contents of the full file first.
		String before = (history.store.size() < 3) ? '' : newContents
		// Save changed contents to disk
		store.write newContents.bytes
		// Create a history file so we can rebuild any version if and when we want to.
		history.save(request.userId, before, newContents)

		response.write "usdlc.highlight('sky');"
		response.write request.query['after'] ?: ''
	}

	// Point Apache Commons logging to a uSDLC proxy.
	static { apacheCommons() }
}
