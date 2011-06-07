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

/**
 * The Timer class is used to calculate, record and display elapsed time.
 */
class Timer {
	/**
	 * Constructor takes a title for this timer that will be recorded to the log
	 * @param title Anything that can be coerced to a string
	 */
	Timer(title = '') { this.title = title }
	/**
	 * Log the elapsed time since this timer was instantiated in csv for, being title and time in ms.
	 * @param path Path to log file to which the data is appended.
	 * @return Elapsed time in ms.
	 */
	def log(path) {
		def elapsed = System.currentTimeMillis() - start
		Store.base(path).append("$title,$elapsed")
		return elapsed
	}
	/**
	 * Return the elapsed time in a human readable form.
	 * @return "1 h 23 m 44 s" or "347 ms"
	 */
	String toString() {
		long elapsed = System.currentTimeMillis() - start
		def string = new StringBuffer()
		if (elapsed < 1000) {
			string.append("$elapsed ms")
		} else {
			elapsed /= 1000
			def seconds = elapsed % 60
			string.append("$seconds s")
			elapsed /= 60
			def minutes = elapsed % 60
			def hours = elapsed / 60
			if (hours || minutes) { string.insert(0, "$minutes m ") }
			if (hours) { string.insert(0, "$hours h ") }
		}
		return string
	}

	private start = System.currentTimeMillis(), title
}
