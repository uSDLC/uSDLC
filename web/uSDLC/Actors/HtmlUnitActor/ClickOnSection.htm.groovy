//doc.div(class: 'message') {
    // Load index.html from a page in scratch. Does not matter if it does not exist.
//    def page = WebTest.load("http://${env.host[0]}/scratch")
def page = net.usdlc.WebClient.load("http://google.com")
    // Make sure page decorations have loaded by looking for the uSDLC image
    assert page.xpath("//img#pageTitle").size() == 1
    // Fetch the list uf uSDLC sections.
    def sections = page.xpath("//div.section")
    // Even an empty file will have one from the template
    assert sections.size() > 0
    // When you load a page there should not be a section in focus
    assert !sections[0].hasClass('inFocus')
    // Click on a section will trigger JQuery to place it in focus
    sections[0].click()
    // Confirm that the section has focus
    assert !sections[0].hasClass('inFocus')
//}
