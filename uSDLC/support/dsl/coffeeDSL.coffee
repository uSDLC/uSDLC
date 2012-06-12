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

javaArray = (arrayList) ->
	arrayList.get(index) for index in [0...arrayList.size()]

javaMap = (javaMap) ->
  map = {}
  for key in javaMap.keySet().toArray()
    map[String(key)] = String(javaMap.get(key).toString())
  return map

# convert an array of Java strings to a Javascript array
strings = (array) -> String(string) for string in array

class Store
  constructor: (@path) -> @store = usdlc.Store.base(path)
  dir: -> return javaArray @store.dir()
  delete: -> @store.delete()
  purge: -> fs(entry).delete() for entry in @dir()
  copyTo: (target) -> @store.copyTo(target)

fs = (path) -> new Store usdlc.Store.base path

statements = []
gwt = (pattern, action) -> statements.unshift {pattern:pattern, action:action}
gwt.processor = (statement) ->
  for item in statements
    match = item.pattern.exec(statement)
    if match
      item.action(match...)
      return # only runs first match
  throw "No command pattern for '#{statement}'\n"
