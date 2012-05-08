package usdlc

import usdlc.actor.Actor
import static usdlc.Log.apacheCommons
import static usdlc.MimeTypes.mimeType
import static usdlc.config.Config.config

/**
 * Core Processor for uSDLC - no matter which web server is in
 * vogue. It is uses as follows for each http request the
 * server receives for it.
 */
class Exchange {

	static class Header {
		String host, method, query, uri, fragment, cookie, acceptEncoding
		String contentType
	}
	Header header

	class Request {
		InputStream inputStream
		Header header
		Map query, cookies
		User user
		Session session

		String body() { inputStream.text }
	}
	Request request

	Exchange processRequest(InputStream inputStream, Header header) {
		try {
			request = new Request()
			request.inputStream = inputStream
			request.header = header
			request.with {
				query = Dictionary.query(header.query)
				cookies = Dictionary.cookies(header.cookie)
				session = Session.load(cookies['usdlc-session'] as String)
				session.exchange = this
				user = User.set(session)
			}
			if (header.contentType == 'application/x-www-form-urlencoded') {
				request.query += Dictionary.query(request.body())
			}
			setStore(header.uri)
		} catch (problem) {
			problem.printStackTrace()
		}
		this
	}

	static sessions = [:]

	Response response
	class Response {
		PrintStream out
		Map header = [:]
		def post = '', isStatic = false

		void setSessionCookie(String session) {
			header['Set-Cookie'] = "usdlc-session=$session;Path=/;$expiryDate"
		}

		void print(Object text) {
			String string = text.toString()
			if (first) {
				first = false
				if (string[0] != '<') {
					out.print '<pre>'
					post = '</pre>'
				}
			}
			out.print string
			out.flush()
		}

		boolean first = true

		void write(Object text) {
			out.print text.toString()
			out.flush()
		}

		void write(byte[] bytes) {
			out.write bytes
			out.flush()
		}

		def complete() {
			if (!isStatic) {
				write(post)
				out.close()
			}
			isStatic
		}
	}
	static expiryDate = 'Expires=Sun, 17 Jan 2038 19:14:07 GMT'

	def loadResponse(OutputStream outputStream, Closure sendResponseHeader) {
		this.sendResponseHeader = sendResponseHeader
		try {
			String action = request.query['action']
			if (!action) {
				action = 'read'
			}
			response = createResponse(outputStream)

			if (request.user.authorised(store, action)) switch (action) {
				case 'save':    // saves html and actor
					save(request.body())
					def after = request.query['after'] ?: ''
					staticResponse "usdlc.highlight('sky');$after"
					break
				case 'raw':    // actors sent rather than run (editing)
					staticResponse store.read()
					break
				default:
					checkForStaticGzip()
					Actor actor = Actor.load(store)
					if (actor) {
						dynamicResponse { actor.run([exchange: this]) }
					} else {
						staticResponse store.read()
					}
					break
			} else if (action == 'save') {
				staticResponse "usdlc.highlight('red');"
			} else {
				staticResponse '~locked~'
			}
		} catch (problem) {
			problem.printStackTrace()
		}
		response.complete()
	}

	Closure sendResponseHeader

	private staticResponse(String string) { staticResponse string.bytes }

	private staticResponse(byte[] bytes) {
		response.header['Content-Length'] = bytes.length as String
		sendResponseHeader()
		response.write bytes
	}

	private dynamicResponse(Closure action) {
		response.header['Connection'] = 'close'
		sendResponseHeader()
		action()
	}

	private Response createResponse(OutputStream outputStream) {
		Response response
		response = new Response()

		if (request.session.isNewSession) {
			response.sessionCookie = request.session.key
		}
		response.out = new PrintStream(outputStream, true)
		response.header['Content-Type'] =
			request.query.mimeType ?: mimeType(store.pathFromWebBase)
		response
	}
	/**
	 * Check to see if there is a gzip on disk before running actor or sending
	 * static file. Dates are checked so that if source is newer than zip,
	 * the source will be used. For scripts it will need to update the gzip
	 * if it is hoped to use next time.
	 */
	private checkForStaticGzip() {
		if (request.header.acceptEncoding.indexOf('gzip') != -1) {
			def gzip = Store.base("${store.pathFromWebBase}.gzip")
			if (gzip.exists() && !store.newer(gzip)) {
				response.header['Content-Encoding'] = 'gzip'
				store = gzip
			}
		}
	}

	Store store
	boolean inferredTarget

	void setStore(String path) {
		String urlBase = config.urlBase
		if (path.startsWith(urlBase)) {
			path = path[urlBase.size()..-1]
		}
		store = Page.store(path)
	}

	void save(String newContents) {
		store.write newContents.bytes
	}

	// Point Apache Commons logging to a uSDLC proxy.
	static { apacheCommons() }
}
