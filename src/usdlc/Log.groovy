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
package usdlc

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.impl.SimpleLog

/**
 * Many of the packages use the Apache commons logging wrapper. This class is used to point the results to
 * the response to the browser. Use for the offending classes like:
 *
 * 	static {* 		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "usdlc.Log");
 *}*
 * User: Paul Marrington
 * Date: 13/03/11
 * Time: 12:17 PM
 */
class Log extends SimpleLog {
	Log(String name) { super(name) }
	/**
	 * By default, SimpleLog sends to stderr. Let's redirect it to somewhere more useful.
	 * @param buffer buffer the logger has prepared for the record
	 */
	protected void write(StringBuffer buffer) { System.out.println buffer }
	/**
	 * Given a file path to save the log to, return a closure that will write whatever it is given
	 * @param name Name/path of file to append log information to.
	 * @return closure to call to write to the log.
	 */
	static file(name) {
		def store = Store.base("store/log/${name}.log")
		store.append("\n${new Date().format('yyyy-MM-dd HH:mm')}: ")
		return { store.append it }
	}
	static csv(name) {
		def store = Store.base("store/log/${name}.csv")
		store.append("\n${new Date().format('yyyyMMdd,HHmm,Z')},")
		return { store.append it }
	}
	/**
	 * Shortcut convenience method to write to stdout (server output stream).
	 * @param message Message to write
	 */
	static inf(message) { System.out.println message; message }
	/**
	 * Shortcut convenience method to write to stderr (server output stream).
	 * @param message Message to write
	 */
	static err(message) { System.err.println message; message }
	/**
	 * The most likely (and in fact only) use for this class is the interception Apache Commons logging -
	 * so we had better tell the factory.
	 * @return
	 */
	static void apacheCommons() {
		LogFactory.factory.setAttribute('org.apache.commons.logging.Log', 'usdlc.Log');
		// "trace", "debug", "info", "warn", "error", or "fatal"
		System.properties['org.apache.commons.logging.simplelog.defaultlog'] = 'error';
	}
}
