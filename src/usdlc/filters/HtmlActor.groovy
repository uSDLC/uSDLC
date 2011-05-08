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
package usdlc.filters

import usdlc.Config
import usdlc.Environment
import usdlc.Filer
import usdlc.Store

/**
 * uSDLC supports actors and filters. This filter provides HTML templating. By default it runs templates/template.html.groovy that provides header, body and scrolling elements.
 *
 * User: Paul Marrington
 * Date: 23/11/10
 * Time: 6:22 PM
 */
class HtmlActor {
	def env = Environment.data()
	/**
	 Use to generate HTML to display on the screen.
	 */
	public run(script) {
		def file = new Filer(script)

		switch (env.query.action) {
			case "run":
				cgi "rt", 'run.html.groovy'
				break
			case 'history':
				def end = (env.query._index_ ?: '-1').toInteger()
				env.out.println file.history.restore(end)
				break
			case 'cut':
			case 'copy':
				def base = file.store.parent
				def targetPath = Store.base('clipboard').uniquePath(env.query.title).replace('\\', '/')
				env.query.dependents.tokenize(',').each {
					//noinspection GroovyNestedSwitch
					switch (env.query.action) {
						case 'copy': Store.base("$base/$it").copy(targetPath); break
						case 'cut': Store.base("$base/$it").move(targetPath); break
					}
				}
				def contents = env.in.text.bytes
				Store.base("$targetPath/Section.html").write(contents)
				env.doc.js("usdlc." + env.query.action + "SectionSuccessful('$env.query.title','/$targetPath')")
				break
			case 'paste':
				def target = file.store.parent
				def from = Store.base(env.query.from)
				from.dir(~/[^.]*/) { Store.base(it).move(target) }
				env.out.println new String(Store.base("$env.query.from/Section.html").read())
				from.rmdir()
				break
			default:    // Suck the HTML file contents - converting from byte[] to String.
				env.out.println new String(file.contents)
				break
		}
	}
	/**
	 * Helper method used by language specific sub-classes. HTML, for example, uses this method to fire off the template script after passing in the HTML as BODY in the environment.
	 *
	 * @param dir - Relative directory to the script - usually inferred from url path.
	 * @param cmd - Name of http command (end of path). Script to run is created from this.
	 * @param nvpList a list of strings in the form 'name=value' to be added to the environment map.
	 */
	protected cgi(dir, template) {
		def script = template
		def file = new Filer(script) // interim settings
		if (!file.clientExt) {
			/*
			 No client extension means a HTTP url without a stop - so we can't guess either client format or server function. If the command ends in /, use template.html - otherwise assume it is int he default script language set in WEB-INF/web.groovy.
			 */
			if (script[-1] == '/') {
				script += 'template.html'
			}
			script += '.' + Config.defaultScriptLanguage
		}
		/*
		 Filer does the real work if executing the CGI - since it knows all the details.
		 */
		new Filer(env.script = "$dir/$script").actorRunner()
	}
}
