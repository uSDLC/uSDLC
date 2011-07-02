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

import usdlc.server.Header
import usdlc.server.Request
import usdlc.server.Response
import static init.Config.config

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
	static void exchange(Request request, Response response, Closure startResponse) {
		// Massage the path so that it will point to the correct place for this server
		Header header = request.header()
		Filer file = file(header)
		String script = file.store.path
		Map query = request.query()
		String mimeType = query['mimeType'] ?: file.mimeType()

		response.contentType(mimeType)
		response.session(request.session())
		startResponse()

		try {
			switch (query['action']) {
				case 'save':    // saves html and actor
					// Contents to write are sent from the browser. Get them and save them to the file
					file.save(request.body())
					response.write "usdlc.highlight('sky');"
					if (query?.after) response.write query['after']
					break
				case 'raw':    // so actor are send to browser for editing instead of running
					response.write file.rawContents
					break
				case 'run':
					file.pageRunner { file.actorRunner() }
					break
				default:        // act for active, return content for static content
					if (file.actor) {
						file.actorRunner()
					} else {
						response.write file.contents
					}
			}
		} catch (Throwable problem) {
			response.error problem.message ?: problem.class.name
			problem.printStackTrace()
		} finally {
			// No matter what we want to close the connection. Otherwise the browser spins forever (since we are not providing a content length in the response header.
			response.complete()
		}
	}

	private static Filer file(Header header) {
		String path = header.uri()
		if (path.startsWith(config.urlBase)) {
			// For servers that have usdlc on a sub-path - as in http:myServer.com/myApps/usdlc.
			path = path.substring(config.urlBase.size())
		}
		//Filer is full of magic - including deciding whether a file is client or server.
		Filer file = new Filer(path)
		if (!file.fullExt) {
			// The path does not have a dot that we can use to infer file type. Assume it is a directory and add a trailing slash if there is not already one preset and load it as a new page.
			file = new Filer(path = config.rootFile)
		}
		return file
	}
	// Point Apache Commons logging to a uSDLC proxy.
	static { Log.apacheCommons() }
}
