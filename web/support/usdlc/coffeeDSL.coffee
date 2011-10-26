globals = (-> (-> this)())()
usdlc = Packages.usdlc
print = (text) -> exchange.response.print text
$session = exchange.getRequest().session
session = 
	instance : (cls) -> $session.get('instance').call cls

assert = (test, msg) -> throw msg if not test

startTime = java.lang.System.currentTimeMillis()
timer = -> 
	Math.floor((java.lang.System.currentTimeMillis() - startTime) / 1000)
sleep = (seconds) -> support.$sleep(seconds)
