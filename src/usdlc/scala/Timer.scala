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
package usdlc.scala

/**
 * Create a timer for measuring how long an action takes and record it to a log. 
 * It can be created with a string title/comment.
 */
case class Timer(ms: Long) {
	override def toString = {
		val text = new StringBuffer()
		if (ms < 1000) {
			text.append(ms).append(" ms")
		} else {
			var elapsed = ms / 1000
			val seconds = elapsed % 60
			elapsed /= 60
			val minutes = elapsed % 60
			val hours = elapsed / 60
			if (hours > 0) text.append(hours).append(" h ")
			if (hours > 0 || minutes > 0) text.append(minutes).append(" m ")
			if (seconds > 0) text.append(seconds).append(" s")
			text
		}
		text.toString
	}
}

object Timer {
	def measure(callback: () => Unit) = {
		val start = System.currentTimeMillis()
		callback()
		Timer(System.currentTimeMillis() - start)
	}
}