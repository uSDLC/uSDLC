page = new usdlc.Page(usdlc.Store.tmp('PageUpdating.html'))
assert page.title.endsWith('Page Updating')		      
assert page.subtitle == ''
assert page.synopsis == ''