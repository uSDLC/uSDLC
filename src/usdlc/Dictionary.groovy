package usdlc
/**
 * Dictionaries can be created from strings of name/value pairs - such as cookies, headers or the query string in a URL address.
 */
class Dictionary {
	def assign = '='
	def separator = ';'
	def storage = [:]
	/**
	 * Use this to create a cookie dictionary
	 *
	 * def cookies = Dictionary.cookies(request.cookies)
	 * def userId = cookies.userId
	 *
	 * @param text String containing "name=value;" pairs
	 * @return dictionary ready to use
	 */
	static Map cookies(String text) {
		fromString(text, '=', ';')
	}
	/**
	 * Use this to create a cookie dictionary
	 *
	 * def query = Dictionary.query(request.queryString)
	 * def action = query.action
	 *
	 * @param text String containing "name=value&" pairs
	 */
	static query(String text) {
		fromString(text, '=', '&') { URLDecoder.decode(it, "UTF-8") }
	}
	/**
	 * Use this to create a dictionary from command line arguments
	 *
	 * def arguments = Dictionary.commandLine(args)
	 * context.variables += Dictionary.commandLine(args)
	 *
	 * @param args from command line in the form of a=b "c=d e" f=g
	 * @return dictionary ready to use
	 */
	static commandLine(args) {
		def map = [:]
		args.each {
			def nvp = it.split(/\s*=\s*/)
			map[nvp[0]] = (nvp.size() < 2) ? '' : nvp[1]
		}
		map
	}
	/**
	 * A map can be added to from a string in the define format (separators and assignment operators)
	 * @param text Text to parse for map
	 * @return map
	 */
	static fromString(String text, String assign, String separate,
			decoder = {it}) {
		def map = [:]
		if (text) {
			text.split(/\s*$separate\s*/).each {
				def nvp = it.split(/\s*$assign\s*/)
				map[decoder(nvp[0])] = (nvp.size() < 2) ? '' : decoder(nvp[1])
			}
		}
		map
	}
	/**
	 * Since we may have changed entries to have binary values, behave accordingly.
	 * @return String representation of dictionary.
	 */
	static String toString(String map, String assign, String separate) {
		StringBuilder builder = new StringBuilder()
		map.each { key, value ->
			builder.append(key).append(assign).append(value).append(separate)
		}
		builder.length = builder.size() - separate.size()
		builder.toString()
	}
}
