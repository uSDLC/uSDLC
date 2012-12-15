package usdlc

device = exchange.request.query.name
result = exchange.request.query.result
remote = RemoteComms.get(device)

if (result) {
    println "Test returned $result"
    remote.toUSDLC.put(result)
} else {
    println "Connected to ${device}, waiting for code..."
    code = remote.fromUSDLC.take()
    println "Injecting code into ${device}"
    write code
}

