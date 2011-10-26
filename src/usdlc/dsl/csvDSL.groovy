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
package usdlc.dsl

def newCsv(String name, Map params = [:]) {
	new usdlc.CSV(store: exchange.store.rebase("${name}.csv"), context: params)
}
def csvLoad(String name, Map params = [:]) {
	newCsv(name, params).load()
}
def csvLoad(String name, Map params, Closure perRow) {
	def csv = newCsv(name, params)
	csv.perRow = perRow
	csv.load()
}
csv = this.&csvLoad
