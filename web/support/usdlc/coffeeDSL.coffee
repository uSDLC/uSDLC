globals = (-> (-> this)())()
contexts = {}
usdlc = Packages.usdlc

dsc = (cmd, context, next, help) ->
	contexts[cmd] = {before : context, next : next, help : help}
	globals[cmd] = (params...) -> delegate.dsc cmd, params
	
print = (text) -> exchange.response.print text
