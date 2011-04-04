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
package net.usdlc

/*
 * User: Paul Marrington
 * Date: 13/03/11
 * Time: 1:24 PM
 */

/**
 * The age-old problem if holding on to data that is to be kept for a conversation or similar temporal event.
 */
class Environment {
	/**
	 * This base solution assumes that immediate data can be retrieved from the thread name. Typically instantiated in any method that needs access to environmental data:
	 *
	 * def my = Environment.data()
	 * my.header = requestHeader
	 */
	static data() {
		def key = getKey()
		if (!dataMap[key]) { dataMap[key] = [:] }
		if (!dataMap.containsKey(key)) {
			throw new Error("what the ???")
		}
		return dataMap[key]
	}
	/**
	 * Over-ride this method if your environment can't use the thread name to retrieve data during a single conversation exchange.
	 * @return String unique to the current exchange/thread.
	 */
	static getKey() {
		return Thread.currentThread().name
	}
	/**
	 * Hold the environment data keys to the thread/current exchange.
	 */
	static dataMap = [:]
}
