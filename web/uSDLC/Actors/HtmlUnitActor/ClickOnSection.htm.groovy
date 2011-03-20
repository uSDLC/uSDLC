doc.div(class: 'text') {
	// Load index.html from a page in scratch. Does not matter if it does not exist.
	def client = new net.usdlc.WebClient("http://${header.host[0]}/Sandbox/scratch")
	// Make sure page decorations have loaded by looking for the uSDLC image
	assert client["img#pageTitle"].size() == 1
	// Fetch the list uf uSDLC sections.
	def sections = client["div.section"]
	// Even an empty file will have one from the template
	assert sections.size() > 0
	// When you load a page there should not be a section in focus
	assert !sections[0].hasClass('inFocus')
	// Click on a section will trigger JQuery to place it in focus
	sections[0].click()
	// Confirm that the section has focus
	assert sections[0].hasClass('inFocus')
}
