device = exchange.request.query.get("name")
remote = usdlc.RemoteComms.get(device)

java.lang.System.out.println "Connected to #{device}, waiting for code..."
code = remote.fromUSDLC.take()
java.lang.System.out.println "Injecting code into #{device}"
write code
