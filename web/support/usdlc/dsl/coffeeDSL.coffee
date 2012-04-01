globals = (-> (-> this)())()
usdlc = Packages.usdlc
groovy = Packages.groovy

print = (text) -> exchange.response.print "#{text}\n"
output = (text) -> exchange.response.print text
$session = exchange.getRequest().session
session = instance : (cls) -> $session.get('instance').call cls
include = run = dsl = (script) -> support.include script

assert = (test, msg) -> throw msg if not test

startTime = java.lang.System.currentTimeMillis()
timer = ->
	Math.floor((java.lang.System.currentTimeMillis() - startTime) / 1000)
sleep = (seconds) -> support.$sleep(seconds)

jsArray = (arrayList) ->
	arrayList.get(index) for index in [0...arrayList.size()]
