import usdlc.Store

query =  exchange.request.query
page = Store.base(query.page)
page.rebase("Given_$query.scriptName").file.text =
	query.given.replaceAll(~/,\s*/, '\n')
page.rebase("When_$query.scriptName").file.text =
	query.when.replaceAll(~/,\s*/, '\n')
page.rebase("Then_$query.scriptName").file.text =
	query.then.replaceAll(~/,\s*/, '\n')
write 'ok'
