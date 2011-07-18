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
package usdlc.actor

import groovy.xml.NamespaceBuilder
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import usdlc.Ant
import usdlc.Dictionary
import usdlc.Log
import usdlc.Store

/**
 * User: Paul Marrington
 * Date: 27/03/11
 * Time: 4:22 PM
 */
//class IvyActor extends GroovyActor {
//	def bind() {
//		context.doc = BrowserBuilder.newInstance('text/text')
//		delegate = new Ivy(Log.file('ivy'))
//		return bind([ivy: delegate])
//	}
//}

class IvyActor extends GroovyActor {
	def dsl = [
			organisation: { args.organisation = it },
			module: { args.module = it },
			group: { group = it },
			required: { conf(it ?: 'default').group('jars/required').fetch().source() },
			optional: { conf(it ?: 'default').group('jars/optional').fetch().source() },
	]

	IvyActor() {
		log = Log.file('ivy')
		ant = Ant.builder(log)
		ivy = NamespaceBuilder.newInstance(ant, 'antlib:org.apache.ivy.ant')
		ant.reset()
	}

//	IvyActor methodMissing(String name, Object[] value) {
	//		if (value.size() > 1) {
	//			def before = args[name]
	//			args[name] = value[0]
	//			((Closure) value[1])()
	//			args[name] = before
	//
	//		} else {
	//			args[name] = value[0]
	//		}
	//		this
	//	}

	IvyActor group(String group, Closure closure) {
		def before = this.group
		this.group = group
		closure()
		this.group = before
		this
	}

	Map resolve(String resolveArgumentString) {
		args += Dictionary.fromString(resolveArgumentString, ':', ' ')
	}

	IvyActor remove(String toRemove) {
		toRemove?.split()?.each {
			ant.delete(file: "lib/$group/$it", verbose: true)
		}
		this
	}

	IvyActor fetch() {
		ivy.resolve(args + [inline: true, showprogress: false, keep: true])
		ivy.retrieve(pattern: "web/lib/$group/[artifact].[ext]")
		this
	}

	IvyActor conf(String configuration) { args['conf'] = configuration }

	IvyActor source(boolean fetchSource) {
		context['fetchSource'] = fetchSource
		this
	}

	IvyActor source() {
		if (context['fetchSource']) {
			conf('sources').group('source').fetch().conf('javadoc').group('javadoc').fetch()
		}
		this
	}

	IvyActor source(String moduleName) {
		args['module'] = moduleName
		source().fetch()
	}

	void download(String url, String to = '') {
		def entity = new DefaultHttpClient().execute(new HttpGet(url)).entity
		if (entity) {
			log "Download $url, $entity.contentLength bytes\n"
			Map uri = Store.split(to ?: url)
			mkdir()
			def out = new FileOutputStream("web/lib/$group/${uri['name']}${uri['ext']}")
			entity.writeTo(out)
			out.close()
		}
	}

	private mkdir() { ant.mkdir(dir: "web/lib/$group") }

	def ivy
	Closure log
	String group
	Ant ant
	Map args = [:]
}
