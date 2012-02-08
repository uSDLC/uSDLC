page = new usdlc.Page('/uSDLC/index.gsp')
assert page.title == 'Unifying the SDLC'
assert page.subtitle.size() > 0
assert page.synopsis.size() > 0
