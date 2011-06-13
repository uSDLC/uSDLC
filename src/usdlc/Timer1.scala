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
 * Create a timer for measuring how long an action takes and record it to a log. It can be created with a string
 * title/comment.
 */
class Timer1(title: String = "", comment: String = "") {
	val start = System.currentTimeMillis()
	/**
	 * Log the elapsed time since this timer was instantiated in csv for, being title and time in ms.
	 * @param path Path to log file to which the data is appended.
	 * @return Elapsed time in ms.
	 */
	def log(path: String) = {
		val elapsed = System.currentTimeMillis() - start
		Store.base(path).append(title) //.append(",").append(elapsed).append(",").append(comment)
		elapsed
	}

	/**
	 * Return the elapsed time in a human readable form.
	 * @return "1 h 23 m 44 s" or "347 ms"
	 */
	override def toString = {
		var elapsed = System.currentTimeMillis() - start

		val string = new StringBuffer()
		if (elapsed < 1000) {
			string.append(elapsed).append(" ms")
		} else {
			elapsed /= 1000
			val seconds = elapsed % 60
			elapsed /= 60
			val minutes = elapsed % 60
			val hours = elapsed / 60
			if (hours > 0) string.append(hours).append(" h ")
			if (hours > 0 || minutes > 0) string.append(minutes).append(" m ")
			if (seconds > 0) string.append(seconds).append(" s")
			string
		}
		string.toString
	}
}