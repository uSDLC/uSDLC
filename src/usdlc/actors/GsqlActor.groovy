package usdlc.actors

import usdlc.BrowserBuilder
import usdlc.db.Gsql

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

/**
 * User: Paul Marrington
 * Date: 5/05/11
 * Time: 9:16 PM
 *
 * jdbc:h2:/data/test;AUTO_SERVER=TRUE
 * jdbc:h2:mem:test:db1;DB_CLOSE_DELAY=-1
 */
class GsqlActor extends GroovyActor {
	def bind() {
		binding.doc = BrowserBuilder.newInstance('text/text')
		binding.ensure.gsql = { new Gsql() }
		delegate = binding.gsql
		return this
	}
}
