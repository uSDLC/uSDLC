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
package net.usdlc
/**
 * Dictionaries can be created from strings of name/value pairs - such as cookies, headers or the query string in a URL address.
 * User: Paul Marrington
 * Date: 9/01/11
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
	 * @param text String containing "name=value;" pairs
	 * @return dictionary ready to use
	 */
	static cookies(text) {
		return fromString(text, '=', ';')
	}
	/**
	 * Use this to create a cookie dictionary
	 *
	 * def query = Dictionary.query(request.queryString)
	 * def action = query.action
	 * @param text String containing "name=value&" pairs
	 * @return dictionary ready to use
	 */
	static query(text) {
		return fromString(text, '=', '&')
	}
	/**
	 * A map can be added to from a string in the define format (separators and assigment operators)
	 * @param text Text to parse for map
	 * @return map
	 */
	static fromString(text, assign, separate) {
		def map = [:]
		if (text) {
			text.split(/\s*$separate\s*/).each {
				def nvp = it.split(/\s*$assign\s*/)
				map[nvp[0]] = (nvp.size() < 2) ? "" : nvp[1]
			}
		}
		return map
	}
	/**
	 * Since we may have changed entries to have binary values, behave accordingly.
	 * @return String representation of dictionary.
	 */
	static toString(map, assign, separate) {
		StringBuilder builder;
		map.each { key, value ->
			builder.append(key).append(assign).append(value).append(separate)
		}
		builder.length = builder.size() - separate.size()
		return builder.toString()
	}
}
