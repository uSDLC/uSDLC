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

/*
 * User: Paul Marrington
 * Date: 13/03/11
 * Time: 1:24 PM
 */

/**
 * The age-old problem if holding on to data that is to be kept for a conversation or similar temporal event.
 */
class Environment {
	def my = data()
	/**
	 * Get a named property
	 * @param name
	 * @return
	 */
	def propertyMissing(name) { return my.containsKey(name) ? my[name] : null }
	/**
	 * This base solution assumes that immediate data can be retrieved from the thread name. Typically instantiated in any method that needs access to environmental data:
	 *
	 * def env = Environment.data()
	 * env.header = requestHeader
	 */
	static Map data() {
		def key = getKey()
		if (!dataMap[key]) { dataMap[key] = [:] }
		return dataMap[key] as Map
	}
	/**
	 * Over-ride this method if your environment can't use the thread name to retrieve data during a single conversation exchange.
	 * @return String unique to the current exchange/thread.
	 */
	static getKey() {
		return Thread.currentThread().name
	}
	/**
	 * Retrieve the host that made the request that created this environment.
	 * @return Host (authority) string - as in http://usdlc.net
	 */
	static String getHost() {
		return "http://${data().header.host[0]}"
	}
	/**
	 * Hold the environment data keys to the thread/current exchange.
	 */
	static dataMap = [:]
	/**
	 * So we can say Environment.blah and get an environment variable called blah directly.
	 */
	static {
		Environment.metaClass.static.propertyMissing = { name -> return data()."$name" }
	}
}
