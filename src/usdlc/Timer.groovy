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
package usdlc

/**
 * User: Paul Marrington
 * Date: 8/05/11
 * Time: 3:25 PM
 */
// todo: add Timer to tools page
class Timer {
	String toString() {
		long elapsed = System.currentTimeMillis() - start
		if (elapsed < 1000) { return "$elapsed ms" }
		def string = new StringBuffer()
		elapsed /= 1000
		def seconds = elapsed % 60
		string.append("$seconds s")
		elapsed /= 60
		def minutes = elapsed % 60
		def hours = elapsed / 60
		if (hours || minutes) { string.insert(0, "$minutes m ") }
		if (hours) { string.insert(0, "$hours h ") }
		return string
	}

	private start = System.currentTimeMillis()
}
