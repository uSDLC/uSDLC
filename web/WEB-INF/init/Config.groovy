package init
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
/**
 * User: paul
 * Date: 29/06/11
 * Time: 6:36 PM
 */
class Config extends usdlc.Config {
	public static Config config

	/**
	 * This is how we load the configuration
	 */
	static load(baseDirectory, args) { config = new Config(); config.init(baseDirectory, args) }

	String urlBase = ""
	String rootFile = "/rt/template.html.groovy"
	String usdlcDatabase = "jdbc:h2:.db/usdlc"
	/*
	 Path to use for defining Java and Groovy files. Point to the source for static (unchanging) code files and the web directory for files that will change as part of the installation.
	 */
	String[] srcPath = ["", "Actors"]
	String[] libPath = ["lib/jars"]
	/*
	 If the file does not exist, use a template file for that usdlc.server.servletengine.server and client side extension.
	 */
	Map<String, String> template = [
			html: "template",
			'html.groovy': "template",
			groovy: "template",
			gradle: "template"
	]
	/*
	 Define the script language to use for a address line url that specifies the path only.
	 */
	String defaultScriptLanguage = "groovy"
	boolean alwaysCheckForRecompile = true
	/*
	 * What local port is used for the local usdlc.server.standalone?
	 */
	int port = 9000;
	/*
	 * Environment registrations
	 */
	Map<String, String> environmentRegister = [
			db: "usdlc.db.Database"
	]
}
