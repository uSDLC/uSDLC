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
package usdlc.reports

import usdlc.Page
import usdlc.db.Database

/**
 * User: Paul Marrington
 * Date: 8/05/11
 * Time: 9:48 AM
 */
class Tasks {
	static version = Database.version('classpath:/usdlc/reports/Tasks')

	def processPage(contents) {
		def page = new Page(contents)
		page.sections { section ->
		}
		page.updated ? page.toString() : contents
	}
}
