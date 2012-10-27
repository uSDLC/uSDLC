query =  exchange.request.query
if (exchange.request.user.login(query.name, query.password))
	write exchange.request.user.toHtml()
