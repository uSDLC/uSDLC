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

/*
 For any but the stand-alone server there will be a URL on the server that is redirected to uSDLC. We need to remove this common base path to work out where in the structure we are. This regex should be set up so that the first group is the one we want to use.One for the address in the browser location bar and one to find the same path on disk (or in database)
 */
urlBase = ""
webBase = "web"
rootFile = "/rt/template.html.groovy"

/*
 Path to use for defining Java and Groovy files. Point to the source for static (unchanging) code files and the web directory for files that will change as part of the installation.
 */
//noinspection GroovyUnusedAssignment
srcPath = ['web/Actors', 'web', 'src']
libPath = ['web/lib/jars', 'lib/jars', '.']
/*
 If the file does not exist, use a template file for that server and client side extension.
 */
template = [
		html: 'template',
		'html.groovy': 'template',
		groovy: 'template',
		gradle: 'template'
]
/*
 Define the script language to use for a address line url that specifies the path only.
 */
defaultScriptLanguage = 'groovy'
allwaysCheckForRecompile = true
/*
 * What local port is used for the local server?
 */
port = 9000
