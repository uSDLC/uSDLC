/*
 Copyright 2011 the Authors for http://usdlc.net

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package usdlc.server

import java.io.InputStream
import usdlc.Dictionary
import io.Source

/**
 * User: paul
 * Date: 30/06/11
 * Time: 6:53 PM
 */

case class Request(
	  inputStream: InputStream,
	  header: Header
	  ) {
	val query = Dictionary.query(header.query)
	val cookies = Dictionary.cookies(header.cookie)
	val userId = if (cookies.containsKey("userId")) cookies.get("userId") else "anon"
	val session = if (cookies.containsKey("session")) cookies.get("session") else Session.newKey

	def body = Source.fromInputStream(inputStream).getLines().mkString("\n")
}

object Session {
	def newKey = {
		val before = last
		last = System.currentTimeMillis()
		if (last == before) {
			last += 1
		}
		last.toString
	}

	var last = 0l
}