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
import net.usdlc.Environment

class Sandbox {
	@Delegate Environment environment = new Environment()
	/**
	 * Open a page within the sandbox
	 * @param name Page name
	 * @return Reference to sandbox for more work
	 */
	static page(name = 'scratch') {
		def sandbox = new Sandbox()
		sandbox.client.load("http://${sandbox.header.host[0]}/Sandbox/$name")
		return sandbox
	}
	/**
	 * Create a new section, give it focus and open the HTML editor.
	 */
	public newSection() {
		def sections = client["div.section"]
		sections[0].click()
		client.executeJavaScript("usdlc.insertSectionBelowFocus()")
	}
}
