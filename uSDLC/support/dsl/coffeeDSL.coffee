globals = (-> (-> this)())()
usdlc = Packages.usdlc
groovy = Packages.groovy

print = (text) -> exchange.response.print "#{text}\n"
output = (text) -> exchange.response.print text
session = exchange.getRequest().session
#session = instance : (cls) -> $session.get('instance').call cls
include = run = dsl = (script) -> support.include script

assert = (test, msg) -> throw msg or 'assert failed' if not test

startTime = java.lang.System.currentTimeMillis()
timer = ->
	Math.floor((java.lang.System.currentTimeMillis() - startTime) / 1000)
sleep = (seconds) -> support.$sleep(seconds)

jsArray = (arrayList) ->
	arrayList.get(index) for index in [0...arrayList.size()]

strings = (array) ->
	String(string) for string in array

statements = []
gwt = (pattern, action) -> statements.unshift {pattern:pattern, action:action}
gwt.processor = (statement) ->
  for item in statements
    match = item.pattern.exec(statement)
    if match
      item.action(match...)
      break # only runs first match
