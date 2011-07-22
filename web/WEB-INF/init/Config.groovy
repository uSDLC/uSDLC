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
package init
/**
 * User: paul
 * Date: 29/06/11
 * Time: 6:36 PM
 */
class Config extends usdlc.Config {
	public static final Config config = new Config()

	Config() {
		urlBase = ''
		srcPath = ['web', 'web/dsl', 'jar:file:usdlc.jar!/usdlc/dsl']
		libPath = ['lib/jars']
		template = [
				html: 'template',
				'html.groovy': 'template',
				groovy: 'template',
				gradle: 'template'
		]
		databases = [
				usdlc: 'jdbc:h2:.db/usdlc'
		]
		browserDriverList = 'firefox:chrome:ie:htmlunit'
		/*
			 Define the script language to use for a address line url that specifies the path only.
			 */
		defaultScriptLanguage = 'groovy'
		alwaysCheckForRecompile = true
		/*
			 * What local port is used for the local usdlc.server.standalone?
			 */
		port = 9000
		/*
			 * Environment registrations
			 */
		environmentRegister = [
				db: 'usdlc.db.Database'
		]
	}
}
