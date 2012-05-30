query =  exchange.request.query
ok = exchange.request.user.login(query.name, query.password)
write ok ? 'ok' : 'error'
