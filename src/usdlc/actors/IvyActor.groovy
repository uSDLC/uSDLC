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
package usdlc.actors

import groovy.xml.NamespaceBuilder
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import usdlc.*

/**
 * User: Paul Marrington
 * Date: 27/03/11
 * Time: 4:22 PM
 */
class IvyActor extends GroovyActor {
	def bind() {
		binding.doc = BrowserBuilder.newInstance('text/text')
		delegate = new Ivy(Log.file('ivy'))
		return bind([ivy: delegate])
	}
}

class Ivy {
	def env = Environment.session()

	Ivy(log) {
		this.log = log
		ant = Ant.builder(log)
		ivy = NamespaceBuilder.newInstance(ant, 'antlib:org.apache.ivy.ant')
		ant.reset()
	}

	def methodMissing(String name, value) {
		if (value.size() > 1) {
			def before = args[name]
			args[name] = value[0]
			value[1]()
			args[name] = before

		} else {
			args[name] = value[0]
		}
		return this
	}

	def group(group) {
		this.group = group
		return this
	}

	def group(group, Closure closure) {
		def before = this.group
		this.group = group
		closure()
		this.group = before
		return this
	}

	def resolve(resolveArgumentString) {
		args += Dictionary.fromString(resolveArgumentString, ':', ' ')
	}

	def remove(remove) {
		remove?.split()?.each {
			ant.delete(file: "lib/$group/$it", verbose: true)
		}
		return this
	}

	def fetch(Map more) {
		ivy.resolve(args + [inline: true, showprogress: false, keep: true])
		ivy.retrieve(pattern: "lib/$group/[artifact].[ext]")
		return this
	}

	def required(configuration = 'default') {
		return conf(configuration).group('jars/required').fetch().source()
	}

	def optional(configuration = 'default') {
		return conf(configuration).group('jars/optional').fetch().source()
	}

	def source(boolean fetchSource) {
		env.fetchSource = fetchSource
		return this
	}

	def source() {
		if (env?.fetchSource) {
			conf('sources').group('source').fetch().conf('javadoc').group('javadoc').fetch()
		}
		return this
	}

	def source(String moduleName) { module(moduleName).source().fetch() }

	def download(String url, String to = "") {
		def entity = new DefaultHttpClient().execute(new HttpGet(url)).entity
		if (entity) {
			log "Download $url, $entity.contentLength bytes\n"
			def uri = Store.split(to ?: url)
			ant.mkdir(dir: "lib/$group")
			def out = new FileOutputStream("lib/$group/${uri.name}${uri.ext}")
			entity.writeTo(out)
			out.close()
		}
	}

	def ant, ivy, log, group, args = [:]
}
