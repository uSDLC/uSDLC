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

import groovy.xml.NamespaceBuilder
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient


class Ivy {
	Ivy(consoleWriter) {
		def logFileWriter = Log.file('ivy')
		log = { consoleWriter it; logFileWriter it }
		ant = Ant.builder(log)
		ivy = NamespaceBuilder.newInstance(ant, 'antlib:org.apache.ivy.ant')
		ant.reset()
	}

	Ivy group(String group) {
		this.group = group
		this
	}

	Map resolve(String resolveArgumentString) {
		args += Dictionary.fromString(resolveArgumentString, ':', ' ')
	}

	Ivy remove(String toRemove) {
		toRemove?.split()?.each {
			ant.delete(file: "lib/$group/$it", verbose: true)
		}
		this
	}

	Ivy fetch() {
		ivy.resolve(args + [inline: true, showprogress: false, keep: true])
		ivy.retrieve(pattern: "web/lib/$group/[artifact].[ext]")
		this
	}

	Ivy conf(String configuration) { args['conf'] = configuration; this }

	Ivy source() {
		if (fetchSource) {
			conf('sources').group('source').fetch().conf('javadoc').group('javadoc').fetch()
		}
		this
	}

	def download(String url, String to = '') {
		def entity = new DefaultHttpClient().execute(new HttpGet(url)).entity
		if (entity) {
			log "Download $url, $entity.contentLength bytes\n"
			Map uri = Store.split(to ?: url)
			mkdir()
			def out = new FileOutputStream("web/lib/$group/${uri['name']}${uri['ext']}")
			entity.writeTo(out)
			out.close()
		}
		this
	}

	private mkdir() { ant.mkdir(dir: "web/lib/$group") }

	def ivy
	Closure log
	String group
	Ant ant
	Map args = [:]
	boolean fetchSource
}