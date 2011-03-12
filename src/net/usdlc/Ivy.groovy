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

import groovy.xml.NamespaceBuilder

/**
 * User: Paul Marrington
 * Date: 11/03/11
 * Time: 7:44 PM
 */
class Ivy {
	static retriever(doc) {
		def instance = new Ivy()
		instance.doc = doc
		instance.ant = Ant.builder(doc)
		instance.ivy = NamespaceBuilder.newInstance(instance.ant, 'antlib:org.apache.ivy.ant')
		instance.ant.reset()
		return instance
	}

	private Ivy() {}

	def load(group = 'jars/optional', resolveArgumentString, remove) {
		def resolveArguments = Dictionary.fromString(resolveArgumentString, ':', ' ')
		ivy.resolve(resolveArguments + [inline : true, showprogress : false, keep : true])
		ivy.retrieve(pattern : "lib/$group/[artifact].[ext]")
		if (remove) {
			remove.split().each {
				ant.delete(file : "lib/$group/$it",verbose:true)
			}
		}
		return this
	}

	def ant, ivy, doc
}
