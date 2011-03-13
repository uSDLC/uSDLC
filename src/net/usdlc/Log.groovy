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

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.impl.SimpleLog

/**
 * Many of the packages use the Apache commons logging wrapper. This class is used to point the results to
 * the response to the browser. Use for the offending classes like:
 *
 * 	static {* 		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "net.usdlc.Log");
 *}*
 * User: Paul Marrington
 * Date: 13/03/11
 * Time: 12:17 PM
 */
class Log extends SimpleLog {
	Log(String name) { super(name) }

	def my = Environment.data()
	/**
	 * By default, SimpleLog sends to stderr. Let's redirect it to somewhere more useful.
	 * @param buffer buffer the logger has prepared for the record
	 */
	protected void write(StringBuffer buffer) {
		my.out.println buffer
	}
	/**
	 * The most likely (and in fact only) use for this class is the interception Apache Commons logging -
	 * so we had better tell the factory.
	 * @return
	 */
	static apacheCommons() {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "net.usdlc.Log");
	}
}
