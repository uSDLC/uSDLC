package usdlc.server.servletengine;
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

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import usdlc.Environment
import usdlc.Exchange

/**
 * User: Paul Marrington
 * Date: 11/05/11
 * Time: 9:14 PM
 */
public class UsdlcServlet extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			def env = Environment.session()
			env.in = request.inputStream
			env.out = new PrintStream(response.outputStream, true)
			// Fetch the header from the client and load in additional needed information.
			def header = new HashMap()
			request.headerNames.each { name ->
				header[name] = request.getHeader(name)
			}
			header.method = request.method
			header.query = request.parameterMap
			header.uri = request.requestURL as String
			int hash = header.uri.indexOf('#')
			header.fragment = (hash == -1) ? "" : header.uri.substring(hash + 1);
			// Call uSDLC common code to create a HTTP exchange object. It prepares ready to send the response header. This means that all connections close between exchanges. This is the best approach for local programs as it keeps things clean. For Internet applications, static files give their length in the response header so that the connection with the browser can stay open for multiple exchanges. Most HTTP servers pre-process the request and response headers and don't allow is to write anything to the client until the response header is done. Since we want longer running responses to display progress information in the browser we need a connection that is available immediately and is closed when done.
			Exchange exchange = new Exchange(header);
			exchange.responseHeader.each { key, value -> response.addHeader(key, value) }
			response.setStatus 200, "OK"
			// Call the common uSDLC code for a HTTP exchange (request/response pair). This same method will be used by hosted environments also. At this point we have already processed the header. It will always close the connection after the process is complete.
			exchange.talk()
		} catch (Throwable exception) {
			// Nice simple error process - dump the stack to the usdlc.server.servletengine.server console (stderr) and  return what is probably a blank page. This is great when running from an IDE as you can click on links to see where the problem lies. If the usdlc.server.servletengine.server is run from a command line you can
			exception.printStackTrace();
		}
	}
}