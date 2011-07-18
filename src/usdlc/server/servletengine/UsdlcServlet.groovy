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
package usdlc.server.servletengine;


import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import usdlc.Exchange
import usdlc.Exchange.Header

/**
 * User: Paul Marrington
 * Date: 11/05/11
 * Time: 9:14 PM
 */
class UsdlcServlet extends HttpServlet {
	/**
	 * In the uSDLC world Post and Get are the same (for now)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) { doGet(request, response); }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		initialiseUsdlc()
		Exchange exchange = new Exchange()
		exchange.request(request.inputStream, loadHeader(request)).response(response.outputStream) {
			exchange.response.header.each { key, value -> response.addHeader(key, value) }
			response.setStatus 200, 'OK'
		}
	}

	private loadHeader(HttpServletRequest request) {
		String uri = request.requestURI
		int hash = uri.indexOf('#')
		String fragment = (hash == -1) ? '' : header.uri.substring(hash + 1)
		Map query = request.parameterMap.collectEntries { String key, String[] value -> [key.toLowerCase(), value[0]] }

		new Header(
				host: request.getHeader('host'), method: request.method, query: query, uri: uri,
				fragment: fragment, cookie: requestHeaders['Cookie'] ?: ''
		)
	}

	private initialiseUsdlc() {
		if (!init.Config.config) {
			// Find the current directory as the base of the web directory
			init.Config.load(servletConfig.servletContext.getRealPath('/'), [])
		}
	}
}
