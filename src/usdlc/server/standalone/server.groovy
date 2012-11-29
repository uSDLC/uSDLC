package usdlc.server.standalone

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import usdlc.Desktop
import usdlc.Exchange
import usdlc.Exchange.Header
import usdlc.config.Config

import java.util.concurrent.Executors

import static usdlc.config.Config.config
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import usdlc.HttpClient

/**
 * This is a Groovy script that starts up a web server to serve uSDLC content.
 * Configuration is take from a configuration DSL combined with parameters
 * from the command line.
 * By default the configuration file is ./usdlc/WEB-INF/web.groovy.
 * You can moveTo to a new base directory (and WEB-INF file) by setting
 * baseDirectory on the command line:
 *
 * uSDLC baseDirectory=~/uSDLC
 */
Config.load('standalone', args)

/*def host = InetAddress.localHost.hostAddress*/
def host = "localhost"
def baseUrl = "http://$host:$config.port"
println "Starting uSDLC on $baseUrl from ${new File('.').absolutePath}"
println '''
Copyright 2010-12 the Authors for http://usdlc.net
use http://github/usdlc/usdlc to confirm author contribution

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
'''

HttpServer server
def socket = new InetSocketAddress(config.port as Integer)
try {
	server = HttpServer.create(socket, 0)
} catch (BindException be) {
	//noinspection GroovyEmptyCatchBlock
	try {
		def url = "${baseUrl}/usdlc/rt/util/exit.groovy?action=stop"
		new HttpClient(secondsTimeout: 0).get(url)
	} catch (anyException) {}
	server = HttpServer.create(socket, 0)
}
server.createContext '/', { HttpExchange httpExchange ->
	httpExchange.with {
		def rh = requestHeaders, ru = requestURI
		def header = new Header(
				host: rh.Host[0],
				method: requestMethod,
				query: ru.query,
				uri: ru.path,
				fragment: ru.fragment,
				cookie: (rh.Cookie ?: [''])[0],
				acceptEncoding: (rh?.'Accept-Encoding' ?: [''])[0],
				contentType: (rh.'Content-type' ?: [''])[0],
		)
		Exchange exchange = new Exchange()
		exchange.processRequest(requestBody, header).
				loadResponse(responseBody) {
			exchange.response.header.each { key, value ->
				httpExchange.responseHeaders.add(key, value)
			}
			httpExchange.sendResponseHeaders 200, 0
		}
	}
} as HttpHandler

server.executor = Executors.newCachedThreadPool()
server.start()
if (! config.noBrowser) {
	Desktop.openURL("http://localhost:$config.port/usdlc/home")
}
