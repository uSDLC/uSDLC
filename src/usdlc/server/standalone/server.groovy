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
package usdlc.server.standalone

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.util.concurrent.Executors
import usdlc.Exchange
import usdlc.Exchange.Header
import usdlc.Config
import static usdlc.Config.config

/**
 * This is a Groovy script that starts up a web server to serve uSDLC content.
 * Configuration is take from a configuration DSL combined with parameters from the command line.
 * By default the configuration file is ./web/WEB-INF/web.groovy.
 * You can move to a new base directory (and WEB-INF file) by setting baseDirectory on the command line:
 *
 * uSDLC baseDirectory=~/uSDLC
 */
Config.load('standalone', 'web', args)

def host = InetAddress.localHost.hostAddress
def baseUrl = "http://$host:$config.port/$config.urlBase"
System.out.println "Starting uSDLC on $baseUrl from $config.baseDirectory/"

HttpServer server
def socket = new InetSocketAddress(config.port)
try {
	server = HttpServer.create(socket, 0)
} catch (BindException be) {
	try { "${baseUrl}rt/util/exit.groovy?action=stop".toURL().text } catch (e) { /* Not needed */ }
	server = HttpServer.create(socket, 0)
}
server.createContext '/', { HttpExchange httpExchange ->
	httpExchange.with {
		def header = new Header(
				host: requestHeaders.Host[0], method: requestMethod, query: requestURI.query, uri: requestURI.path,
				fragment: requestURI.fragment, cookie: (requestHeaders?.Cookie ?: [''])[0]
				)
		Exchange exchange = new Exchange()
		exchange.request(requestBody, header).response(responseBody) {
			exchange.response.header.each { key, value -> httpExchange.responseHeaders.add(key, value) }
			httpExchange.sendResponseHeaders 200, 0
		}
	}
} as HttpHandler

server.executor = Executors.newCachedThreadPool()
server.start();
