package usdlc.server.standalone
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

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.util.concurrent.Executors
import usdlc.server.Header
import usdlc.server.Request
import usdlc.server.Response
import static init.Config.config
import static usdlc.Exchange.exchange

/**
 * This is a Groovy script that starts up a web server to serve uSDLC content. Configuration is take from a configuration DSL combined with parameters from the command line. By default the configuration file is ./web/WEB-INF/web.groovy. You can move to a new base directory (and WEB-INF file) by setting baseDirectory on the command line:
 *
 * uSDLC baseDirectory=~/uSDLC
 */
init.Config.load('web', args)
config.baseDirectory = new File(config.baseDirectory as String).absolutePath

def host = InetAddress.localHost.hostName
def baseUrl = "http://$host:$config.port/$config.urlBase"
println "Starting uSDLC on $baseUrl\n    from $config.baseDirectory"

HttpServer server
def socket = new InetSocketAddress(config.port)
try {
	server = HttpServer.create(socket, 0)
} catch (BindException be) {
	try { "${baseUrl}rt/util/exit.groovy?action=stop".toURL().text } catch (e) { e.printStackTrace() }
	server = HttpServer.create(socket, 0)
}
server.createContext '/', { HttpExchange httpExchange ->
	try {
		httpExchange.with {
			Request request = new Request(requestBody, new Header(
					requestHeaders.Host[0], requestMethod, requestURI.query, requestURI.path,
					requestURI.fragment, requestHeaders['Cookie'] ?: ''
			))
			Response response = new Response(responseBody)
			exchange(request, response) {
				// Call uSDLC common code to create a HTTP exchange object. It prepares ready to send the response header. This means that all connections close between exchanges. This is the best approach for local programs as it keeps things clean. For Internet applications, static files give their length in the response header so that the connection with the browser can stay open for multiple exchanges. Most HTTP servers pre-process the request and response headers and don't allow is to write anything to the client until the response header is done. Since we want longer running responses to display progress information in the browser we need a connection that is available immediately and is closed when done.
				response.header().each { key, value -> httpExchange.responseHeaders.add(key, value) }
				sendResponseHeaders 200, 0
			}
		}
	} catch (Throwable exception) {
		// Nice simple error process - dump the stack to the usdlc.server.servletengine.server console (stderr) and  return what is probably a blank page. This is great when running from an IDE as you can click on links to see where the problem lies. If the usdlc.server.servletengine.server is run from a command line you can
		exception.printStackTrace()
	}
} as HttpHandler

server.executor = Executors.newCachedThreadPool()
server.start();
