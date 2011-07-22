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

def now = new Date().time
usdlc.Store.base('store/clipboard').dir().sort().reverse().each { clip ->
	def item = usdlc.Store.parseUnique(clip)
	if (item) {
		long age = now - item.date.time
		int shade = 0x0000FF - (age / 60000)   // blue to black over 4 hours
		def colour = Integer.toHexString((shade < 0) ? 0 : shade).padLeft(6, '0')

		out "<a href='$item.path' style='color:#$colour'>$item.title</a>"
	}
}
