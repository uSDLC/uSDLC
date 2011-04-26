import usdlc.Environment

class Sandbox {
	/**
	 * This delegating to the environment means that these classes can act like the calling script - data in the binding can be accessed directly (as in client)
	 */
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
