query =  exchange.request.query
exchange.request.user.login(query.name, query.password)
write exchange.request.user.toHtml()
