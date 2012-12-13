device = exchange.request.query.name
remote = usdlc.RemoteComms.get(device)

code = remote.fromUSDLC.take()
write code
