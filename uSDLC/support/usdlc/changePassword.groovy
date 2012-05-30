query =  exchange.request.query
user = exchange.request.user
ok = user.changePassword(query.was, query.to)
write ok ? 'ok' : 'error'
