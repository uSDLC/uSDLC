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
package net.usdlc.server.standalone

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.util.concurrent.Executors
import net.usdlc.Config
import net.usdlc.Exchange
import net.usdlc.Store

/**
 * This is a Groovy script that starts up a web server to serve uSDLC content. Content is retrieved from the
 * current directory when this script is run plus a web base in the config file. You can also set the port
 * to be used in the config file. "urlBase" is usually empty for the stand-aloner server. It is used by hosted
 * environments where uSDLC is on a sub-path - as in http://askowl.com.au/usdlc.
 */
def host = InetAddress.localHost.hostName
println "Starting uSDLC on http://$host:$Config.web.port/$Config.web.urlBase from ${new File(Store.webBase as String).absolutePath}"
// Point grape cache to a place inside of uSDLC. If you are mixing with an IDE or the Grape command line, you may want to use "mklink /j" to connect ~/.groovy and usdlc/lib directories
System.setProperty('grape.root', "${new File(Config.web.grapeRoot as String).canonicalPath}")

HttpServer server = HttpServer.create(new InetSocketAddress(Config.web.port), 0)
server.createContext '/', { HttpExchange httpExchange ->
	try {
		/*
		 * Fetch the header from the client and load in additional needed information.
		 */
		def header = new HashMap(httpExchange.requestHeaders)
		header.method = httpExchange.requestMethod
		header.query = httpExchange.requestURI.query
		header.uri = httpExchange.requestURI.path
		header.fragment = httpExchange.requestURI.fragment
		header.in = httpExchange.requestBody
		header.out = new PrintStream(httpExchange.responseBody, true)
		/*
		 Call uSDLC common code to create a HTTP exchange object. It prepares ready to send the response header. This means that all connections close between exchanges. This is the best approach for local programs as it keeps things clean. For Internet applications, static files give their length in the response header so that the connection with the browser can stay open for multiple exchanges. Most HTTP servers pre-process the request and response headers and don't allow is to write anything to the client until the response header is done. Since we want longer running responses to display progress information in the browser we need a connection that is available immediately and is closed when done.
		 */
		def exchange = new Exchange(header)
		exchange.responseHeader.each { key, value -> httpExchange.responseHeaders.add(key, value) }
		httpExchange.sendResponseHeaders 200, 0
		// Call the common uSDLC code for a HTTP exchange (request/response pair). This same method will be used by hosted environments also. At this point we have already processed the header. It will always close the connection after the process is complete.
		exchange.talk()
	} catch (Throwable exception) {
		/*
		 Nice simple error process - dump the stack to the server console (stderr) and  return what is probably a blank page. This is great when running from an IDE as you can click on links to see where the problem lies. If the server is run from a command line you can
		 */
		exception.printStackTrace()
	}
} as HttpHandler
server.executor = Executors.newCachedThreadPool()
server.start();
