import usdlc.Store

def parse(statements) {
	statements.replaceAll(~/,\s*/, '\n').trim()
}

query =  exchange.request.query
page = Store.base(query.page)

page.rebase("Given_$query.scriptName").file.text = parse(query.given)
page.rebase("When_$query.scriptName").file.text = parse(query.when)
page.rebase("Then_$query.scriptName").file.text = parse(query.then)

write 'ok'
